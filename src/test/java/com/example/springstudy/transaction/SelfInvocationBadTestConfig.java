package com.example.springstudy.transaction;

import javax.sql.DataSource;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@TestConfiguration(proxyBeanMethods = false)
public class SelfInvocationBadTestConfig {

    @Bean
    DataSourceInitializer selfInvocationSchemaInitializer(DataSource transactionLabDataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("transaction-self-invocation-schema.sql"));
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(transactionLabDataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
