package org.example.picturestorebackend.api.imagesearch;

import lombok.extern.slf4j.Slf4j;
import org.example.picturestorebackend.api.imagesearch.model.ImageSearchResult;
import org.example.picturestorebackend.api.imagesearch.sub.GetImageFirstUrlApi;
import org.example.picturestorebackend.api.imagesearch.sub.GetImageListApi;
import org.example.picturestorebackend.api.imagesearch.sub.GetImagePageUrlApi;

import java.util.List;

@Slf4j
public class ImageSearchApiFacade {
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        List<ImageSearchResult> imageList = searchImage("https://t14.baidu.com/it/u=3080988030,3794722778&fm=224&app=112&f=JPEG?w=500&h=500");
        System.out.println("结果列表"+ imageList);
    }

}
