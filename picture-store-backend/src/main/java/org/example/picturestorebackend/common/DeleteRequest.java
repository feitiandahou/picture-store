package org.example.picturestorebackend.common;

import lombok.Data;

/**
 * 通用的删除请求类
 */
@Data
public class DeleteRequest {
    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
