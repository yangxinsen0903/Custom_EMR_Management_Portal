package com.azure.csu.tiger.rm.api.controller;

import com.azure.csu.tiger.rm.api.exception.RmException;
import com.azure.csu.tiger.rm.api.request.*;
import com.azure.csu.tiger.rm.api.response.JobResponse;
import com.azure.csu.tiger.rm.api.response.ListVmResponse;
import com.azure.csu.tiger.rm.api.service.ResourceGroupService;
import com.azure.csu.tiger.rm.api.service.VirtualMachineService;
import com.azure.csu.tiger.rm.api.utils.ConstantUtil;
import com.azure.csu.tiger.rm.api.utils.JsonUtil;
import com.azure.csu.tiger.rm.api.utils.ParamValidator;
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

import java.io.IOException;

@Api(tags="VirtualMachine rest api")
@RequestMapping("/api/v1/vms")
@RestController
public class VirtualMachineController {

    private static final Logger logger = LoggerFactory.getLogger(VirtualMachineController.class);

    @Autowired
    private VirtualMachineService virtualMachineService;
    @Autowired
    private ResourceGroupService resourceGroupService;

    @ApiOperation(value = "创建虚机")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = JobResponse.class),
    })
    @PostMapping(produces = {"application/json"})
    public ResponseEntity<JobResponse> createGroups(@RequestBody CreateVmsRequest request) throws IOException {
        if (request != null) {
            logger.info("Create cluster groups' request is: {}", JsonUtil.obj2String(request));
        }

        if (!ParamValidator.checkClusterName(request.getClusterName())) {
            logger.warn("Cluster name is invalid.");
            throw new RmException(HttpStatus.BAD_REQUEST, "Cluster name is invalid.");
        }
        resourceGroupService.createResourceGroup(request.getRegion(), ConstantUtil.getResourceGroupName(request.getClusterName()));
        JobResponse response = virtualMachineService.createClusterGroups(request);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "批量扩容虚机")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = JobResponse.class),
    })
    @PutMapping(path = "/appendVirtualMachines", produces = {"application/json"})
    public ResponseEntity<JobResponse> appendGroups(@RequestBody AppendVmsRequest request) throws IOException {
        if (request != null) {
            logger.info("Create cluster groups' request is: {}", JsonUtil.obj2String(request));
        }

        if (!ParamValidator.checkClusterName(request.getClusterName())) {
            logger.warn("Cluster name is invalid.");
            throw new RmException(HttpStatus.BAD_REQUEST, "Cluster name is invalid.");
        }

        boolean existResourceGroup = resourceGroupService.existResourceGroup(ConstantUtil.getResourceGroupName(request.getClusterName()));
        if (!existResourceGroup) {
            throw new RmException(HttpStatus.BAD_REQUEST, String.format("Resource group %s not found.", ConstantUtil.getResourceGroupName(request.getClusterName())));
        }
        JobResponse response = virtualMachineService.appendClusterGroups(request);

        return ResponseEntity.ok(response);
    }


    @ApiOperation(value = "更新数据盘大小")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = JobResponse.class),
    })
    @PutMapping(path = "/updateVMsDiskSize", produces = {"application/json"})
    public ResponseEntity<JobResponse> updateVMsDiskSize(@RequestBody UpdateVmsDiskSizeRequest request) throws IOException {
        if (request != null) {
            logger.info("Update data disk's request is: {}", JsonUtil.obj2String(request));
        }

        JobResponse response = virtualMachineService.updateVmsDataDisk(request);

        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "更新PV2数据盘IOPS和MBPS")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = JobResponse.class),
    })
    @PutMapping(path = "/updateVMsDiskIopsAndMbps", produces = {"application/json"})
    public ResponseEntity<JobResponse> updateVMsDiskIopsAndMbps(@RequestBody UpdateVmsDiskIopsAndMbpsRequest request) throws IOException {
        if (request != null) {
            logger.info("Update data disk's request is: {}", JsonUtil.obj2String(request));
        }
        JobResponse response = virtualMachineService.updateVmsDataDiskIopsAndMbpsWithArm(request);

        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "删除VM")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = JobResponse.class),
    })
    @DeleteMapping(path = "/{clusterName}/{vmName}", produces = {"application/json"})
    public ResponseEntity<JobResponse> deleteVm(@PathVariable String clusterName, @PathVariable String vmName) {
        return deleteVmWithDns(clusterName, vmName, null);
    }

    @ApiOperation(value = "删除VM")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = JobResponse.class),
    })
    @DeleteMapping(path = "/{clusterName}/{vmName}/{dnsName}", produces = {"application/json"})
    public ResponseEntity<JobResponse> deleteVmWithDns(@PathVariable String clusterName, @PathVariable String vmName, @PathVariable(required = false) String dnsName) {
        if (!StringUtils.hasText(clusterName) || !StringUtils.hasText(clusterName)) {
            logger.warn("clusterName is empty or vmName is empty");
            throw new RmException(HttpStatus.BAD_REQUEST, "clusterName is empty or vmName is empty");
        }
        JobResponse jobResponse = virtualMachineService.deleteVirtualMachine(clusterName.trim(), vmName.trim(), dnsName == null ? dnsName : dnsName.trim());
        return ResponseEntity.ok(jobResponse);
    }

    @ApiOperation(value = "批量删除VM")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = JobResponse.class),
    })
    @PutMapping(path = "/deleteVirtualMachines", produces = {"application/json"})
    public ResponseEntity<JobResponse> deleteVms(@RequestBody DeleteVirtualMachinesRequest request) {
        if (request == null) {
            logger.warn("request is null");
            throw new RmException(HttpStatus.BAD_REQUEST, "request is null");
        }
        logger.info("Debug deleteVirtualMachines request dnsName: {}", request.getDnsNames());
        JobResponse response = virtualMachineService.deleteVirtualMachines(request);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "批量查询VM")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = JobResponse.class),
    })
    @PostMapping(path = "/listAll", produces = {"application/json"})
    public ResponseEntity<ListVmResponse> listVms(@RequestBody ListVmsRequest request) {
        if (CollectionUtils.isEmpty(request.getSubNetIds()) && !StringUtils.hasText(request.getRegion())) {
            logger.warn("SubNetIds and Region are both empty");
            throw new RmException(HttpStatus.BAD_REQUEST, "SubNetIds and Region can not be both empty");
        }
        if (request.getPageNo() != null && request.getPageNo() < 1) {
            logger.warn("PageNo is invalid");
            throw new RmException(HttpStatus.BAD_REQUEST, "PageNo must be greater than 0");
        }
        if (request.getPageSize() != null && request.getPageSize() < 1) {
            logger.warn("PageSize is invalid");
            throw new RmException(HttpStatus.BAD_REQUEST, "PageSize must be greater than 0");
        }
        ListVmResponse response = virtualMachineService.listVirtualMachines(request);
        return ResponseEntity.ok(response);
    }

}
