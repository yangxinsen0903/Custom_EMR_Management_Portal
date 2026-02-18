package com.azure.csu.tiger.rm.api.controller;

import com.azure.csu.tiger.rm.api.exception.RmException;
import com.azure.csu.tiger.rm.api.response.JobResponse;
import com.azure.csu.tiger.rm.api.service.VirtualMachineService;
import com.azure.csu.tiger.rm.api.utils.ConstantUtil;
import com.azure.csu.tiger.rm.api.utils.HttpUtil;
import com.nimbusds.jose.util.Pair;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Api(tags="Compute Fleet rest api")
@RequestMapping("/api/v1/fleet")
@RestController
public class ComputeFleetController {

    private static final Logger logger = LoggerFactory.getLogger(ComputeFleetController.class);

    @Autowired
    private VirtualMachineService virtualMachineService;

    @Autowired
    private HttpUtil httpUtil;

    @ApiOperation(value = "查询fleet")
    @GetMapping(path = "/{cluster}/{group}", produces = {"application/json"})
    public ResponseEntity<String> getFleet(@PathVariable String cluster, @PathVariable String group) {
        String resourceGroup = ConstantUtil.getResourceGroupName(cluster);
        String fleetName = ConstantUtil.buildFleetName(cluster, group);
        Pair<Integer, String> response = httpUtil.doGetFleet(resourceGroup, fleetName);
        if (response.getLeft() == HttpStatus.NOT_FOUND.value()) {
            throw new RmException(HttpStatus.NOT_FOUND, "Fleet not found, cluster: " + cluster + ", group: " + group);
        }
        return ResponseEntity.ok(response.getRight());
    }

    @ApiOperation(value = "删除fleet")
    @DeleteMapping(path = "/{cluster}/{group}", produces = {"application/json"})
    public ResponseEntity<JobResponse> deleteFleet(@PathVariable String cluster, @PathVariable String group) {
        if(!StringUtils.hasLength(cluster) || !StringUtils.hasLength(group)) {
            throw new RmException(HttpStatus.BAD_REQUEST, "Cluster or group is empty");
        }
        JobResponse response = virtualMachineService.deleteClusterGroup(cluster, group);

        return ResponseEntity.ok(response);
    }

}
