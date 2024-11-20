package ru.t1.java.demo.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;

@TestConfiguration
@EnableJpaRepositories
public class LiquibaseConfig {

    @Bean
    public SpringLiquibase springLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDefaultSchema("public");
        liquibase.setLiquibaseSchema("public");
        liquibase.setDropFirst(true);
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.xml");
        liquibase.setTestRollbackOnUpdate(true);
        return liquibase;
    }
}
