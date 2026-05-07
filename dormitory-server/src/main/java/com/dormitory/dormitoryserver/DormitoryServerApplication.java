package com.dormitory.dormitoryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DormitoryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DormitoryServerApplication.class, args);
    }

}
