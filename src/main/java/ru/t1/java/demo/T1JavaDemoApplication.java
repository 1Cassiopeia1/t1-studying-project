package ru.t1.java.demo;

import com.example.t1projectspringbootstarter.feign.ClientFeign;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients(basePackageClasses = ClientFeign.class)
@EnableJpaRepositories(basePackages = {"ru.t1.java.demo"})
public class T1JavaDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(T1JavaDemoApplication.class, args);
    }

}
