package com.azure.csu.tiger.rm.api.controller;

import com.azure.csu.tiger.rm.api.exception.RmException;
import com.azure.csu.tiger.rm.api.request.AddResourceGroupTagsRequest;
import com.azure.csu.tiger.rm.api.request.DeleteResourceGroupTagsRequest;
import com.azure.csu.tiger.rm.api.request.UpdateResourceGroupTagsRequest;
import com.azure.csu.tiger.rm.api.response.JobResponse;
import com.azure.csu.tiger.rm.api.response.ResourceGroupResponse;
import com.azure.csu.tiger.rm.api.service.ResourceGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@Api(tags="Resource Group rest api")
@RequestMapping("/api/v1/rgs")
@RestController
public class ResourceGroupController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceGroupController.class);

    @Autowired
    private ResourceGroupService resourceGroupService;

    @ApiOperation(value = "查询资源组")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ResourceGroupResponse.class),
    })
    @GetMapping(path = "/{name}", produces = {"application/json"})
    public ResponseEntity<ResourceGroupResponse> getResourceGroup(@PathVariable String name) {
        if (!StringUtils.hasText(name)) {
            logger.warn("resource group name is empty");
            throw new RmException(HttpStatus.BAD_REQUEST, "resource group name is empty");
        }
        logger.info("get resource group info, name: {}", name);
        name = name.trim();
        if (!resourceGroupService.existResourceGroup(name)) {
            logger.warn("Resource group {} not found", name);
            throw new RmException(HttpStatus.BAD_REQUEST, String.format("resource group %s not found", name));
        }
        ResourceGroupResponse response = resourceGroupService.getResourceGroup(name);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "删除资源组")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = JobResponse.class),
    })
    @DeleteMapping(path = "/{name}", produces = {"application/json"})
    public ResponseEntity<JobResponse> deleteResourceGroup(@PathVariable String name) {
        if (!StringUtils.hasText(name)) {
            logger.warn("resource group name is empty");
            throw new RmException(HttpStatus.BAD_REQUEST, "resource group name is empty");
        }
        logger.info("delete resource group, name: {}", name);
        name = name.trim();
        if (!resourceGroupService.existResourceGroup(name)) {
            logger.warn("Resource group {} not found", name);
            throw new RmException(HttpStatus.BAD_REQUEST, String.format("resource group %s not found", name));
        }
        JobResponse response = resourceGroupService.deleteResourceGroup(name);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "更新资源组标签")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ResourceGroupResponse.class),
    })
    @PutMapping(path = "/{name}/updateTags", produces = {"application/json"})
    public ResponseEntity<ResourceGroupResponse> updateResourceGroupTags(@PathVariable String name, @RequestBody UpdateResourceGroupTagsRequest request) {
        if (!StringUtils.hasText(name)) {
            logger.warn("resource group name is empty");
            throw new RmException(HttpStatus.BAD_REQUEST, "resource group name is empty");
        }
        if (CollectionUtils.isEmpty(request.getTags())) {
            logger.warn("tags is empty");
            throw new RmException(HttpStatus.BAD_REQUEST, "tags is empty");
        }
        logger.info("update resource group tags, name: {}, tags: {}", name, request.getTags());
        name = name.trim();
        if (!resourceGroupService.existResourceGroup(name)) {
            logger.warn("Resource group {} not found", name);
            throw new RmException(HttpStatus.BAD_REQUEST, String.format("resource group %s not found", name));
        }
        resourceGroupService.resetResourceGroupTags(name, request.getTags());
        ResourceGroupResponse response = resourceGroupService.getResourceGroup(name);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "增加资源组标签")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ResourceGroupResponse.class),
    })
    @PutMapping(path = "/{name}/addTags", produces = {"application/json"})
    public ResponseEntity<ResourceGroupResponse> addResourceGroupTags(@PathVariable String name, @RequestBody AddResourceGroupTagsRequest request) {
        if (!StringUtils.hasText(name)) {
            logger.warn("resource group name is empty");
            throw new RmException(HttpStatus.BAD_REQUEST, "resource group name is empty");
        }
        if (CollectionUtils.isEmpty(request.getTags())) {
            logger.warn("tags is empty");
            throw new RmException(HttpStatus.BAD_REQUEST, "tags is empty");
        }
        logger.info("add resource group tags, name: {}, tags: {}", name, request.getTags());
        name = name.trim();
        if (!resourceGroupService.existResourceGroup(name)) {
            logger.warn("Resource group {} not found", name);
            throw new RmException(HttpStatus.BAD_REQUEST, String.format("resource group %s not found", name));
        }
        resourceGroupService.createOrUpdateResourceGroupTags(name, request.getTags().stream().collect(Collectors.toMap(t -> t.getTagName(), t -> t.getTagValue())));
        ResourceGroupResponse response = resourceGroupService.getResourceGroup(name);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "删除资源组标签")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ResourceGroupResponse.class),
    })
    @DeleteMapping(path = "/{name}/deleteTags", produces = {"application/json"})
    public ResponseEntity<ResourceGroupResponse> deleteResourceGroupTags(@PathVariable String name, @RequestBody DeleteResourceGroupTagsRequest request) {
        if (!StringUtils.hasText(name)) {
            logger.warn("resource group name is empty");
            throw new RmException(HttpStatus.BAD_REQUEST, "resource group name is empty");
        }
        if (CollectionUtils.isEmpty(request.getTagNames())) {
            logger.warn("tags is empty");
            throw new RmException(HttpStatus.BAD_REQUEST, "tags is empty");
        }
        logger.info("delete resource group tags, name: {}, tags: {}", name, request.getTagNames());
        name = name.trim();
        if (!resourceGroupService.existResourceGroup(name)) {
            logger.warn("Resource group {} not found", name);
            throw new RmException(HttpStatus.BAD_REQUEST, String.format("resource group %s not found", name));
        }
        resourceGroupService.deleteResourceGroupTags(name, request.getTagNames());
        ResourceGroupResponse response = resourceGroupService.getResourceGroup(name);
        return ResponseEntity.ok(response);
    }
}
