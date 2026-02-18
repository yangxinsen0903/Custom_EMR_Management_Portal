package com.azure.csu.tiger.ansible.agent.helper;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;

public class FileHelper {

    private final static Logger logger = LoggerFactory.getLogger(FileHelper.class);

    public static String extractFileName(String url) {
        URL urlObj = null;
        try {
            urlObj = new URL(url);
        } catch (MalformedURLException e) {
            logger.error("Extract file name fail: "+e.getMessage());
            throw new RuntimeException(e);
        }
        String path = urlObj.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public static void ChownFile(String fileFullPath) {

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("chmod", "700", fileFullPath);
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            logger.info("Chmod file " + fileFullPath + " exit code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return folder in filepath
     */
    public static String getFolder(String filepath) {
        String result = filepath;
        result = result.replaceAll("\\\\", "/");
        if (filepath.contains("/")) {
            result = filepath.substring(0, filepath.lastIndexOf("/"));
        }
        return result;
    }


    public static boolean createFile(String fullfilepath, String content, boolean replace) {
        if (content == null || content.length() == 0) {
            logger.error("create file " + fullfilepath + ":no content at all");
            return false;
        }
        //create folder if necessary
        String folderPath = getFolder(fullfilepath);
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(fullfilepath);
        boolean writeContent = false;
        if (!file.exists()) {
            try {
                file.createNewFile();
                writeContent = true;
            } catch (IOException e) {
                logger.error("create file " + fullfilepath + " error", e);
                return false;
            }
        } else {
            if (replace) {
                writeContent = true;
            }
        }
        if (!writeContent) {
            return true;
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            if (content != null) {
                fileOutputStream.write(content.getBytes("UTF-8"));
            }
            fileOutputStream.close();
        } catch (Exception e) {
            logger.error("create file " + fullfilepath + " error ", e);
            return false;
        } finally {
            IOUtils.closeQuietly(fileOutputStream);
        }
        return true;
    }

    public static boolean createFile(String fullfilepath, ByteArrayOutputStream outputStream, boolean replace) {
        byte[] content = outputStream.toByteArray();
        if (content.length == 0) {
            logger.error("create file " + fullfilepath + ":no content at all");
            return false;
        }
        //create folder if necessary
        String folderPath = getFolder(fullfilepath);
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(fullfilepath);
        boolean writeContent = false;
        if (!file.exists()) {
            try {
                file.createNewFile();
                writeContent = true;
            } catch (IOException e) {
                logger.error("create file " + fullfilepath + " error", e);
                return false;
            }
        } else {
            if (replace) {
                writeContent = true;
            }
        }
        if (!writeContent) {
            return true;
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            if (content != null) {
                fileOutputStream.write(content);
            }
            fileOutputStream.close();
        } catch (Exception e) {
            logger.error("create file " + fullfilepath + " error ", e);
            return false;
        } finally {
            IOUtils.closeQuietly(fileOutputStream);
        }
        return true;
    }


    public static void deleteFile(String fullfilepath) {

        Path path = Paths.get(fullfilepath);
        try {
            Files.delete(path);
            logger.info("File deleted successfully.");
        } catch (NoSuchFileException e) {
            logger.error("File does not exist.");
        } catch (DirectoryNotEmptyException e) {
            logger.error("Directory is not empty.");
        } catch (IOException e) {
            logger.error("IO error occurred.");
        }
    }
}
