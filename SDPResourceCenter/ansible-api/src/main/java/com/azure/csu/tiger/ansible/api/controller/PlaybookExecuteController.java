package com.azure.csu.tiger.ansible.api.controller;

import com.azure.csu.tiger.ansible.api.config.ConstantsConfig;
import com.azure.csu.tiger.ansible.api.dao.JobListDao;
import com.azure.csu.tiger.ansible.api.jooq.tables.records.JoblistRecord;
import com.azure.csu.tiger.ansible.api.model.AnsibleExecuteMsg;
import com.azure.csu.tiger.ansible.api.service.impl.ServiceBusClientTopicSubServiceImpl;
import com.azure.csu.tiger.ansible.api.bo.AnsibleTransResponse;
import com.azure.csu.tiger.ansible.api.service.PlaybookExecuteService;
import com.azure.csu.tiger.ansible.api.vo.ExecuteResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Api(tags="Ansible Execute API")
@RequestMapping("/api/PlaybookExecute/")
@RestController
public class PlaybookExecuteController {

    private static final Logger logger = LoggerFactory.getLogger(PlaybookExecuteController.class);

    @Autowired
    PlaybookExecuteService playbookExecuteService;
    @Autowired
    private ServiceBusClientTopicSubServiceImpl serviceBusCTSServiceImpl;
    @Autowired
    private JobListDao jobListDao;

    @ApiOperation(value = "执行Playbook")
    @PostMapping(path = "/execute", produces = {"application/json"})
    public ResponseEntity<ExecuteResponse> submitData(@Valid @RequestBody AnsibleExecuteMsg ansibleExecuteMsg, BindingResult bindingResult) {

        ResponseEntity response;
        String  requestMsgTransactionId = ansibleExecuteMsg.getTransactionId();
        try {
            logger.info("Received Execute Playbook Request: @@@@@@ {}", ansibleExecuteMsg);

            if (bindingResult.hasErrors()) {

                List errorList = bindingResult.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
                response = ResponseEntity.badRequest().body(ExecuteResponse.badRequest(errorList.toString()));
                playbookExecuteService.saveAnsibleControlLog(ansibleExecuteMsg.toString(),response.toString(), requestMsgTransactionId,ConstantsConfig.JOB_STATUS_FAIL.getNumberValue());
                logger.info("Validate Request Message failed "+response.toString());
                return response;
            }

            List<JoblistRecord> joblistRecords = jobListDao.fetchTransJobList(ansibleExecuteMsg.getTransactionId());
            if (joblistRecords.size() > 0) {
                logger.info("TransactionId already exists: {}", ansibleExecuteMsg.getTransactionId());
                response = ResponseEntity.badRequest().body(ExecuteResponse.badRequest("TransactionId already exists"));
                playbookExecuteService.saveAnsibleControlLog(ansibleExecuteMsg.toString(),response.toString(), requestMsgTransactionId,ConstantsConfig.JOB_STATUS_FAIL.getNumberValue());
                return response;
            }
            // Process the requestData
            List<String> jobList = playbookExecuteService.sendMessageToServiceBusTopic(ansibleExecuteMsg);

            response = ResponseEntity.ok(ExecuteResponse.success(jobList));

            playbookExecuteService.saveAnsibleControlLog(ansibleExecuteMsg.toString(),response.toString(), requestMsgTransactionId,ConstantsConfig.JOB_STATUS_SUCCESS.getNumberValue());
        } catch (Exception e) {
            response = ResponseEntity.badRequest().body(ExecuteResponse.badRequest(e.getMessage()));
            playbookExecuteService.saveAnsibleControlLog(ansibleExecuteMsg.toString(),response.toString(), requestMsgTransactionId,ConstantsConfig.JOB_STATUS_FAIL.getNumberValue());
            logger.error("Execute Playbook API Exception: @@@@@@ "+e.getMessage());
            throw new RuntimeException(e);
        }

        // Returning response entity
        return response;
    }

    @ApiOperation(value = "获取Playbook Job运行结果")
    @GetMapping(path="/GetTransactionExecuteResult/{transactionId}", produces = {"application/json"})
    public ResponseEntity getTransactionResult(@PathVariable String transactionId) {

        logger.info("Get Transaction Execute Result, TransactionID: {}", transactionId);
        if (!StringUtils.hasText(transactionId)) {
            logger.warn("transactionId id is empty");
            ResponseEntity.badRequest().build();
        }
        AnsibleTransResponse response = playbookExecuteService.fetchTransactionResult(transactionId);
        return ResponseEntity.ok(response);
    }

    // Existing methods
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

}
