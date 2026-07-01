package com.smartquote;

import com.smartquote.repository.TestPingRepository;
import com.smartquote.model.TestPing;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendSpringbootApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendSpringbootApplication.class, args);
	}

//	@Bean
//	CommandLineRunner testMongoConnection(TestPingRepository repo) {
//		return args -> {
//			repo.save(new TestPing("MongoDB connected successfully!"));
//			System.out.println("Test document saved to MongoDB Atlas");
//		};
//	}
}
