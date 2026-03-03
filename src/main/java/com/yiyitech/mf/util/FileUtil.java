package com.yiyitech.mf.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName FileUtil.java
 * @Description
 * @createTime 2023年12月28日 15:23:00
 */
@Slf4j
@Component
public class FileUtil {

    //下载文件
    public void downloadFile(String sourceUrl, String path) throws IOException {
        URL url = new URL(sourceUrl);
        try (InputStream input = url.openStream();
             OutputStream out = new FileOutputStream(path)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    //解压文件
    public void decompressGzipFile(String gzFilePath, String outputFilePath) throws IOException {
        try (FileInputStream fInput = new FileInputStream(gzFilePath);
             GZIPInputStream gInput = new GZIPInputStream(fInput);
             FileOutputStream fOut = new FileOutputStream(outputFilePath);
             BufferedOutputStream dest = new BufferedOutputStream(fOut)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gInput.read(buffer)) != -1) {
                dest.write(buffer, 0, bytesRead);
            }
        }
    }

    //删除文件
    public void deleteFile(String gzipFilePath, String jsonFilePath) {
        boolean deleteGz = new File(gzipFilePath).delete();
        boolean deleteJson = new File(jsonFilePath).delete();
    }

}
