package com.example.tab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@MapperScan("com.example.tab.mapper")
@SpringBootApplication
public class TabApplication {

	public static void main(String[] args) {
		SpringApplication.run(TabApplication.class, args);
		//BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		//String password = passwordEncoder.encode("123456");

		//System.out.println("明文密码：123456");
		//System.out.println("BCrypt密码：" + password);
	}

}
