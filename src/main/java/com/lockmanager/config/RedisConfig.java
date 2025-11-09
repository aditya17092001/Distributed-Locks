package com.lockmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;


@Configuration
public class RedisConfig {

	
	@Bean
	public LettuceConnectionFactory redisConnectionFactory() {
		// Uses spring.data.redis.* properties automatically
		return new LettuceConnectionFactory();
	}

	@Bean
	public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}
}
