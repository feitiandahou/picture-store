package org.example.picturestorebackend.model.dto.space;

import lombok.Data;
import org.example.picturestorebackend.common.PageRequest;

import java.io.Serializable;

/**
 * 查询空间请求
 */
@Data
public class SpaceUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别： 0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 最大数量
     */
    private long maxCount;

    /**
     * 最大容量
     */
    private long maxSize;

}
