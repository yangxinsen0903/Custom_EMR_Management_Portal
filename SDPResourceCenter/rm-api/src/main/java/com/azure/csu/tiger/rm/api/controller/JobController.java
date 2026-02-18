package com.azure.csu.tiger.rm.api.controller;

import com.azure.csu.tiger.rm.api.exception.RmException;
import com.azure.csu.tiger.rm.api.response.GetJobStatusResponse;
import com.azure.csu.tiger.rm.api.service.JobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags="Job rest api")
@RequestMapping("/api/v1/jobs")
@RestController
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    @Autowired
    private JobService jobService;

    @ApiOperation(value = "查询任务执行状态")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = GetJobStatusResponse.class),
    })
    @GetMapping(path = "/{id}", produces = {"application/json"})
    public ResponseEntity<GetJobStatusResponse> getJobStatus(@PathVariable String id) {
        logger.info("get job status, id: {}", id);
        if (!StringUtils.hasText(id)) {
            logger.warn("job id is empty");
            throw new RmException(HttpStatus.BAD_REQUEST, "job id is empty");
        }
        GetJobStatusResponse response = jobService.getJobStatus(id);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "查询任务执行结果细节")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = GetJobStatusResponse.class),
    })
    @GetMapping(path = "/{id}/provisionDetail", produces = {"application/json"})
    public ResponseEntity<GetJobStatusResponse> getJobProvisionDetail(@PathVariable String id) {
        logger.info("get job provisionDetail, id: {}", id);
        if (!StringUtils.hasText(id)) {
            logger.warn("job id is empty");
            throw new RmException(HttpStatus.BAD_REQUEST, "job id is empty");
        }
        return ResponseEntity.ok(null);
    }
}
