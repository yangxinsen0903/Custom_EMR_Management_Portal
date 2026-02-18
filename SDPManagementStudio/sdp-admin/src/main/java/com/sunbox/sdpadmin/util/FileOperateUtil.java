package com.sunbox.sdpadmin.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 2015/4/23.
 */
public class FileOperateUtil {

    static final Log LOG = LogFactory.getLog(FileOperateUtil.class.getName());

    public static void download(HttpServletRequest request, HttpServletResponse response, String storeName, String realName) throws Exception {

        LOG.info("download excel begin: " + realName);

        try {
           /* Resource resource = new ClassPathResource("/conf/system.properties");
            Properties props = PropertiesLoaderUtils.loadProperties(resource)*/;
//            String filepath = props.getProperty("downloadfilepath");
            //String filepath = PropertiesUtil.props.getProperty("DownloadFilePath");
            String filepath = "/usr/local/data/";
            response.setContentType("text/html;charset=UTF-8");
            request.setCharacterEncoding("UTF-8");
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;

            String downLoadPath = filepath + storeName;

            long fileLength = new File(downLoadPath).length();

            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", "attachment; filename="
                    + new String(realName.getBytes("utf-8"), "ISO8859-1"));
            response.setHeader("Content-Length", String.valueOf(fileLength));

            bis = new BufferedInputStream(new FileInputStream(downLoadPath));
            bos = new BufferedOutputStream(response.getOutputStream());
            byte[] buff = new byte[2048];
            int bytesRead;
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
            bis.close();
            bos.close();

        } catch (Exception ex) {
            LOG.error(ex);
            throw ex;
        }

        LOG.info("download excel finish: " + realName);
    }

  public static List<String> imgext=new ArrayList<String>();
    static
    {
        imgext.add(".png");imgext.add(".jpg");imgext.add(".jpeg");imgext.add(".gif");

    }

    /**
     * 是否允许的图片后缀名
     * @param ext
     * @return
     */
    public static boolean IsAllowImgExt(String ext)
    {
       return imgext.contains(ext.toLowerCase());
    }
}
