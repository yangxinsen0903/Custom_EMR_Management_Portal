package com.sunbox.sdpadmin.controller;

import com.sunbox.domain.BaseUserInfo;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.images.ImageRequest;
import com.sunbox.domain.images.SaveImageRequest;
import com.sunbox.service.IImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 操作镜像相关接口
 */
@RestController
@RequestMapping("/admin/image")
public class ImageController extends BaseAdminController {
    @Autowired
    private IImageService baseImageService;

    /**
     * 镜像列表分页
     *
     * @param imageRequest
     * @return
     */
    @GetMapping(value = "/listImage")
    public ResultMsg listImage(ImageRequest  imageRequest) {
        return baseImageService.listImage(imageRequest);
    }

    /**
     * 新增镜像和脚本信息
     *
     * @param imageRequest
     * @return
     */
    @PostMapping(value = "/saveImageScript")
    public ResultMsg saveImageScript(@Valid @RequestBody SaveImageRequest imageRequest) {
        BaseUserInfo userInfo = getUserInfo();
        imageRequest.setCreatedby(userInfo.getUserName());
        return baseImageService.saveImageScript(imageRequest);
    }

    /**
     * 镜像脚本列表分页
     *
     * @param imageRequest
     * @return
     */
    @GetMapping(value = "/listImageScript")
    public ResultMsg listImageScript(@Valid ImageRequest  imageRequest) {
        return baseImageService.listImageScript(imageRequest);
    }


}