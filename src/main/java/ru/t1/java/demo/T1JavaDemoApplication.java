package ru.t1.java.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.t1.java.demo.feign.ClientFeign;

@SpringBootApplication
@EnableFeignClients(basePackageClasses = ClientFeign.class)
public class T1JavaDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(T1JavaDemoApplication.class, args);
    }

}
