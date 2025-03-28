package com.hfdy.hfdypan.utils;

import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.exception.BusinessException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author hf-dy
 * @date 2025/3/11 23:53
 */
@Component
@Slf4j
public class FileReqRespUtil {
    @Resource
    private MinIOUtil minIOUtil;

    /**
     * is限流写入输出流
     *
     * @param response
     * @param is
     * @param contentType
     * @param speed
     */
    public void writeStreamToResponse(HttpServletResponse response, InputStream is, String contentType, long speed) {
        try (OutputStream os = response.getOutputStream()) {
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            response.setContentType(contentType);

            byte[] buffer = new byte[8192]; // 使用固定大小的缓冲区，例如8KB
            int bytesRead;
            long startTime = System.currentTimeMillis();
            long totalBytesRead = 0;

            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
                os.flush(); // 确保数据被发送到客户端

                totalBytesRead += bytesRead;
                long elapsedTime = System.currentTimeMillis() - startTime;

                // 计算需要的总时间以保持目标速率
                long expectedElapsedTime = (long) ((totalBytesRead * 1000.0) / speed);

                // 如果当前经过的时间少于预期的经过时间，则休眠差值时间
                if (elapsedTime < expectedElapsedTime) {
                    Thread.sleep(Math.max(0, expectedElapsedTime - elapsedTime));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
    }


    public void writeStreamToResponse(HttpServletResponse response, String path, String contentType, long speed) {
        try {
            InputStream is = minIOUtil.getFileStream(path);
            writeStreamToResponse(response, is, contentType, speed);
        } catch (Exception e) {
            throw new BusinessException(HttpCodeEnum.FILE_GET_ERROR);
        }
    }
}
