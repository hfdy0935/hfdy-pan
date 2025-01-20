package com.hfdy.hfdypan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement // 事务
@EnableScheduling // 定时任务
@EnableAsync // 异步调用
@MapperScan("com.hfdy.hfdypan.mapper")
@EnableConfigurationProperties
public class HfdyPanApplication {
    public static void main(String[] args) {
        SpringApplication.run(HfdyPanApplication.class, args);
    }
}
