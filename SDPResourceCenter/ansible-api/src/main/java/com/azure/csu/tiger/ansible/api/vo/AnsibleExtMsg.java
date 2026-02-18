package com.azure.csu.tiger.ansible.api.vo;

import javax.validation.constraints.*;
import java.util.List;

public class AnsibleExtMsg {

        @NotBlank(message = "transactionId cannot be blank")
        private String transactionId;

        @NotNull(message = "can not be null")
        @NotEmpty(message = "can not be empty")
        private List<String> nodeList;

        @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9+.-]*://.*",message = "must be valid uri")
        private String playbookUri = "https://test.com";

        @NotBlank(message = "playbookType cannot be blank")
        private String playbookType;

        @NotNull(message = "can not be null")
        @NotEmpty(message = "must be valid url")
        private List<String> scriptFileUris;

        @NotBlank(message = "extraVars cannot be blank")
        private String extraVars;

        @NotBlank(message = "apiVersion cannot be blank")
        private String apiVersion;

        @NotBlank(message = "username cannot be blank")
        private String username;

        @Digits(message = "timeout cannot be blank", integer = 4, fraction = 0)
        private Integer timeout;

        public @NotBlank(message = "transactionId cannot be blank") String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(@NotBlank(message = "transactionId cannot be blank") String transactionId) {
            this.transactionId = transactionId;
        }

        public @NotNull(message = "can not be null") @NotEmpty(message = "can not be empty") List<String> getNodeList() {
            return nodeList;
        }

        public void setNodeList(@NotNull(message = "can not be null") @NotEmpty(message = "can not be empty") List<String> nodeList) {
            this.nodeList = nodeList;
        }

        public @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9+.-]*://.*") String getPlaybookUri() {
            return playbookUri;
        }

        public void setPlaybookUri(@Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9+.-]*://.*") String playbookUri) {
            this.playbookUri = playbookUri;
        }

        public @NotBlank(message = "playbookType cannot be blank") String getPlaybookType() {
            return playbookType;
        }

        public void setPlaybookType(@NotBlank(message = "playbookType cannot be blank") String playbookType) {
            this.playbookType = playbookType;
        }

        public @NotEmpty(message = "scriptFileUris cannot be blank") List<String> getScriptFileUris() {
            return scriptFileUris;
        }

        public void setScriptFileUris(@NotEmpty(message = "scriptFileUris cannot be blank") List<String> scriptFileUris) {
            this.scriptFileUris = scriptFileUris;
        }

        public @NotBlank(message = "extraVars cannot be blank") String getExtraVars() {
            return extraVars;
        }

        public void setExtraVars(@NotBlank(message = "extraVars cannot be blank") String extraVars) {
            this.extraVars = extraVars;
        }

        public @NotBlank(message = "apiVersion cannot be blank") String getApiVersion() {
            return apiVersion;
        }

        public void setApiVersion(@NotBlank(message = "apiVersion cannot be blank") String apiVersion) {
            this.apiVersion = apiVersion;
        }

        public @NotBlank(message = "username cannot be blank") String getUsername() {
            return username;
        }

        public void setUsername(@NotBlank(message = "username cannot be blank") String username) {
            this.username = username;
        }

        public @Digits(message = "timeout cannot be blank", integer = 4, fraction = 0) Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(@Digits(message = "timeout cannot be blank", integer = 4, fraction = 0) Integer timeout) {
            this.timeout = timeout;
        }
    }
