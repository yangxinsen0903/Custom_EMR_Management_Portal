package com.azure.csu.tiger.ansible.api.model;

import com.azure.csu.tiger.ansible.api.config.CustomStringListDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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

  @NotNull(message = "nodeList can not be null")
  @NotEmpty(message = "nodeList can not be empty")
  @JsonProperty("nodeList")
  @JsonDeserialize(using = CustomStringListDeserializer.class)
  private List<String> nodeList = null;

  @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9+.-]*://.*",message = "playbookUri must be valid uri")
  @JsonProperty("playbookUri")
  private String playbookUri = null;

  @NotNull(message = "playbookType Can not be null")
  @Digits(integer = 1,fraction = 0)
  @JsonProperty("playbookType")
  private Integer playbookType = null;

  @NotNull(message = "scriptFileUris can not be null")
  @NotEmpty(message = "scriptFileUris can not be empty")
  @JsonProperty("scriptFileUris")
  @Valid
  private List<String> scriptFileUris = null;

  @NotNull(message = "extraVars can not be null")
  @JsonProperty("extraVars")
  private String extraVars = null;

  @NotNull(message = "apiVersion can not be null")
  @JsonProperty("apiVersion")
  private String apiVersion = null;

  @JsonProperty("username")
  private String username = null;

  @NotNull(message = "timeout can not be null")
  @DecimalMin("0")
  @JsonProperty("timeout")
  private Integer timeout = null;

  private String sshKeyVaultName;

  private String sshPrivateSecretName;

  public AnsibleExecuteMsg copy() {
    AnsibleExecuteMsg sAnsibleExecuteMsg = new AnsibleExecuteMsg();
    sAnsibleExecuteMsg.jobId = this.jobId;
    sAnsibleExecuteMsg.transactionId = this.transactionId;
    sAnsibleExecuteMsg.nodeList = this.nodeList;
    sAnsibleExecuteMsg.playbookUri = this.playbookUri;
    sAnsibleExecuteMsg.playbookType = this.playbookType;
    sAnsibleExecuteMsg.scriptFileUris = this.scriptFileUris;
    sAnsibleExecuteMsg.extraVars = this.extraVars;
    sAnsibleExecuteMsg.apiVersion = this.apiVersion;
    sAnsibleExecuteMsg.username = this.username;
    sAnsibleExecuteMsg.timeout = this.timeout;
    sAnsibleExecuteMsg.sshKeyVaultName = this.sshKeyVaultName;
    sAnsibleExecuteMsg.sshPrivateSecretName = this.sshPrivateSecretName;
    return sAnsibleExecuteMsg;

  }
}
