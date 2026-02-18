package com.azure.csu.tiger.ansible.api.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AnsibleTransResponse {

    private String transactionId;

    private List<AgentExecuteDTO> jobList;

}
