package com.sunbox.service;

import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.images.ImageRequest;
import com.sunbox.domain.images.SaveImageRequest;

/**
 * 操作镜像相关接口
 */
public interface IImageService {

    ResultMsg listImage(ImageRequest imageRequest);

    ResultMsg listImageScript(ImageRequest imageRequest);

    ResultMsg saveImageScript(SaveImageRequest imagesRequest);

}
