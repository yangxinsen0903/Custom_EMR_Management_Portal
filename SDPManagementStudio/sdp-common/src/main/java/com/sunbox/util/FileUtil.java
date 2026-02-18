package com.sunbox.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;



/**
 * Created by DiaoWen 0n 2021/9/27
 * 文件下载，压缩包下载
 */
public class FileUtil {
    /**
     * @Description: 把某个文件路径下面的文件包含文件夹压缩到一个文件下
     * @param: file
     * @param: rootPath 相对地址
     * @param: zipoutputStream
     * @author: DiaoWen
     * @date: 2021/9/27
     */
    public static void zipFileFun(File file,String rootPath,ZipOutputStream zipoutputStream){
        if(file.exists()){
            if(file.isFile()){
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                try {
                    String relativeFilePath = file.getPath().replace(rootPath+File.separator, "");
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis,10*1024);
                    ZipEntry zipEntry;
                    if(!relativeFilePath.contains("\\")){
                        zipEntry = new ZipEntry(file.getName());
                    }else{
                        zipEntry = new ZipEntry(relativeFilePath);
                    }
                    zipoutputStream.putNextEntry(zipEntry);
                    //开始写文件
                    byte[] b = new byte[10*1024];
                    int size = 0;
                    while((size=bis.read(b,0,10*1024)) != -1){
                        zipoutputStream.write(b,0,size);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    try {
                        if(bis != null){
                            bis.close();
                        }
                        if(fis != null){
                            fis.close();
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
    }

    /*
     * 获取某个文件夹下的所有文件
     */
    public static Vector<File> getPathAllFiles(File file,Vector<File> vector){
        if(file.isFile()){
            vector.add(file);
        }else{
            File[] files = file.listFiles();
            for(File f : files){
                if(f.isDirectory()){
                    getPathAllFiles(f,vector);
                }else{
                    vector.add(f);
                }
            }
        }
        return vector;
    }

    /**
     * @Description:  压缩文件到指定文件夹
     * @Param: ourceFilePath 源地址
     * @Param: destinFilePath 目的地址
     * @author: DiaoWen
     * @date: 2021/9/27
     */
    public static String zipFiles(String sourceFilePath,String destinFilePath){
        File sourceFile = new File(sourceFilePath);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = dateFormat.format(new Date())+".zip";
        String zipFilePath = destinFilePath+File.separator+fileName;
        try {
            File zipFile = new File(zipFilePath);
            Vector<File> vector = FileUtil.getPathAllFiles(sourceFile, new Vector<>());
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(new BufferedOutputStream(fos));
            byte[] bufs = new byte[1024 * 10];
            for (int i = 0; i < vector.size(); i++) {
                ZipEntry zipEntry = new ZipEntry(vector.get(i).getName());
                zos.putNextEntry(zipEntry);
                fis = new FileInputStream(vector.get(i));
                bis = new BufferedInputStream(fis, 1024 * 10);
                int read = 0;
                while ((read = bis.read(bufs, 0, 1024 * 10)) != -1) {
                    zos.write(bufs, 0, read);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            // 关闭流
            try {
                if (null != bis)
                    bis.close();
                if (null != zos)
                    zos.closeEntry();
                zos.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return fileName;
    }

    /**
     * @Description: zip打包下载指定文件夹下的所有文件，支持树型结构含文件夹
     * @Param: filePath:文件路径 如：C:\Users\Lenovo\Desktop\demoFile
     * @Param: filenName:文件名不含后缀 如：testZip
     * @author: DiaoWen
     * @date: 2021/9/27
     */
    public static void zipDownloadRelativePath(HttpServletResponse response, String filePath, String filenName) {
        Vector<File> fileVector = new Vector<File>();
        File file = new File(filePath);
        File [] subFile = file.listFiles();
        for(int i = 0; i<subFile.length; i++){
            if(!subFile[i].isDirectory()){
                fileVector.add(subFile[i]);
            }else{
                Vector vector = FileUtil.getPathAllFiles(subFile[i],new Vector<>());
                fileVector.addAll(vector);
            }
        }
        String fileName = filenName+".zip";
        try {
            response.reset();
            response.setHeader("Content-disposition", "attachment;filename=" + new String(fileName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        OutputStream out = null;
        BufferedInputStream bis = null;
        ZipOutputStream zos = null;
        String zipFilePath = filePath+File.separator+fileName;
        File zipFile = new File(zipFilePath);
        try {
            if(!zipFile.exists()){
                zipFile.createNewFile();
            }
            out = response.getOutputStream();
            zos = new ZipOutputStream(new FileOutputStream(zipFile));
            for(int i = 0;i< fileVector.size();i++){
                zipFileFun(fileVector.get(i),filePath,zos);
            }
            if(zos != null){
                zos.closeEntry();
                zos.close();
            }
            byte[] bt = new byte[10*1024];
            int size = 0;
            bis = new BufferedInputStream(new FileInputStream(zipFilePath),10*1024);
            while((size=bis.read(bt,0,10*1024)) != -1){
                out.write(bt,0,size);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //关闭相关流
            try {
                if(bis != null){
                    bis.close();
                }
                if(out != null){
                    out.close();
                }
                if(zipFile.exists()){
                    zipFile.delete();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    /**
     * @Description: 下载Excel文件到指定路径，如果为临时文件可通过返回文件路径操作临时文件及文件架
     * @param: valueKeys 如："OrderDate","OilName"
     * @param: titleKeys 如："订单日期","油品名称"
     * @param: fileTitle 文件名  如：测试Excel表1
     * @param: fileType 文件类型（xls、xlsx）
     * @param: excelContentList 数据列表
     * @param: path 文件路径 如：C:\Users\Lenovo\Desktop\demoFile
     * @Return:  文件路径\文件名.后缀名   path\fileTitle.fileType 如：C:\Users\Lenovo\Desktop\demoFile\测试Excel表1.xls
     * @author: DiaoWen
     * @date: 2021/9/27
     */
    public static <T> String downLoadExcel(String[] valueKeys, String[] titleKeys, String fileTitle, String fileType,
                                       List<T> excelContentList, String path){
        FileOutputStream os = null;
        HSSFWorkbook wbook = null;
        SXSSFWorkbook xwbook = null;
        try {
            String fileName = "";
            //excel名称
            if(fileType.equals("xls")){
                fileName = fileTitle + ".xls";
                wbook = new HSSFWorkbook();
                HSSFSheet wsheet = wbook.createSheet(fileTitle);
                HSSFCellStyle cellStyle = wbook.createCellStyle();
                cellStyle.setWrapText(true);
                wsheet.setDefaultColumnWidth(35);
                HSSFFont font =  wbook.createFont();
                font.setFontHeightInPoints((short) 12);
                font.setBold(true);
                cellStyle.setFont(font);
                int rowIndex = 0;
                Row row = wsheet.createRow(rowIndex++);
                for (int m = 0; m < titleKeys.length; m++) {
                    Cell cell = row.createCell((short) m);
                    cell.setCellStyle(cellStyle);
                    row.createCell(m).setCellValue(titleKeys[m] == null ? "" : titleKeys[m]);
                }
                for (int i = 0; i < excelContentList.size(); i++) {
                    row = wsheet.createRow(rowIndex++);
                    JSONObject map = JSONObject.parseObject(JSON.toJSONString(excelContentList.get(i), SerializerFeature.WriteDateUseDateFormat));
                    for (int k = 0; k < valueKeys.length; k++) {
                        row.createCell(k).setCellValue(StringUtils.isBlank(ObjectUtils.toString(map.get(valueKeys[k]), ""))?"":map.get(valueKeys[k]).toString());
                    }
                }
                path = path+ File.separator+fileName;
                os = new FileOutputStream(path);
                wbook.write(os);
                os.flush();
                wbook.close();
            }
            if(fileType.equals("xlsx")){
                fileName = fileTitle + ".xlsx";
                xwbook = new SXSSFWorkbook(1000);
                Sheet wsheet = xwbook.createSheet(fileName);
                XSSFCellStyle xssfCellStyle = (XSSFCellStyle) xwbook.createCellStyle();
                Font font = xwbook.createFont();
                font.setFontHeightInPoints((short) 12);
                font.setFontHeight((short) 20);
                xssfCellStyle.setFont(font);
                xssfCellStyle.setWrapText(false);
                xssfCellStyle.setAlignment(HorizontalAlignment.CENTER);
                xssfCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                int rowIndex = 0;
                Row row = wsheet.createRow(rowIndex++);
                for (int m = 0; m < titleKeys.length; m++) {
                    Cell cell = row.createCell((short) m);
                    cell.setCellStyle(xssfCellStyle);
                    row.createCell(m).setCellValue(titleKeys[m] == null ? "" : titleKeys[m]);
                }
                //设置行数据
                for (int i = 0; i < excelContentList.size(); i++) {
                    row = wsheet.createRow(rowIndex++);
                    JSONObject map = JSONObject.parseObject(JSON.toJSONString(excelContentList.get(i), SerializerFeature.WriteDateUseDateFormat));
                    for (int k = 0; k < valueKeys.length; k++) {
                        row.createCell(k).setCellValue(StringUtils.isBlank(ObjectUtils.toString(map.get(valueKeys[k]), ""))?"":map.get(valueKeys[k]).toString());
                    }
                }
                path = path+ File.separator+fileName;
                os = new FileOutputStream(path);
                xwbook.write(os);
                os.flush();
                xwbook.close();
            }
            os.close();
        }catch (Exception e){
            if(null!=os){
                try {
                    os.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            if(null!=wbook){
                try {
                    wbook.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            if(null!=xwbook){
                try {
                    xwbook.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }finally{
            if(null!=os){
                try {
                    os.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            if(null!=wbook){
                try {
                    wbook.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            if(null!=xwbook){
                try {
                    xwbook.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
        return path;
    }
}