package com.example.tab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@MapperScan("com.example.tab.mapper")
@SpringBootApplication
public class TabApplication {

	public static void main(String[] args) {
		SpringApplication.run(TabApplication.class, args);
	}

}
