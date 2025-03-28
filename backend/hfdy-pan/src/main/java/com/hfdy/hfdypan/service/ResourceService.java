package com.hfdy.hfdypan.service;

/**
 * @author hf-dy
 * @date 2025/3/5 16:27
 */
public interface ResourceService {

    /**
     * 给资源添加前缀，变成能访问的url，目前有avatar和lyric
     *
     * @param filename
     * @return
     */
    String addPrefix(String filename);
}
