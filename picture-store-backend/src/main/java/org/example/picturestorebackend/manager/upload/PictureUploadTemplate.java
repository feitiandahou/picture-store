package org.example.picturestorebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;
import org.example.picturestorebackend.config.CosClientConfig;
import org.example.picturestorebackend.exception.BusinessException;
import org.example.picturestorebackend.exception.ErrorCode;
import org.example.picturestorebackend.manager.CosManager;
import org.example.picturestorebackend.model.dto.file.UploadPictureResult;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * 图片上传模板
 */
@Slf4j
public abstract class PictureUploadTemplate {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        //校验图片
        validPicture(inputSource);
        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginalFilename(inputSource);
        //自己拼接文件上传路径，而不是使用原始文件名称，可以增加安全性
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;
        try{
            //上传文件
            file = File.createTempFile(uploadPath, null);
            processFile(inputSource, file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);

            CIUploadResult ciUploadResult = putObjectResult.getCiUploadResult();
            if(ciUploadResult == null){
                // 无法获取 ImageInfo，需走其他方式（如自己解析图片元数据）
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                        "上传失败，请确认是系统是否启用了数据万象（Cloud Infinite）功能");
            }
            //获取图片信息对象, 返回封装结果
            ImageInfo imageInfo = ciUploadResult.getOriginalInfo().getImageInfo();

            //获得图片处理结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                //获取压缩之后得到的文件信息
                CIObject compressedCiObject = objectList.get(0);
                //缩略图默认等于压缩图
                CIObject thumbnailCiObject = compressedCiObject;
                //有生成缩略图，才获取缩略图
                if(objectList.size() > 1) {
                    thumbnailCiObject = objectList.get(1);
                }
                //封装压缩图的返回结果
                return  buildResult(originalFilename, compressedCiObject, thumbnailCiObject);
            }
            return buildResult(originalFilename,file, uploadPath, imageInfo);
        }catch (Exception e){
            log.error("文件读写或上传COS失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            //临时文件清理
            this.deleteTempFile(file);
        }
    }
    /**
     * 校验输入源（本地文件或URL)
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 获取输入源的原始文件名
     */
    protected  abstract String getOriginalFilename(Object inputSource);

    /**
     * 处理输入源并生成本地临时文件
     */
    protected abstract void processFile(Object inputSource, File file) throws Exception;

    private UploadPictureResult buildResult(String originalFilename, CIObject compressedCiObject, CIObject thumbnailCiObject){
        //计算宽高
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        //封装返回结果
        UploadPictureResult uploadPictureResult= new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost()+"/"+ compressedCiObject.getKey());
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        //设置缩略图地址
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
        //返回可访问的地址
        return uploadPictureResult;
    }
    /**
     * 封装返回结果
     */
    private UploadPictureResult buildResult(String originalFilename, File file, String uploadPath, ImageInfo imageInfo) {
        //计算宽高
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        //封装返回结果
        UploadPictureResult uploadPictureResult= new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost()+"/"+ uploadPath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        //返回可访问的地址
        return uploadPictureResult;
    }
    /**
     * 清理临时文件
     */
    public void deleteTempFile(File file) {
        if(file == null) return;
        //删除临时文件
        boolean deleteResult = file.delete();
        if(!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }
}
