package org.example.picturestorebackend.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建扩图任务相应类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOutPaintingTaskResponse {
    private Output output;
    /**
     * 表示人物的输出信息
     */
    @Data
    public static class Output {
        /*
        任务 ID
         */
        private String taskId;

        /**
         * 任务状态
         * PENDING：任务排队中
         * RUNNING：任务处理中
         * SUCCEEDED：任务执行成功
         * FAILED：任务执行失败
         * CANCELED：任务已取消
         * UNKNOWN：任务不存在或状态未知
         */
        private String taskStatus;
    }

    /**
     * 接口失败的错误码
     * 接口成功请求不会返回该参数
     */
    private String code;

    /**
     * 接口错误信息
     * 接口成功请求不会返回该参数
     */
    private String message;

    /**
     * 请求唯一标识
     * 可用于请求明细溯源和问题排查
     */
    private String requestId;
}
