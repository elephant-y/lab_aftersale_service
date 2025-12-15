package org.oomall.labservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@MapperScan("org.oomall.labservice.mapper")
public class LabServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LabServiceApplication.class, args);
    }

}
