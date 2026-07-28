package com.smit.compliq.config;

import javax.sql.DataSource;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseConfig {

    @Value("${app.datasource.postgres.url}")
    private String postgresUrl;

    @Value("${app.datasource.postgres.username}")
    private String postgresUsername;

    @Value("${app.datasource.postgres.password}")
    private String postgresPassword;

    @Value("${spring.ai.vectorstore.pgvector.dimensions:768}")
    private int dimensions;

    @Bean
    public DataSource postgresDataSource() {
        return DataSourceBuilder.create()
                .url(postgresUrl)
                .username(postgresUsername)
                .password(postgresPassword)
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    @Bean
    public JdbcTemplate postgresJdbcTemplate(DataSource postgresDataSource) {
        return new JdbcTemplate(postgresDataSource);
    }

    @Bean
    public VectorStore vectorStore(JdbcTemplate postgresJdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(postgresJdbcTemplate, embeddingModel)
                .dimensions(dimensions)
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .initializeSchema(true)
                .build();
    }
}
