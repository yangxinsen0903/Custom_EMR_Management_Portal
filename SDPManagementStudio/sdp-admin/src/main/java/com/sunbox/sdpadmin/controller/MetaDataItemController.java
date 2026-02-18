package com.sunbox.sdpadmin.controller;

import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.BaseUserInfo;
import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpadmin.controller.annotation.PermissionLimit;
import com.sunbox.service.IMetaDataItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/admin/meta")
public class MetaDataItemController extends BaseAdminController {

    @Autowired
    private IMetaDataItemService metaDataItemService;

    /**
     * 元数据查询 (不分页)
     *
     * @return
     */
    @PostMapping("/selectMetaDataList")
    public ResultMsg selectMetaDataList(@RequestBody JSONObject itemRequest) {
        return metaDataItemService.selectMetaDataList(itemRequest);
    }

    /**
     * 元数据新增
     *
     * @return
     */
    @PostMapping("/insertMetaData")
    @PermissionLimit(role = {"Administrator"})
    public ResultMsg insertMetaData(@RequestBody JSONObject itemRequest) {
        BaseUserInfo userInfo = getUserInfo();
        return metaDataItemService.insertMetaData(itemRequest, userInfo);
    }

    /**
     * 元数据更新
     *
     * @return
     */
    @PostMapping("/updateMetaData")
    @PermissionLimit(role = {"Administrator"})
    public ResultMsg updateMetaData(@RequestBody JSONObject itemRequest) {
        BaseUserInfo userInfo = getUserInfo();
        return metaDataItemService.updateMetaData(itemRequest, userInfo);
    }

    /**
     * 删除
     *
     * @return
     */
    @PostMapping("/deleteMetaDataById")
    @PermissionLimit(role = {"Administrator"})
    public ResultMsg deleteMetaDataById(@RequestBody JSONObject itemRequest) {
        return metaDataItemService.deleteMetaDataById(itemRequest.getLong("id"));
    }


}
