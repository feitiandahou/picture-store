package org.example.picturestorebackend.controller;

import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.example.picturestorebackend.annotation.AuthCheck;
import org.example.picturestorebackend.common.BaseResponse;
import org.example.picturestorebackend.common.ResultUtils;
import org.example.picturestorebackend.constant.UserConstant;
import org.example.picturestorebackend.exception.BusinessException;
import org.example.picturestorebackend.exception.ErrorCode;
import org.example.picturestorebackend.manager.CosManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@RestController
@Slf4j
public class TestCosController {
    @Resource
    CosManager cosManager;

    public TestCosController(CosManager cosManager) {
        this.cosManager = cosManager;
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file")MultipartFile multipartFile) {
        //文件目录
        String fileName = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/%s", fileName);
        File file = null;
        try {
            //上传文件
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filePath,file);
            //返回可访问地址
            return ResultUtils.success(filePath);

        }catch (Exception e) {
            log.error("file upload error, filepath = "+filePath, "上传失败");
        }finally {
            if(file != null) {
                //删除临时文件
                boolean delete = file.delete();
                if(!delete) {
                    log.error("file delete error, filepath = {}", filePath);
                }
            }
        }
        return null;
    }

    /**
     * 测试文件下载
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try{
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            //处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            //设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            //写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        }catch (Exception e){
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        }finally {
            if(cosObjectInput != null) {
                cosObjectInput.close();
            }
        }

    }
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }
}

