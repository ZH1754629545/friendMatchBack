package com.example.demo.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @className: RedissonConfig
 * @author: ZH
 * @date: 2024/12/1 16:32
 * @Version: 1.0
 * @description:
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Slf4j
public class RedissonConfig {
    private String host;
    private String port;
    @Bean
    public RedissonClient redissonClient() {
        // 1. 创建配置
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(3);
        // 2. 创建实例
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
