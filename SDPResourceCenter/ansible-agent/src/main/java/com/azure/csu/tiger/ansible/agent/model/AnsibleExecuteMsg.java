package com.azure.csu.tiger.ansible.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

/**
 * AnsibleExecuteMsg
 */
@Validated
@ApiModel
@ToString
@Data
@NoArgsConstructor
public class AnsibleExecuteMsg {

  @NotBlank(message = "transactionId cannot be blank")
  @JsonProperty("transactionId")
  private String transactionId = null;

  @JsonProperty("jobId")
  private String jobId = null;

  @NotNull(message = "can not be null")
  @NotEmpty(message = "can not be empty")
  @JsonProperty("nodeList")
  private List<String> nodeList = null;

  @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9+.-]*://.*",message = "must be valid uri")
  @JsonProperty("playbookUri")
  private String playbookUri = null;

  @NotNull(message = "Can not be null")
  @Digits(integer = 1,fraction = 0)
  @JsonProperty("playbookType")
  private Integer playbookType = null;

  @NotNull(message = "can not be null")
  @NotEmpty(message = "can not be empty")
  @JsonProperty("scriptFileUris")
  @Valid
  private List<String> scriptFileUris = null;

  @NotNull(message = "can not be null")
  @JsonProperty("extraVars")
  private String extraVars = null;

  @NotNull(message = "can not be null")
  @JsonProperty("apiVersion")
  private String apiVersion = null;

  @NotNull(message = "can not be null")
  @JsonProperty("username")
  private String username = null;

  @NotNull(message = "can not be null")
  @DecimalMin("0")
  @JsonProperty("timeout")
  private Integer timeout = null;

  private String sshKeyVaultName;

  private String sshPrivateSecretName;


  public AnsibleExecuteMsg(AnsibleExecuteMsg sAnsibleExecuteMsg) {
    this.jobId = sAnsibleExecuteMsg.jobId;
    this.transactionId = sAnsibleExecuteMsg.transactionId;
    this.nodeList = sAnsibleExecuteMsg.nodeList;
    this.playbookUri = sAnsibleExecuteMsg.playbookUri;
    this.playbookType = sAnsibleExecuteMsg.playbookType;
    this.scriptFileUris = sAnsibleExecuteMsg.scriptFileUris;
    this.extraVars = sAnsibleExecuteMsg.extraVars;
    this.apiVersion = sAnsibleExecuteMsg.apiVersion;
    this.username = sAnsibleExecuteMsg.username;
    this.timeout = sAnsibleExecuteMsg.timeout;
    this.sshKeyVaultName = sAnsibleExecuteMsg.sshKeyVaultName;
    this.sshPrivateSecretName = sAnsibleExecuteMsg.sshPrivateSecretName;
  }

}
