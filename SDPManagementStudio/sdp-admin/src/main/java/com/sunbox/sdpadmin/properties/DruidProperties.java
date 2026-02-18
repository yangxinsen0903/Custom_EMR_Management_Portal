package com.sunbox.sdpadmin.properties;

import com.sunbox.db.DruidConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.datasource")
public class DruidProperties extends DruidConfig {
}
