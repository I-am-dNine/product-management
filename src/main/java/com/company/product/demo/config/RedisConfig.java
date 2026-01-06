package com.company.product.demo.config;

import com.company.product.demo.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Product> productRedisTemplate(
            RedisConnectionFactory factory) {

        RedisTemplate<String, Product> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // key: String
        template.setKeySerializer(new StringRedisSerializer());

        // value: JSON with JavaTimeModule
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // Use Jackson2JsonRedisSerializer for type safety and clean JSON
        Jackson2JsonRedisSerializer<Product> serializer = new Jackson2JsonRedisSerializer<>(mapper, Product.class);

        template.setValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
