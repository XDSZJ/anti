package com.hicore.antiapollo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author https://blog.csdn.net/qq_38704184
 * @version 1.0
 * @package com.hicore.antiapollo.config
 * @date 2020/1/16 16:19
 */
@Configuration
public class UserConfig {
    @Value("${username:zhangsan}")
    private String username;
}
