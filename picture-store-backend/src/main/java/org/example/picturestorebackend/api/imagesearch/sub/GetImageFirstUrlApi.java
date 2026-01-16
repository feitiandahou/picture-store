package org.example.picturestorebackend.api.imagesearch.sub;

import lombok.extern.slf4j.Slf4j;
import org.example.picturestorebackend.exception.BusinessException;
import org.example.picturestorebackend.exception.ErrorCode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取图片列表接口的api (step2)
 */
@Slf4j
public class GetImageFirstUrlApi {
    /**
     * 获取图片列表页面地址
     * @param url
     * @return
     */
    public static String getImageFirstUrl(String url) {
        try{
            //使用 Jsoup获取HTML内容
            Document document = Jsoup.connect(url)
                    .timeout(5000)
                    .get();
            //获取所有<script>标签
            Elements scriptElements = document.getElementsByTag("script");
            //遍历找到包含`firstUrl`的脚本内容
            for (Element script : scriptElements) {
                String scriptContent = script.html();
                if(scriptContent.contains("\"firstUrl\"")){
                    //正则表达式提取firstUrl的值
                    Pattern pattern = Pattern.compile("\"firstUrl\"\\s*:\\s*\"(.*?)\"");
                    Matcher matcher = pattern.matcher(scriptContent);
                    if(matcher.find()){
                        String firstUrl = matcher.group(1);
                        //处理转义字符
                        firstUrl = firstUrl.replace("\\/", "/");
                        return firstUrl;
                    }
                }
            }
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未找到url");
        }catch (Exception e){
            log.error("搜索失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

    public static void main(String[] args) {
        //请求目标URL
        String imageUrl = "https://t15.baidu.com/it/u=3507828363,3940729037&fm=224&app=112&f=JPEG?w=356&h=499";
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = getImageFirstUrl(imagePageUrl);
        System.out.println("搜索成功， 结果URL:" + imageFirstUrl);
    }

}
