package com.example.hotel_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableTransactionManagement
@SpringBootApplication
public class HotelBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelBackApplication.class, args);
	}

}
