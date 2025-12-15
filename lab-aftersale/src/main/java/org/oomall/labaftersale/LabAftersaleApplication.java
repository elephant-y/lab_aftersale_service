package org.oomall.labaftersale;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@MapperScan("org.oomall.labaftersale.mapper")
public class LabAftersaleApplication {

    public static void main(String[] args) {
        SpringApplication.run(LabAftersaleApplication.class, args);
    }

}
