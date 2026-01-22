package org.example.picturestorebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.picturestorebackend.model.dto.space.SpaceAddRequest;
import org.example.picturestorebackend.model.dto.space.SpaceQueryRequest;
import org.example.picturestorebackend.model.entity.Space;
import org.example.picturestorebackend.model.entity.User;
import org.example.picturestorebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 17403
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2026-01-11 15:16:32
*/
public interface SpaceService extends IService<Space> {
    /**
     * 创建空间
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 校验空间
     * @param space
     * @param add
     */
    void validSpace(Space space, boolean add);

    /**
     * 获取空间包装类（单条)
     * @param space
     * @param request
     * @return
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取空间包装类（分页)
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 获取查询对象
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 根据空间级别填充空间对象
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 校验空间权限
     * @param loginUser
     * @param space
     */
    void checkSpaceAuth(User loginUser, Space space);

}
