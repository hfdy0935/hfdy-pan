package com.hfdy.hfdypan.service.impl;

import com.hfdy.hfdypan.service.ResourceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * @author hf-dy
 * @date 2025/3/5 16:27
 */
@Service
public class ResourceServiceImpl implements ResourceService {
    @Value("${server.deploy_address}")
    private String deployAddress;


    @Override
    public String addPrefix(String filename) {
        return deployAddress + "/api/resource/" + filename;
    }
}
