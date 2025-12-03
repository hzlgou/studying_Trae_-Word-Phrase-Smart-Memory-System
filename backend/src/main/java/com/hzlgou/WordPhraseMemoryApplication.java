package com.hzlgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WordPhraseMemoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(WordPhraseMemoryApplication.class, args);
    }
}