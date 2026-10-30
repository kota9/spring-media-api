package com.example.springstudy.transaction;

import javax.sql.DataSource;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@TestConfiguration(proxyBeanMethods = false)
public class TransactionPropagationTestConfig {

    @Bean
    DataSourceInitializer transactionPropagationSchemaInitializer(DataSource transactionLabDataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("transaction-propagation-schema.sql"));
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(transactionLabDataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    @Bean
    PropagationInnerService propagationInnerService(JdbcTemplate transactionLabJdbcTemplate) {
        return new PropagationInnerService(transactionLabJdbcTemplate);
    }

    @Bean
    PropagationOuterService propagationOuterService(
            JdbcTemplate transactionLabJdbcTemplate,
            PropagationInnerService propagationInnerService) {
        return new PropagationOuterService(transactionLabJdbcTemplate, propagationInnerService);
    }
}
