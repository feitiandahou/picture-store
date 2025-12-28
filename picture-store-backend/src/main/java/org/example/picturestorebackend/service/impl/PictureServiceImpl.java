package org.example.picturestorebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.picturestorebackend.exception.BusinessException;
import org.example.picturestorebackend.exception.ErrorCode;
import org.example.picturestorebackend.exception.ThrowUtils;
import org.example.picturestorebackend.manager.FileManager;
import org.example.picturestorebackend.manager.upload.FilePictureUpload;
import org.example.picturestorebackend.manager.upload.PictureUploadTemplate;
import org.example.picturestorebackend.manager.upload.UrlPictureUpload;
import org.example.picturestorebackend.mapper.PictureMapper;
import org.example.picturestorebackend.model.dto.file.UploadPictureResult;
import org.example.picturestorebackend.model.dto.picture.PictureQueryRequest;
import org.example.picturestorebackend.model.dto.picture.PictureReviewRequest;
import org.example.picturestorebackend.model.dto.picture.PictureUploadByBatchRequest;
import org.example.picturestorebackend.model.dto.picture.PictureUploadRequest;
import org.example.picturestorebackend.model.entity.Picture;
import org.example.picturestorebackend.model.entity.User;
import org.example.picturestorebackend.model.enums.PictureReviewStatusEnum;
import org.example.picturestorebackend.model.vo.PictureVO;
import org.example.picturestorebackend.model.vo.UserVO;
import org.example.picturestorebackend.service.PictureService;
import org.example.picturestorebackend.service.UserService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 17403
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-12-22 21:40:32
*/
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService {

    @Resource
    private FileManager fileManager;

    @Resource
    private UserService userService;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        //从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        //修改数据时, id不能为空， 有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        //如果传递了url, 才校验
        if(StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url过长");
        }
        if(StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //判断是新增还是删除
        Long pictureId = null;
        if(pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        //如果是更新吗，判断图片是否存在
        if(pictureId != null && pictureId > 0){
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            //仅本人或管理员可编辑图片
            if(!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
        //上传图片，得到图片信息
        //按照用户id划分目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        //根据inputSource的类型区分上传方式
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if(inputSource instanceof String){
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);

        //构造要入库的图片信息
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        //支持外层传递图片名称
        String picName = uploadPictureResult.getPicName();
        if(pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }
        picture.setName(picName);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        //补充审核参数
        this.fillReviewParams(picture, loginUser);
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

    /**
     * 分页获取图片封装
     * @param picturePage
     * @param request
     * @return
     */
    @Override
    public Page<PictureVO> getPcitureVOpage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if(CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        //对象列表 -> 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
        //1.关联查询用户信息
        //1,2,3,4
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        //1->user1 2-> user2
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        //2.填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if(userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }


    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if(pictureQueryRequest == null) {
            return queryWrapper;
        }
        //从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();

        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        //从多字段中搜索
        if(StrUtil.isNotBlank(searchText)) {
            //需要拼接查询条件
            queryWrapper.and(qw -> qw.like("name", searchText)
                        .or()
                        .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        // JSON数组查询
        if(CollUtil.isNotEmpty(tags)) {
            /* and (tag like "%\"Java\"%" and like "%\"Python\"%" */
            for (String tag: tags) {
                queryWrapper.like("tags", "\"" + tag + "\"" );
            }
        }
        //排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        //1.校验参数
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        String reviewMessage = pictureReviewRequest.getReviewMessage();;
        if(id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.判断图片是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //3.校验审核状态是否重复
        if(oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
        }
        //4. 数据库操作
        Picture updatePicture = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if(userService.isAdmin(loginUser)){
            //管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(new Date());
        } else {
            //非管理员，无论编辑还是创建，默认都是待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        //校验参数
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count == null || count <= 0 || count > 30, ErrorCode.PARAMS_ERROR, "最多 30 条");
        //名称前缀默认等于搜索关键词
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if(StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        //抓取内容
        String encodedSearchText = null;
        try {
            encodedSearchText = URLEncoder.encode(searchText.trim(), String.valueOf(StandardCharsets.UTF_8));
        }catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        ThrowUtils.throwIf(encodedSearchText == null, ErrorCode.SYSTEM_ERROR);

        String fetchUrl = "https://cn.bing.com/images/async?q=" + encodedSearchText + "&mmasync=1";
        Document document;
        try {
            // 设置 User-Agent 模拟浏览器，降低被反爬概率
            document = Jsoup.connect(fetchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36")
                    .timeout(10000)
                    .get();
        }catch (IOException e){
            log.error("获取页面失败", e);
            throw  new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        //解析内容
        Elements imgItems = document.select("div.imgpt a.iusc");
        if(imgItems.isEmpty()){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        //遍历元素，依次处理上传图片
        int uploadCount = 0;
        for(Element aTag : imgItems) {
            if(uploadCount >= count) break;

            String dataM = aTag.attr("m");
            if(StrUtil.isBlank(dataM)) {
                log.info("跳过无效图片项，缺少calss='m'");
                continue;
            }
            //处理图片的地址，防止转义或者和对象存储冲突的问题
            //例如xxx.cn?params=xxx,应该只保留xxx.cn
            String originalImageUrl  = extractOriginalImageUrl(dataM);
            if(StrUtil.isBlank(originalImageUrl )) {
                log.warn("无法解析原图 URL:{}", dataM);
                continue;
            }
            // 清理 URL：移除查询参数（可选，但推荐）
            originalImageUrl = cleanImageUrl(originalImageUrl);

            // 跳过无效协议（如 data:image、javascript 等）
            if (!originalImageUrl.startsWith("http")) {
                log.warn("非 HTTP 图片链接，跳过: {}", originalImageUrl);
                continue;
            }
            //上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(originalImageUrl );
            pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
            try {
                PictureVO pictureVO = this.uploadPicture(originalImageUrl , pictureUploadRequest, loginUser);
                log.info("图片上传成功, id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }

        }
        return  uploadCount;
    }
    private String extractOriginalImageUrl(String dataMJson) {
        // 3. HTML 解码：把 &quot; 转回 "
        String decodedJson = HtmlUtil.unescape(dataMJson); // Hutool 的 HtmlUtil
        // data-m 是一个 JSON 字符串，例如：
        // {"murl":"https://example.com/cat.jpg","turl":"...", ...}
        try{
            JSONObject json = JSONUtil.parseObj(decodedJson);
            return json.getStr("murl");
        } catch (Exception e) {
            log.warn("解析 calss='m'的json属性失败{}", dataMJson, e);
            return null;
        }
    }
    private String cleanImageUrl(String url) {
        if (StrUtil.isBlank(url)) return url;
        int qIndex = url.indexOf('?');
        if (qIndex > 0) {
            return url.substring(0, qIndex);
        }
        return url;
    }
}




