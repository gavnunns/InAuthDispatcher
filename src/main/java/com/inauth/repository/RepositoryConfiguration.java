package com.inauth.repository;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by gavnunns on 6/27/16.
 */
@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = {"com.inauth.domain"})
@EnableJpaRepositories(basePackages = {"com.inauth.repository"})
@EnableTransactionManagement
public class RepositoryConfiguration {
}
