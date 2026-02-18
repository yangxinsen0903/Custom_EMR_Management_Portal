package com.sunbox.sdpcompose;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.sunbox.sdpcompose.model.azure.request.*;
import com.sunbox.sdpcompose.model.azure.response.AzureResponse;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SdpComposeTest {
    public static void main(String[] args) {
        AzureResponse azureResponse = new AzureResponse();
        List<JSONObject> list = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        String nam = "127.0.0.1";
        jsonObject.put("nam", nam);
        list.add(jsonObject);
        azureResponse.setStatus("success");
        azureResponse.setCode("200");
        azureResponse.setData(list);
        azureResponse.setMessage("");
//        System.out.println(JSON.toJSONString(azureResponse));

        AzureVmsRequest azureVmsRequest = new AzureVmsRequest();
        DataDiskElement dataDiskElement = new DataDiskElement();
        TagElement tagElement = new TagElement();
        List<DataDiskElement> dataDiskElements = new ArrayList<>();
        List<TagElement> tagElements = new ArrayList<>();
        dataDiskElements.add(dataDiskElement);
        tagElements.add(tagElement);
        OSDiskClass osDiskClass = new OSDiskClass();
        SpecClass specClass = new SpecClass();
        specClass.setOsDisk(osDiskClass);
        specClass.setDataDisks(dataDiskElements);
        VmGroups vmGroups = new VmGroups();
        vmGroups.setVirtualMachineSpec(specClass);
        List<VmGroups> vmGroupsList = new ArrayList<>();
        vmGroupsList.add(vmGroups);
        azureVmsRequest.setVirtualMachineGroups(vmGroupsList);
        System.out.println(JSON.toJSONString(azureVmsRequest,SerializerFeature.WriteNullStringAsEmpty));

        AzureExecuteJobPlaybookRequest azureExecuteJobPlaybookRequest = new AzureExecuteJobPlaybookRequest();
        System.out.println(JSON.toJSONString(azureExecuteJobPlaybookRequest,SerializerFeature.WriteNullStringAsEmpty));
    }
}
