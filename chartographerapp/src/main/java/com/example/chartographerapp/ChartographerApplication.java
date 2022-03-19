package com.example.chartographerapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class ChartographerApplication {

	public static String contentPath;

	public static void main(String[] args) throws IOException {

		contentPath = args[0];
		Path path = Path.of(contentPath);
		if (!Files.exists(path)) {
			Files.createDirectories(path);
		}

		SpringApplication.run(ChartographerApplication.class, args);
	}

}
