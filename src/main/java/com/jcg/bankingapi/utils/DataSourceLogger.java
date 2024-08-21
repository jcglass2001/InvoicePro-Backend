package com.jcg.bankingapi.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DataSourceLogger {
    @Value("${spring.datasource.url}")
    private String dataSourceUrl;

    @PostConstruct
    public void logDataSourceUrl(){
        System.out.println("Datasource URL: " + dataSourceUrl);
    }
}
