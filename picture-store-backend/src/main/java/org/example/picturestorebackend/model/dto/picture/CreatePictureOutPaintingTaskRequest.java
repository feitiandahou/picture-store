package org.example.picturestorebackend.model.dto.picture;

import lombok.Data;
import org.example.picturestorebackend.api.model.CreateOutPaintingTaskRequest;

import java.io.Serializable;

/**
 * 创建扩图任务请求
 */
@Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {
    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;

}
