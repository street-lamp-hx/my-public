package com.yiyitech.mf.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.event.ProgressEvent;
import com.aliyun.oss.event.ProgressEventType;
import com.aliyun.oss.event.ProgressListener;
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
/**
 * @author hx
 * @version 1.0.0
 * @ClassName OssUtilTest.java
 * @Description
 * @createTime 2025年09月26日 11:15:00
 */
@Slf4j
@Component
public class OssUtilTest {
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

    private volatile OSS ossClient;
    private final Object lock = new Object();

    /** 初始化 OSS 客户端（带超时/重试/连接池参数） */
    public void openOss() {
        if (ossClient != null) return;
        synchronized (lock) {
            if (ossClient == null) {
                ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
                // ——网络相关（关键：socketTimeout 是“单次读阻塞”的超时）——
                conf.setConnectionTimeout(30_000);     // 连接超时 30s
                conf.setSocketTimeout(120_000);        // 读超时 120s（链路慢也能容忍，但不会无限挂）
                conf.setRequestTimeout(600_000);       // 单请求上限 10min
                conf.setMaxErrorRetry(2);              // SDK 自动重试 2 次
                // ——连接池/回收——
                conf.setMaxConnections(64);
                conf.setIdleConnectionTime(10_000);    // 10s 回收空闲连接
                // 其他默认即可
                ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret, conf);
                log.info("OSS client initialized with timeouts: conn=30s, sock=120s, req=10min, retry=2");
            }
        }
    }

    /** 关闭 OSS 客户端 */
    public void closeOss() {
        try {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        } catch (Exception ignore) {
        } finally {
            ossClient = null;
        }
    }

    /**
     * 下载对象为流（带重试 + 缓冲）
     * 注意：使用方负责关闭返回的 InputStream
     */
    public InputStream getObjectStream(String key) {
        ensureOpened();
        final int MAX_RETRIES = 3;
        final long BACKOFF_BASE_MS = 2_000L; // 2s, 4s, 8s

        com.aliyun.oss.OSSException lastOss = null;
        com.aliyun.oss.ClientException lastClient = null;
        RuntimeException lastRt = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                OSSObject obj = ossClient.getObject(bucketName, key);
                ObjectMetadata md = obj.getObjectMetadata();
                long len = md != null ? md.getContentLength() : -1L;
                if (len > 0) {
                    log.info("getObjectStream: key={}, contentLength={} bytes", key, len);
                } else {
                    log.info("getObjectStream: key={}, contentLength=unknown", key);
                }
                // 用 BufferedInputStream 包一层，减少底层 read 次数（对弱网更稳）
                return new BufferedInputStream(obj.getObjectContent(), 256 * 1024);
            } catch (com.aliyun.oss.OSSException e) {
                lastOss = e;
                if (attempt == MAX_RETRIES) break;
                long sleep = BACKOFF_BASE_MS << (attempt - 1);
                log.warn("getObjectStream attempt {} failed (OSSException: {}), retry in {} ms, key={}",
                        attempt, e.getErrorCode(), sleep, key);
                sleepQuiet(sleep);
            } catch (com.aliyun.oss.ClientException e) {
                lastClient = e;
                if (attempt == MAX_RETRIES) break;
                long sleep = BACKOFF_BASE_MS << (attempt - 1);
                log.warn("getObjectStream attempt {} failed (ClientException: {}), retry in {} ms, key={}",
                        attempt, e.getMessage(), sleep, key);
                sleepQuiet(sleep);
            } catch (RuntimeException e) {
                lastRt = e;
                if (attempt == MAX_RETRIES) break;
                long sleep = BACKOFF_BASE_MS << (attempt - 1);
                log.warn("getObjectStream attempt {} failed (RuntimeException: {}), retry in {} ms, key={}",
                        attempt, e.getMessage(), sleep, key);
                sleepQuiet(sleep);
            }
        }
        if (lastOss != null) throw lastOss;
        if (lastClient != null) throw lastClient;
        throw new RuntimeException("getObjectStream failed: " + (lastRt != null ? lastRt.getMessage() : "unknown"));
    }

    /** OSS 删除 */
    public void deleteObject(String key) {
        ensureOpened();
        ossClient.deleteObject(bucketName, key);
    }

    /** 普通下载并上传到 OSS（保留原逻辑） */
    public void downloadFileToOss(String fileName, String sourceUrl) throws Exception {
        ensureOpened();
        try {
            URL url = new URL(sourceUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(30_000);
            conn.setReadTimeout(120_000);
            conn.setRequestProperty("Accept-Encoding", "identity");
            int code = conn.getResponseCode();
            if (code / 100 != 2) throw new IOException("HTTP " + code + " when GET " + sourceUrl);
            try (InputStream inputStream = new BufferedInputStream(conn.getInputStream(), 1 << 20)) {
                ossClient.putObject(bucketName, folder.concat(fileName), inputStream);
            }
        } catch (Exception e) {
            log.info(fileName + ": 下载上传oss failure：" + e.getMessage());
            throw e;
        }
    }

    /** 大文件上传到 OSS（保留你的稳定版） */
    public void downloadSpReportFileToOss(String fileName, String sourceUrl) throws Exception {
        ensureOpened();
        final int CONNECT_TIMEOUT_MS = 30_000;
        final int READ_TIMEOUT_MS    = 20 * 60 * 1000;
        final int MAX_RETRIES        = 3;
        final long BACKOFF_BASE_MS   = 2_000L;

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
                conn.setRequestProperty("Accept-Encoding", "identity");

                int code = conn.getResponseCode();
                if (code / 100 != 2) throw new IOException("HTTP " + code + " when GET " + sourceUrl);

                long contentLen = conn.getContentLengthLong();
                in = new BufferedInputStream(conn.getInputStream(), 1 << 20);

                PutObjectRequest put = new PutObjectRequest(bucketName, objectKey, in);
                if (contentLen > 0) {
                    ObjectMetadata meta = new ObjectMetadata();
                    meta.setContentLength(contentLen);
                    put.setMetadata(meta);
                }
                ossClient.putObject(put);
                log.info("OSS uploaded: bucket={}, key={}, size={}", bucketName, objectKey, contentLen);
                return;
            } catch (Exception e) {
                if (attempt == MAX_RETRIES) {
                    log.warn("OSS upload failed after {} attempts, key={}, err={}", attempt, objectKey, e.toString());
                    throw e;
                } else {
                    long sleep = BACKOFF_BASE_MS << (attempt - 1);
                    log.warn("OSS upload attempt {} failed, retrying in {} ms, key={}, err={}",
                            attempt, sleep, objectKey, e.toString());
                    sleepQuiet(sleep);
                }
            } finally {
                if (in != null) try { in.close(); } catch (IOException ignore) {}
                if (conn != null) conn.disconnect();
            }
        }
    }

    /** 带进度日志的大文件上传（保留原逻辑） */
    public void downloadSpReportFileToOssView(String fileName, String sourceUrl) throws Exception {
        ensureOpened();
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            URL url = new URL(sourceUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(30_000);
            conn.setReadTimeout(20 * 60 * 1000);
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setRequestProperty("Accept-Encoding", "identity");

            int code = conn.getResponseCode();
            if (code / 100 != 2) throw new IOException("HTTP " + code + " when GET " + sourceUrl);

            long total = conn.getContentLengthLong();
            in = new BufferedInputStream(conn.getInputStream(), 1 << 20);

            String objectKey = "spreport/" + fileName;
            PutObjectRequest req = new PutObjectRequest(bucketName, objectKey, in);
            if (total > 0) {
                ObjectMetadata meta = new ObjectMetadata();
                meta.setContentLength(total);
                req.setMetadata(meta);
            }
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

    /** 获取对象大小（保留原逻辑） */
    public long getObjectSize(String key) {
        ensureOpened();
        try {
            ObjectMetadata md = ossClient.getObjectMetadata(bucketName, key);
            return md.getContentLength();
        } catch (OSSException e) {
            log.warn("getObjectSize: key={} errorCode={} message={}", key, e.getErrorCode(), e.getMessage());
            return -1L;
        } catch (Exception e) {
            log.warn("getObjectSize: key={} ex={}", key, e.getMessage(), e);
            return -1L;
        }
    }

    // ===== 私有工具 =====
    private void ensureOpened() {
        if (ossClient == null) {
            throw new IllegalStateException("OSS client not initialized. Call openOss() first.");
        }
    }
    private static void sleepQuiet(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    /** 进度监听器（保留原逻辑） */
    private static class OssProgressLogger implements ProgressListener {
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

        @Override public void progressChanged(ProgressEvent e) {
            ProgressEventType type = e.getEventType();
            switch (type) {
                case TRANSFER_STARTED_EVENT:
                    if (totalBytes > 0) {
                        log.info("{}: 上传开始，总大小 {} MB", fileName, fmt(toMB(totalBytes)));
                    } else {
                        log.info("{}: 上传开始，总大小未知", fileName);
                    }
                    break;
                case REQUEST_BYTE_TRANSFER_EVENT:
                    uploaded += e.getBytes();
                    boolean hitStep = uploaded >= nextMark;
                    boolean finishedKnown = (totalBytes > 0 && uploaded == totalBytes);
                    if (hitStep || finishedKnown) {
                        double mb = toMB(uploaded);
                        double seconds = Math.max(0.001, (System.currentTimeMillis() - start) / 1000.0);
                        double speed = mb / seconds; // MB/s
                        if (totalBytes > 0) {
                            log.info("{}: 已上传 {}/{} MB ({}%), 速度 {} MB/s",
                                    fileName, fmt(mb), fmt(toMB(totalBytes)),
                                    fmt(uploaded * 100.0 / totalBytes), fmt(speed));
                        } else {
                            log.info("{}: 已上传 {} MB, 速度 {} MB/s", fileName, fmt(mb), fmt(speed));
                        }
                        nextMark += stepBytes;
                    }
                    break;
                case TRANSFER_COMPLETED_EVENT: {
                    double mbAll = toMB(uploaded);
                    double secAll = Math.max(0.001, (System.currentTimeMillis() - start) / 1000.0);
                    log.info("{}: 上传完成，累计 {} MB，用时 {} s，平均 {} MB/s",
                            fileName, fmt(mbAll), fmt(secAll), fmt(mbAll / secAll));
                    break;
                }
                case TRANSFER_FAILED_EVENT:
                    log.warn("{}: 上传失败，已发送 {} MB", fileName, fmt(toMB(uploaded)));
                    break;
                default:
                    break;
            }
        }
    }
}
