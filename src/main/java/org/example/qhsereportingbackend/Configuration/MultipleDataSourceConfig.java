package org.example.qhsereportingbackend.Configuration;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class MultipleDataSourceConfig {

    @Bean(name = "taskModifiedDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.taskmodified")
    public DataSource taskModifiedDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "taskModifiedJdbcTemplate")
    public JdbcTemplate taskModifiedJdbcTemplate(@Qualifier("taskModifiedDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean(name = "taskCreatedDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.taskcreated")
    public DataSource taskCreatedDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "taskCreatedJdbcTemplate")
    public JdbcTemplate taskCreatedJdbcTemplate(@Qualifier("taskCreatedDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean(name = "formsDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.forms")
    public DataSource formsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "formsJdbcTemplate")
    public JdbcTemplate formsJdbcTemplate(@Qualifier("formsDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}
