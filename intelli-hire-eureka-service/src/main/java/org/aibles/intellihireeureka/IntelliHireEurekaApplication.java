package org.aibles.intellihireeureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class IntelliHireEurekaApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelliHireEurekaApplication.class, args);
    }
}
