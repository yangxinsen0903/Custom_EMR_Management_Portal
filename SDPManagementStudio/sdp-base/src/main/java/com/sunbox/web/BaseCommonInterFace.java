package com.sunbox.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface BaseCommonInterFace {
    default Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }
}
