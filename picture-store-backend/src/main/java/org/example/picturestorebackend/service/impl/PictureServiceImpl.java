package org.example.picturestorebackend.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.picturestorebackend.exception.ErrorCode;
import org.example.picturestorebackend.exception.ThrowUtils;
import org.example.picturestorebackend.manager.FileManager;
import org.example.picturestorebackend.mapper.PictureMapper;
import org.example.picturestorebackend.model.dto.file.UploadPictureResult;
import org.example.picturestorebackend.model.dto.picture.PictureQueryRequest;
import org.example.picturestorebackend.model.dto.picture.PictureUploadRequest;
import org.example.picturestorebackend.model.entity.Picture;
import org.example.picturestorebackend.model.entity.User;
import org.example.picturestorebackend.model.vo.PictureVO;
import org.example.picturestorebackend.model.vo.UserVO;
import org.example.picturestorebackend.service.PictureService;
import org.example.picturestorebackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
* @author 17403
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-12-22 21:40:32
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService {

    @Resource
    private FileManager fileManager;

    @Resource
    private UserService userService;

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        //从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        //修改数据时, id不能为空， 有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNotNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        //如果传递了url, 才校验
        if(StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url过长");
        }
        if(StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //判断是新增还是删除
        Long pictureId = null;
        if(pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        //如果是更新吗，判断图片是否存在
        if(pictureId != null) {
            boolean exist = this.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .exists();
            ThrowUtils.throwIf(!exist, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        //上传图片，得到图片信息
        //按照用户id划分目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        //构造要入库的图片信息
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        //操作数据库
        //如果pictureId不为空，表示更新， 否则是新增
        if(pictureId != null) {
            //如果是更新，需要补充id 和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败，数据库操作失败");
        return PictureVO.objToVo(picture);
    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        //对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        //关联查询用户信息
        Long userId = picture.getUserId();
        if(userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPcitureVOpage(Page<Picture> picturePage, HttpServletRequest request) {
        return null;
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        return null;
    }
}




