package com.azure.csu.tiger.ansible.api.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AgentExecuteDTO {

    public String jobid;

    public String hosts;

    @JsonProperty("jobStatus")
    public String status;

    public String jobresult;
}
