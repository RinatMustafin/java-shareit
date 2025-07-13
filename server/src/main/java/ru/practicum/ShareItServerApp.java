package ru.practicum;

/**
 * Hello world!
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.practicum.shareit")
public class ShareItServerApp {
    public static void main(String[] args) {
        SpringApplication.run(ShareItServerApp.class, args);
    }
}
