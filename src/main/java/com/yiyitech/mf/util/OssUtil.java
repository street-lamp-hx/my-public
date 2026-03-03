package com.yiyitech.mf.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.event.ProgressEventType;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import com.aliyun.oss.event.ProgressListener;
import com.aliyun.oss.event.ProgressEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName AdsReportDownloadUtil.java
 * @Description
 * @createTime 2023年12月27日 16:56:00
 */
@Slf4j
@Component
public class OssUtil {
    @Value("${oss.endpoint}")
    private String endpoint;
    @Value("${oss.access_key_id}")
    private String accessKeyId;
    @Value("${oss.access_key_secret}")
    private String accessKeySecret;
    @Value("${oss.bucket_name}")
    private String bucketName;
    @Value("${oss.folder}")
    private String folder;
    private OSS ossClient;
    private final Object lock = new Object();

    public void openOss() {
        if (ossClient != null) return;
        synchronized (lock) {
            if (ossClient == null) {
                // 初始化一次
                ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            }
        }
    }

    //关闭OSS客户端
    public void closeOss() {
        ossClient.shutdown();
    }

    //OSS获取
    public InputStream getObjectStream(String key) {
        OSSObject obj = ossClient.getObject(bucketName, key);
        return obj.getObjectContent(); // 记得用完关闭
    }

    //OSS删除
    public void deleteObject(String key) {
        ossClient.deleteObject(bucketName, key);
    }

    //普通文件并上传OSS
    public void downloadFileToOss(String fileName, String sourceUrl) throws Exception{
        try {
            URL url = new URL(sourceUrl);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            ossClient.putObject(bucketName, folder.concat(fileName), inputStream);
//            log.info(fileName + ": 下载上传oss success");
        } catch (Exception e) {
            log.info(fileName + ": 下载上传oss failure：" + e.getMessage());
            throw e;
        }
//        finally {
//            ossClient.shutdown();
//        }
    }

    //普通上传文件到oss并生成url
//    public void ossUpload(String fileName, String sourceUrl, File file) {
//        try {
//            ossClient.putObject(bucketName, gzFolder.concat(fileName), file);
//            Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000);
//            URL signedUrl = ossClient.generatePresignedUrl(bucketName, fileName, expiration);
//            log.info(fileName + ": ossUpload success");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    //大文件上传OSS
    public void downloadSpReportFileToOss(String fileName, String sourceUrl) throws Exception {
        final int CONNECT_TIMEOUT_MS = 30_000;           // 连接超时 30s
        final int READ_TIMEOUT_MS    = 20 * 60 * 1000;   // 读超时 20min（大文件）
        final int MAX_RETRIES        = 3;
        final long BACKOFF_BASE_MS   = 2_000L;           // 2s, 4s, 8s 退避

        String objectKey = "spreport/" + fileName;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            HttpURLConnection conn = null;
            InputStream in = null;
            try {
                URL url = new URL(sourceUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
                conn.setReadTimeout(READ_TIMEOUT_MS);
                conn.setRequestMethod("GET");
                conn.setUseCaches(false);
                // 和 URL 中的 response-content-encoding 保持一致，避免透明压缩
                conn.setRequestProperty("Accept-Encoding", "identity");

                int code = conn.getResponseCode(); // 先取状态码，避免卡在 getInputStream
                if (code / 100 != 2) {
                    throw new IOException("HTTP " + code + " when GET " + sourceUrl);
                }

                long contentLen = conn.getContentLengthLong(); // 可能为 -1
                in = new BufferedInputStream(conn.getInputStream(), 1 << 20); // 1MB 缓冲

                PutObjectRequest put = new PutObjectRequest(bucketName, objectKey, in);
                if (contentLen > 0) {
                    ObjectMetadata meta = new ObjectMetadata();
                    meta.setContentLength(contentLen); // 已知长度更稳
                    put.setMetadata(meta);
                }

                ossClient.putObject(put); // 成功返回即完整上传
                // 如不想任何成功日志，删除下一行即可
                log.info("OSS uploaded: bucket={}, key={}, size={}", bucketName, objectKey, contentLen);
                return; // 成功直接返回
            } catch (Exception e) {
                // 仅失败时警告；最后一次失败抛出给上层写 FAILED_DOWNLOAD
                if (attempt == MAX_RETRIES) {
                    log.warn("OSS upload failed after {} attempts, key={}, err={}",
                            attempt, objectKey, e.toString());
                    throw e;
                } else {
                    long sleep = BACKOFF_BASE_MS << (attempt - 1);
                    log.warn("OSS upload attempt {} failed, retrying in {} ms, key={}, err={}",
                            attempt, sleep, objectKey, e.toString());
                    try { Thread.sleep(sleep); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            } finally {
                if (in != null) try { in.close(); } catch (IOException ignore) {}
                if (conn != null) conn.disconnect();
            }
        }
    }

    //大文件上传OSS（可查看进度，调试时用）下边这一整大块
    public void downloadSpReportFileToOssView(String fileName, String sourceUrl) throws Exception {
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            URL url = new URL(sourceUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(30_000);          // 连接超时
            conn.setReadTimeout(20 * 60 * 1000);     // 读超时（大文件建议足够大）
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setRequestProperty("Accept-Encoding", "identity"); // 和 URL 参数一致

            int code = conn.getResponseCode();       // 先拿状态码，避免卡在 getInputStream
            if (code / 100 != 2) {
                throw new IOException("HTTP " + code + " when GET " + sourceUrl);
            }

            long total = conn.getContentLengthLong(); // 可能是 -1（未知）
            in = new BufferedInputStream(conn.getInputStream(), 1 << 20); // 1MB 缓冲

            String objectKey = "spreport/" + fileName;

            PutObjectRequest req = new PutObjectRequest(bucketName, objectKey, in);
            // 如果拿到了 Content-Length，塞到元信息里，便于计算百分比
            if (total > 0) {
                ObjectMetadata meta = new ObjectMetadata();
                meta.setContentLength(total);
                // 可选：meta.setContentType("application/json");  // 看你文件类型
                req.setMetadata(meta);
            }

            // 进度监听：每 10MB 打一次日志（你也可以改成 5MB/50MB 等）
            req.setProgressListener(new OssProgressLogger(fileName, total, 10L * 1024 * 1024));

            ossClient.putObject(req);
            log.info("OSS putObject ok: bucket={}, key={}, size={} bytes", bucketName, objectKey, total);
        } catch (Exception e) {
            log.warn(fileName + ": 下载上传 OSS 失败: " + e.getMessage(), e);
            throw e;
        } finally {
            if (in != null) try { in.close(); } catch (IOException ignore) {}
            if (conn != null) conn.disconnect();
        }
    }
    /** 进度监听器：按固定字节步长（stepBytes）打印进度；若 total 已知则附带百分比与速度 */
    private static class OssProgressLogger implements ProgressListener {
        private static final Logger LOGGER = LoggerFactory.getLogger(OssProgressLogger.class);
        private final String fileName;
        private final long totalBytes;   // -1 表示未知
        private final long stepBytes;
        private long uploaded = 0L;
        private long nextMark = 0L;
        private final long start = System.currentTimeMillis();

        OssProgressLogger(String fileName, long totalBytes, long stepBytes) {
            this.fileName = fileName;
            this.totalBytes = totalBytes;
            this.stepBytes = stepBytes > 0 ? stepBytes : 5L * 1024 * 1024;
            this.nextMark = this.stepBytes;
        }

        private static double toMB(long bytes) { return bytes / 1024.0 / 1024.0; }
        private static String fmt(double v) { return String.format("%.2f", v); }

        @Override
        public void progressChanged(ProgressEvent e) {
            ProgressEventType type = e.getEventType();
            switch (type) {
                case TRANSFER_STARTED_EVENT: {
                    if (totalBytes > 0) {
                        LOGGER.info("{}: 上传开始，总大小 {} MB",
                                fileName, fmt(toMB(totalBytes)));
                    } else {
                        LOGGER.info("{}: 上传开始，总大小未知", fileName);
                    }
                    break;
                }
                case REQUEST_BYTE_TRANSFER_EVENT: {
                    uploaded += e.getBytes();
                    boolean hitStep = uploaded >= nextMark;
                    boolean finishedKnown = (totalBytes > 0 && uploaded == totalBytes);
                    if (hitStep || finishedKnown) {
                        double mb = toMB(uploaded);
                        double seconds = Math.max(0.001, (System.currentTimeMillis() - start) / 1000.0);
                        double speed = mb / seconds; // MB/s
                        if (totalBytes > 0) {
                            LOGGER.info("{}: 已上传 {}/{} MB ({}%), 速度 {} MB/s",
                                    fileName,
                                    fmt(mb),
                                    fmt(toMB(totalBytes)),
                                    fmt(uploaded * 100.0 / totalBytes),
                                    fmt(speed));
                        } else {
                            LOGGER.info("{}: 已上传 {} MB, 速度 {} MB/s",
                                    fileName, fmt(mb), fmt(speed));
                        }
                        nextMark += stepBytes;
                    }
                    break;
                }
                case TRANSFER_COMPLETED_EVENT: {
                    double mbAll = toMB(uploaded);
                    double secAll = Math.max(0.001, (System.currentTimeMillis() - start) / 1000.0);
                    LOGGER.info("{}: 上传完成，累计 {} MB，用时 {} s，平均 {} MB/s",
                            fileName, fmt(mbAll), fmt(secAll), fmt(mbAll / secAll));
                    break;
                }
                case TRANSFER_FAILED_EVENT: {
                    LOGGER.warn("{}: 上传失败，已发送 {} MB",
                            fileName, fmt(toMB(uploaded)));
                    break;
                }
                default:
                    break;
            }
        }
    }


    public long getObjectSize(String key) {
        try {
            // key 要与你上传时的一致，比如 "spreport/ba-search-terms_...json.gz"
            ObjectMetadata md = ossClient.getObjectMetadata(bucketName, key);
            return md.getContentLength(); // 字节数
        } catch (OSSException e) {
            // 常见：NoSuchKey 等
            log.warn("getObjectSize: key={} errorCode={} message={}", key, e.getErrorCode(), e.getMessage());
            return -1L;
        } catch (Exception e) {
            log.warn("getObjectSize: key={} ex={}", key, e.getMessage(), e);
            return -1L;
        }
    }

    /**
     * 上传 JSON 字节到 OSS
     *
     * @param objectKey OSS 上的完整 key，比如 "spreport/browse_tree_ATVPDKIKX0DER_xxx.json"
     * @param data      JSON 内容字节
     */
    public void uploadJsonBytes(String objectKey, byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data is null");
        }
        // 确保 client 已初始化
        openOss();

        try (InputStream in = new java.io.ByteArrayInputStream(data)) {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(data.length);
            meta.setContentType("application/json");

            ossClient.putObject(bucketName, objectKey, in, meta);
            log.info("uploadJsonBytes ok, bucket={}, key={}, size={}", bucketName, objectKey, data.length);
        } catch (Exception e) {
            log.error("uploadJsonBytes failed, bucket={}, key={}, ex={}", bucketName, objectKey, e.getMessage(), e);
            throw new RuntimeException("upload json to oss failed, key=" + objectKey, e);
        }
    }

}
