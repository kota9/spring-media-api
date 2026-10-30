package com.example.springstudy.transaction;

import javax.sql.DataSource;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@TestConfiguration(proxyBeanMethods = false)
@EnableTransactionManagement
public class TransactionLabTestConfig {

    @Bean
    DataSource transactionLabDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:transaction-lab;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    @Bean
    JdbcTemplate transactionLabJdbcTemplate(DataSource transactionLabDataSource) {
        return new JdbcTemplate(transactionLabDataSource);
    }

    @Bean
    PlatformTransactionManager transactionLabTransactionManager(DataSource transactionLabDataSource) {
        return new DataSourceTransactionManager(transactionLabDataSource);
    }

    @Bean
    DataSourceInitializer transactionLabSchemaInitializer(DataSource transactionLabDataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("transaction-schema.sql"));
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(transactionLabDataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    @Bean
    TransactionPracticeService transactionPracticeService(JdbcTemplate transactionLabJdbcTemplate) {
        return new TransactionPracticeService(transactionLabJdbcTemplate);
    }
}
