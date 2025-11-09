package com.lockmanager.config;

import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Configuration
// ✅ Load this config only if spring.data.redis.url property is defined and not empty
@ConditionalOnProperty(name = "spring.data.redis.url")
public class RedisExactUriConfig {

    @Value("${spring.data.redis.url}")
    private String redisUrl;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        if (redisUrl == null || redisUrl.isBlank()) {
            System.err.println("⚠️ spring.data.redis.url is empty; falling back to default localhost:6379");
            return new LettuceConnectionFactory("localhost", 6379);
        }

        // Parse the redis:// or rediss:// URI
        RedisURI redisUri = RedisURI.create(redisUrl);

        RedisStandaloneConfiguration cfg = new RedisStandaloneConfiguration();
        cfg.setHostName(redisUri.getHost());
        cfg.setPort(redisUri.getPort());

        if (redisUri.getUsername() != null && !redisUri.getUsername().isEmpty()) {
            cfg.setUsername(redisUri.getUsername());
        }

        if (redisUri.getPassword() != null && redisUri.getPassword().length > 0) {
            cfg.setPassword(RedisPassword.of(new String(redisUri.getPassword())));
        }

        // Configure Lettuce client (enable SSL for rediss://)
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
                LettuceClientConfiguration.builder()
                        .commandTimeout(Duration.ofSeconds(3))
                        .shutdownTimeout(Duration.ofMillis(100));

        if (redisUri.isSsl()) {
            builder.useSsl();
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(cfg, builder.build());
        factory.afterPropertiesSet();

        System.out.printf("✅ Redis connected: host=%s port=%d ssl=%s%n",
                redisUri.getHost(), redisUri.getPort(), redisUri.isSsl());

        return factory;
    }
}
