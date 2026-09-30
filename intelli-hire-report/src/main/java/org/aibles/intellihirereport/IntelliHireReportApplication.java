package org.aibles.intellihirereport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class IntelliHireReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelliHireReportApplication.class, args);
    }
}
