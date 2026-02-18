package com.sunbox.service.impl;

import com.sunbox.dao.mapper.BaseImageScriptsMapper;
import com.sunbox.dao.mapper.BaseImagesMapper;
import com.sunbox.dao.mapper.BaseReleaseVmImgMapper;
import com.sunbox.domain.BaseImageScripts;
import com.sunbox.domain.BaseImages;
import com.sunbox.domain.BaseReleaseVmImg;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.enums.VmRoleType;
import com.sunbox.domain.images.ImageRequest;
import com.sunbox.domain.images.ImageResponse;
import com.sunbox.domain.images.SaveImageRequest;
import com.sunbox.domain.images.SaveImageScriptRequest;
import com.sunbox.service.IImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 操作镜像相关接口
 */
@Service
public class ImageServiceImpl implements IImageService {
    @Autowired
    private BaseImagesMapper baseImagesMapper;

    @Autowired
    private BaseReleaseVmImgMapper baseReleaseVmImgMapper;

    @Autowired
    private BaseImageScriptsMapper baseImageScriptsMapper;

    @Override
    public ResultMsg listImage(ImageRequest imageRequest) {
        imageRequest.page();
        List<ImageResponse> imagesResponses = baseReleaseVmImgMapper.listImageByReleaseVersion(imageRequest.getReleaseVersion(), imageRequest.getPageStart(), imageRequest.getPageLimit());
        int total = baseReleaseVmImgMapper.countImageByReleaseVersion(imageRequest.getReleaseVersion());
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);
        resultMsg.setData(imagesResponses);
        resultMsg.setTotal(total);
        return resultMsg;
    }

    /**
     * @param imagesRequest
     * @return
     */
    @Override
    public ResultMsg saveImageScript(SaveImageRequest imagesRequest) {
        Date date = new Date();
        BaseImages baseImages = new BaseImages();
        baseImages.setImgId(UUID.randomUUID().toString().replaceAll("-", ""));
        baseImages.setOsImageId(imagesRequest.getOsImageId());
        baseImages.setOsVersion(imagesRequest.getOsVersion());
        baseImages.setOsImageType("CustomImage");
        baseImages.setCreatedby(imagesRequest.getCreatedby());
        baseImages.setCreatedTime(date);
        int insert = baseImagesMapper.insert(baseImages);
        if (insert > 0) {
            //脚本信息
            List<SaveImageScriptRequest> imageScriptList = imagesRequest.getImageScriptList();
            for (int i = 0; i < imageScriptList.size(); i++) {
                SaveImageScriptRequest imageScriptRequest = imageScriptList.get(i);
                BaseImageScripts baseImageScripts = new BaseImageScripts();
                baseImageScripts.setImgId(baseImages.getImgId());
                baseImageScripts.setCreatedby(baseImages.getCreatedby());
                baseImageScripts.setCreatedTime(date);
                baseImageScripts.setScriptName(imageScriptRequest.getScriptName());
                baseImageScripts.setImgScriptId(UUID.randomUUID().toString().replaceAll("-", ""));
                baseImageScripts.setExtraVars(imageScriptRequest.getExtraVars());
                baseImageScripts.setScriptFileUri(imageScriptRequest.getScriptFileUri());
                baseImageScripts.setPlaybookUri(imageScriptRequest.getPlaybookUri());
                baseImageScripts.setSortNo(i);
                baseImageScriptsMapper.insert(baseImageScripts);
            }

            //版本信息
            BaseReleaseVmImg baseReleaseVmImg = new BaseReleaseVmImg();
            baseReleaseVmImg.setCreatedby(baseImages.getCreatedby());
            baseReleaseVmImg.setCreatedTime(date);
            baseReleaseVmImg.setOsVersion(imagesRequest.getOsVersion());
            baseReleaseVmImg.setOsImageType("CustomImage");
            baseReleaseVmImg.setImgId(baseImages.getImgId());
            baseReleaseVmImg.setVmRole(VmRoleType.TASK.getVmRole());
            baseReleaseVmImg.setReleaseVersion(imagesRequest.getReleaseVersion());
            BaseReleaseVmImg releaseVmImg = baseReleaseVmImgMapper.selectByPrimaryKey(imagesRequest.getReleaseVersion(), VmRoleType.TASK.getVmRole());
            if (releaseVmImg == null) {
                baseReleaseVmImgMapper.insert(baseReleaseVmImg);
            }else{
                baseReleaseVmImgMapper.updateByPrimaryKeySelective(baseReleaseVmImg);
            }
        }
        return ResultMsg.SUCCESS();
    }

    @Override
    public ResultMsg listImageScript(ImageRequest imageRequest) {
        imageRequest.page();
        List<BaseImageScripts> imageScripts = baseImageScriptsMapper.getAllByImgId(imageRequest.getImgId(), imageRequest.getPageStart(), imageRequest.getPageLimit());
        int total = baseImageScriptsMapper.countByImgId(imageRequest.getImgId());
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);
        resultMsg.setData(imageScripts);
        resultMsg.setTotal(total);
        return resultMsg;
    }
}
