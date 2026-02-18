package com.azure.csu.tiger.rm.api.controller;

import com.azure.csu.tiger.rm.api.enums.UploadFileEnum;
import com.azure.csu.tiger.rm.api.helper.AzureResourceHelper;
import com.azure.csu.tiger.rm.api.response.GetVmInfoVo;
import com.azure.csu.tiger.rm.api.response.UploadFileResponse;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api/v1/test")
@RestController
public class TestController {

//    @Autowired
//    AzureResourceHelper azureResourceHelper;
//
//    @GetMapping(path = "/dns/create")
//    public ResponseEntity<UploadFileResponse> createDnsRecords() {
//        List<GetVmInfoVo> vmNames = Lists.newArrayList();
//        for(int index = 1; index < 1001; index ++) {
//            GetVmInfoVo vo = new GetVmInfoVo();
//            vo.setDnsRecord("vm_eric_test_" + index);
//            vo.setPrivateIp("10.0.0.101");
//            vmNames.add(vo);
//        }
//        azureResourceHelper.registerVirtualMachinesToPrivateDnsWithRetry(vmNames, "test.sdp.com");
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping(path = "/dns/remove")
//    public ResponseEntity<UploadFileResponse> removeDnsRecords() {
//        List<String> vmNames = Lists.newArrayList();
//        for(int index = 1; index < 1001; index ++) {
//            vmNames.add("vm_eric_test_" + index + ".test.sdp.com");
//        }
//        azureResourceHelper.removeVirtualMachinesFromPrivateDnsWithRetry(vmNames, "test.sdp.com");
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping(path = "/dns/get")
//    public ResponseEntity<UploadFileResponse> getDnsRecord() {
//        azureResourceHelper.getARecord("vm_eric_test_200", "test.sdp.com");
//        azureResourceHelper.getARecord("afterstart-v20-script001-amb-sxao86", "wu2dns.shein.com");
//        return ResponseEntity.ok().build();
//    }
}
