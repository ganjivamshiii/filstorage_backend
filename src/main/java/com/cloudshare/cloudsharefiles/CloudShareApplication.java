package com.cloudshare.cloudsharefiles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
@EnableMongoAuditing
@SpringBootApplication
public class CloudShareApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudShareApplication.class, args);
    }
}
