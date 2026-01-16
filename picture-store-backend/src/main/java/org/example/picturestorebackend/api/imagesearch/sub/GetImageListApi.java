package org.example.picturestorebackend.api.imagesearch.sub;

import cn.hutool.http.HttpException;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.picturestorebackend.api.imagesearch.model.ImageSearchResult;
import org.example.picturestorebackend.exception.BusinessException;
import org.example.picturestorebackend.exception.ErrorCode;

import java.util.List;

/**
 * 获取图片列表(step3)
 */
@Slf4j
public class GetImageListApi {
    /**
     * 获取图片列表
     *
     * @param url
     * @return
     */
    public static List<ImageSearchResult> getImageList(String url) {
        try {
            //发起HttpGet请求
            HttpResponse response = HttpUtil.createGet(url).execute();
            //获取响应内容
            int statusCode = response.getStatus();
            String body = response.body();

            //处理响应
            if(statusCode == 200){
                //解析JSON数据并处理
                return processResponse(body);
            }else {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
        } catch (Exception e){
            log.error("获取图片列表失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片列表失败");
        }
    }
    private static List<ImageSearchResult> processResponse(String responseBody) {
        //解析响应对象
        JSONObject jsonObject = new JSONObject(responseBody);
        if(!jsonObject.containsKey("data")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if(!data.containsKey("list")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        }
        JSONArray list = data.getJSONArray("list");
        return JSONUtil.toList(list, ImageSearchResult.class);
    }
    public static void main(String[] args){
        String url = "https://graph.baidu.com/ajax/pcsimi?carousel=503&entrance=GENERAL&extUiData%5BisLogoShow%5D=1&inspire=general_pc&limit=30&next=2&render_type=card&session_id=2394488701846118899&sign=126e89b9f9123244fc0c701768570605&tk=130b3&tpl_from=pc";
        List<ImageSearchResult> imageList = getImageList(url);
        System.out.println("搜索成功" + imageList);
    }
}
