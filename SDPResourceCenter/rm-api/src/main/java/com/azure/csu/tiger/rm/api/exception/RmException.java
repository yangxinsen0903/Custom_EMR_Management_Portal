package com.azure.csu.tiger.rm.api.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class RmException extends RuntimeException {

    private HttpStatus status;

    public RmException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
