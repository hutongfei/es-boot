package com.my;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EsBootApplication {

    public static void main(String[] args) {
        System.setProperty("hadoop.home.dir", "E:\\hadoop\\hadoop-2.7.7");
        SpringApplication.run(EsBootApplication.class, args);
    }

}
