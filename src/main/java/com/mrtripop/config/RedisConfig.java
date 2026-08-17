package com.mrtripop.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.util.Map;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);
  private static final Duration REFERENCE_DATA_TTL = Duration.ofHours(4);
  private static final Duration SESSION_DATA_TTL = Duration.ofMinutes(15);

  @Bean
  public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    GenericJackson2JsonRedisSerializer serializer = redisSerializer();
    StringRedisSerializer keySerializer = new StringRedisSerializer();

    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(DEFAULT_TTL)
        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

    RedisCacheConfiguration referenceDataConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(REFERENCE_DATA_TTL)
        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

    RedisCacheConfiguration sessionDataConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(SESSION_DATA_TTL)
        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

    Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
        "molecules", referenceDataConfig,
        "brands", referenceDataConfig,
        "stores", referenceDataConfig,
        "storeProducts", referenceDataConfig,
        "authTokens", sessionDataConfig,
        "mfaSessions", sessionDataConfig
    );

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(defaultConfig)
        .withInitialCacheConfigurations(cacheConfigurations)
        .build();
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    GenericJackson2JsonRedisSerializer serializer = redisSerializer();

    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(serializer);
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(serializer);
    return template;
  }

  private GenericJackson2JsonRedisSerializer redisSerializer() {
    PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
        .allowIfBaseType(Object.class)
        .allowIfSubType("com.mrtripop.")
        .allowIfSubType("java.")
        .build();

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    return new GenericJackson2JsonRedisSerializer(mapper);
  }
}