package org.aibles.intellihirequestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class IntelliHireQuestionApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelliHireQuestionApplication.class, args);
    }
}
