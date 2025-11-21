package com.github.kusitms_bugi.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@EnableRedisRepositories(
    basePackages = ["com.github.kusitms_bugi.domain"],
    includeFilters = [ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = [".*RedisRepository"]
    )]
)
class RedisConfig {

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory, jacksonConfig: JacksonConfig): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = GenericJackson2JsonRedisSerializer(jacksonConfig.objectMapper())
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = GenericJackson2JsonRedisSerializer(jacksonConfig.objectMapper())
        }
    }
}