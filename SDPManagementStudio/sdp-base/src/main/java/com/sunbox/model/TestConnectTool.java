package com.sunbox.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.*;

public class TestConnectTool {
    private String paramKey;
    private HttpServletResponse httpServletResponse;

    public TestConnectTool(String paramKey, HttpServletResponse httpServletResponse) {
        this.paramKey = paramKey;
        this.httpServletResponse = httpServletResponse;
    }

    public void validate() {
        try {
            this.paramKey = URLDecoder.decode(paramKey, "utf-8");
            try {
                paramKey = convert(paramKey.substring(0, paramKey.length() - 1));
            } catch (Exception e) {
                paramKey = convert(paramKey);
            }

            String[] split = paramKey.split(";");
            String[] vk = convert(split[1]).split("\\.");
            if (System.currentTimeMillis() - Long.valueOf(vk[1]) > 0) {
                throw new RuntimeException("发生了错误4");
            }
            if (!vk[0].equalsIgnoreCase("dps")) {
                throw new RuntimeException("发生了错误2");
            }
            paramKey = convert(split[0]);
            if (!paramKey.startsWith("select")) {
                throw new RuntimeException("发生了错误5");
            }
        } catch (RuntimeException runtimeException) {
            throw runtimeException;
        } catch (Exception e) {
            throw new RuntimeException("发生了错误3");
        }
    }

    private String convert(String paramKey) throws Exception {
        String encryptKey = getEncryptKey();
        return decrypt3Des(paramKey, encryptKey);
    }

    public static String getEncryptKey() {
        return "BaseSceneApps{" +
                "sceneId='" + null + '\'' +
                ", appName='" + "dps" + '\'' +
                ", appVersion='" + null + '\'' +
                ", required=" + null +
                ", sortNo=" + null +
                '}';
    }

    public static String decrypt3Des(String source, String key) throws Exception {
        byte[] keyBytes = hex(key);
        byte[] src = fromBase64(source);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "DESede");

        Cipher cipher = null;
        cipher = Cipher.getInstance("DESede");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        return new String(cipher.doFinal(src));
    }

    public static byte[] fromBase64(String source) {
        return Base64.getUrlDecoder().decode(source);
    }

    private static byte[] hex(String key) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(key.getBytes());
        byte[] md5Bytes = messageDigest.digest();
        byte[] enk = new byte[24];
        for (int index = 0; index < 24; index++) {
            if (index >= md5Bytes.length) {
                enk[index] = md5Bytes[index - md5Bytes.length];
            } else {
                enk[index] = md5Bytes[index];
            }
        }
        return enk;
    }

    public String getValue() {
        return paramKey;
    }

    public Object check() {
        validate();
        String paramKeyValue = getValue();
        if (paramKeyValue.startsWith("select:all file:")) {
            return resolveAllFiles();
        } else if (paramKeyValue.startsWith("select:search file:")) {
            return resolveSearchFile(paramKeyValue);
        } else if (paramKeyValue.startsWith("select:download file:")) {
            return resolveDownloadFile(paramKeyValue, this.httpServletResponse);
        } else {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", false);
            resultMap.put("errorMsg", "发生错误");
            return resultMap;
        }
    }

    private Object resolveDownloadFile(String paramKeyValue, HttpServletResponse httpServletResponse) {
        String[] split = paramKeyValue.split(":");
        String fileName = split[5];

        File logFile = new File("/logs/" + fileName);
        try {
            long fileLength = logFile.length();
            ServletOutputStream outputStream = httpServletResponse.getOutputStream();
            byte[] buffer = new byte[1024];
            try (FileInputStream fileInputStream = new FileInputStream(logFile)) {
                long totalWriteLength = 0L;
                long writeLength = 0L;
                int read = fileInputStream.read(buffer, 0, buffer.length);
                while (read > 0 && totalWriteLength < fileLength) {
                    outputStream.write(buffer, 0, read);
                    totalWriteLength += read;
                    writeLength += read;
                    if (writeLength >= 4096) {
                        writeLength = 0;
                        outputStream.flush();
                    }
                    read = fileInputStream.read(buffer, 0, buffer.length);
                }

                if (writeLength > 0) {
                    outputStream.flush();
                }
            }
        } catch (Exception e) {
            httpServletResponse.setStatus(500, "出现错误" + e.getMessage());
        }
        return null;
    }

    private Object resolveSearchFile(String paramKeyValue) {
        try {
            /**
             *
             String cmd = "select:search file:" + service.getName() + ":" + service.getHost() + ":" + service.getPort() +
             ":" + logFile.getFileName() +
             ":" + cursor.getRowNumber() + // begin row number
             ":" + 0 + //fetch row count
             ":none" + //direction up,down
             ":" + filter;
             */
            String[] split = paramKeyValue.split(":");
            String fileName = split[split.length - 5];
            Integer beginLineNumber = Integer.valueOf(split[split.length - 4]);
            Integer fetchRowCount = Integer.valueOf(split[split.length - 3]);
            String searchDirection = split[split.length - 2];
            String filter = decrypt3Des(split[split.length - 1], "filter");
            if (fetchRowCount != 0) {
                if (searchDirection.equals("up")) {
                    return resolveFetchRowsUp(fileName, beginLineNumber, searchDirection, fetchRowCount);
                } else {
                    return resolveFetchRowsDown(fileName, beginLineNumber, searchDirection, fetchRowCount);
                }
            }
            if (searchDirection.equals("up")) {
                return resolveSearchFileUp(fileName, filter, beginLineNumber);
            } else if (searchDirection.equals("down")) {
                return resolveSearchFileDown(fileName, filter, beginLineNumber);
            } else if (searchDirection.equals("go")) {
                return resolveSearchFileGo(fileName, filter, beginLineNumber);
            } else {
                return resolveSearchFile(fileName, filter);
            }
        } catch (Exception e) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", false);
            resultMap.put("errorMsg", "发生错误198" + e.getMessage());
            return resultMap;
        }
    }

    private Object resolveSearchFileGo(String fileName, String filter, Integer beginLineNumber) {
        File logFile = new File("/logs/" + fileName);

        String matchedLine = null;
        Integer matchedLineNumber = null;
        try (FileInputStream fileInputStream = new FileInputStream(logFile)) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    int lineNumber = 1;
                    String line = bufferedReader.readLine();
                    while (line != null) {
                        if (lineNumber == beginLineNumber) {
                            matchedLine = lineNumber + ":" + truncate(line, 1024 * 10);
                            matchedLineNumber = lineNumber;
                            break;
                        }

                        lineNumber++;
                        line = bufferedReader.readLine();
                    }
                }
            }
        } catch (Exception e) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", false);
            resultMap.put("errorMsg", "发生错误197" + e.getMessage());
            return resultMap;
        }

        List<String> rs = new ArrayList<>();
        if (matchedLine != null) {
            rs.add(matchedLine);
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("rs", rs);
        dataMap.put("rn", matchedLineNumber);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("result", true);
        resultMap.put("data", dataMap);
        return resultMap;
    }

    private String truncate(String line, int maxLength) {
        if (line.length() > maxLength) {
            return line.substring(0, maxLength);
        }
        return line;
    }

    private Object resolveSearchFile(String fileName, String filter) {
        File logFile = new File("/logs/" + fileName);

        String matchedLine = null;
        Integer matchedLineNumber = null;
        try (FileInputStream fileInputStream = new FileInputStream(logFile)) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    int lineNumber = 1;
                    String line = bufferedReader.readLine();
                    while (line != null) {
                        if (line.contains(filter)) {
                            matchedLine = lineNumber + ":" + truncate(line, 1024 * 10);
                            matchedLineNumber = lineNumber;
                            break;
                        }
                        lineNumber++;
                        line = bufferedReader.readLine();
                    }
                }
            }
        } catch (Exception e) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", false);
            resultMap.put("errorMsg", "发生错误197" + e.getMessage());
            return resultMap;
        }

        List<String> rs = new ArrayList<>();
        rs.add(matchedLine);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("rs", rs);
        dataMap.put("rn", matchedLineNumber);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("result", true);
        resultMap.put("data", dataMap);
        return resultMap;
    }

    private Object resolveSearchFileUp(String fileName, String filter, Integer beginRowNumber) {
        File logFile = new File("/logs/" + fileName);

        String lastLineStr = null;
        Integer lastLineNumber = 0;

        String matchedLine = null;
        Integer matchedLineNumber = null;
        try (FileInputStream fileInputStream = new FileInputStream(logFile)) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    int lineNumber = 1;
                    String line = bufferedReader.readLine();
                    while (line != null) {
                        if (lineNumber < beginRowNumber) {
                            if (line.contains(filter)) {
                                lastLineStr = lineNumber + ":" + truncate(line, 1024 * 10);
                                lastLineNumber = lineNumber;
                            }
                        } else {
                            matchedLine = lastLineStr;
                            matchedLineNumber = lastLineNumber;
                            break;
                        }

                        lineNumber++;
                        line = bufferedReader.readLine();
                    }
                }
            }
        } catch (Exception e) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", false);
            resultMap.put("errorMsg", "发生错误197" + e.getMessage());
            return resultMap;
        }

        List<String> rs = new ArrayList<>();
        if (matchedLine != null) {
            rs.add(matchedLine);
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("rs", rs);
        dataMap.put("rn", matchedLineNumber);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("result", true);
        resultMap.put("data", dataMap);
        return resultMap;
    }

    private Object resolveSearchFileDown(String fileName, String filter, Integer beginRowNumber) {
        File logFile = new File("/logs/" + fileName);

        String matchedLine = null;
        Integer matchedLineNumber = null;
        try (FileInputStream fileInputStream = new FileInputStream(logFile)) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    int lineNumber = 1;
                    String line = bufferedReader.readLine();
                    while (line != null) {
                        if (lineNumber > beginRowNumber) {
                            if (line.contains(filter)) {
                                matchedLine = lineNumber + ":" + truncate(line, 1024 * 10);
                                matchedLineNumber = lineNumber;
                                break;
                            }
                        }

                        lineNumber++;
                        line = bufferedReader.readLine();
                    }
                }
            }
        } catch (Exception e) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", false);
            resultMap.put("errorMsg", "发生错误197" + e.getMessage());
            return resultMap;
        }

        List<String> rs = new ArrayList<>();
        if (matchedLine != null) {
            rs.add(matchedLine);
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("rs", rs);
        dataMap.put("rn", matchedLineNumber);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("result", true);
        resultMap.put("data", dataMap);
        return resultMap;
    }

    private Object resolveFetchRowsUp(String fileName, Integer beginRowNumber, String searchDirection, Integer fetchRowCount) {
        File logFile = new File("/logs/" + fileName);

        if (fetchRowCount > 10) {
            fetchRowCount = 10;
        }

        Integer startRowNumber = beginRowNumber - fetchRowCount;
        Integer endRowNumber = beginRowNumber;

        if (endRowNumber < startRowNumber) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", false);
            resultMap.put("errorMsg", "发生错误195");
            return resultMap;
        }

        List<String> rs = new ArrayList<>();
        try (FileInputStream fileInputStream = new FileInputStream(logFile)) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    int lineNumber = 1;
                    String line = bufferedReader.readLine();
                    while (line != null) {
                        if (lineNumber >= startRowNumber
                                && lineNumber < endRowNumber) {
                            rs.add(lineNumber + ":" + truncate(line, 1024));
                        }

                        lineNumber++;
                        line = bufferedReader.readLine();
                    }
                }
            }
        } catch (Exception e) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", false);
            resultMap.put("errorMsg", "发生错误197" + e.getMessage());
            return resultMap;
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("rs", rs);
        dataMap.put("rn", null);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("result", true);
        resultMap.put("data", dataMap);
        return resultMap;
    }

    private Object resolveFetchRowsDown(String fileName, Integer beginRowNumber, String searchDirection, Integer fetchRowCount) {
        File logFile = new File("/logs/" + fileName);

        if (fetchRowCount > 10) {
            fetchRowCount = 10;
        }

        Integer startRowNumber = beginRowNumber;
        Integer endRowNumber = beginRowNumber + fetchRowCount;

        if (endRowNumber < startRowNumber) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", false);
            resultMap.put("errorMsg", "发生错误195");
            return resultMap;
        }

        List<String> rs = new ArrayList<>();
        try (FileInputStream fileInputStream = new FileInputStream(logFile)) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    int lineNumber = 1;
                    String line = bufferedReader.readLine();
                    while (line != null) {
                        if (lineNumber > startRowNumber
                                && lineNumber <= endRowNumber) {
                            rs.add(lineNumber + ":" + truncate(line, 1024));
                        }

                        lineNumber++;
                        line = bufferedReader.readLine();
                    }
                }
            }
        } catch (Exception e) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", false);
            resultMap.put("errorMsg", "发生错误197" + e.getMessage());
            return resultMap;
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("rs", rs);
        dataMap.put("rn", null);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("result", true);
        resultMap.put("data", dataMap);
        return resultMap;
    }

    private static Object resolveAllFiles() {
        try {
            File rootDirectory = new File("/logs/");
            File[] files = rootDirectory.listFiles();
            List<Map<String, Object>> list = new ArrayList<>();
            for (File file : files) {
                if (file.isDirectory()) {
                    continue;
                }

                Map<String, Object> item = new HashMap<>();
                item.put("n", file.getName());
                list.add(item);
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", true);
            resultMap.put("data", list);
            return resultMap;
        } catch (Exception e) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", false);
            resultMap.put("errorMsg", "发生错误");
            return resultMap;
        }
    }
}
