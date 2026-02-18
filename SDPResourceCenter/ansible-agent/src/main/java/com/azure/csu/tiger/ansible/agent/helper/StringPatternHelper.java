package com.azure.csu.tiger.ansible.agent.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringPatternHelper {

    private static final Logger logger = LoggerFactory.getLogger(StringPatternHelper.class);

    public static String extractExtraVars(String input) {

        String extractedContent = "";
        Pattern pattern = Pattern.compile("\"(.*?)\"");

        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            extractedContent = matcher.group(1);
            logger.info("Extract extra variables from string: {}", extractedContent);
        }
        return extractedContent;
    }
}
