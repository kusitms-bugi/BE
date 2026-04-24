package com.github.kusitms_bugi.global.config

import io.lettuce.core.ClientOptions
import io.lettuce.core.SocketOptions
import io.lettuce.core.TimeoutOptions
import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableRedisRepositories(
    basePackages = ["com.github.kusitms_bugi.domain"],
    includeFilters = [ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = [".*RedisRepository"]
    )]
)
class RedisConfig {

    @Value("\${spring.data.redis.host}")
    private lateinit var host: String

    @Value("\${spring.data.redis.port}")
    private var port: Int = 6379

    @Value("\${spring.data.redis.password:}")
    private var password: String = ""

    @Value("\${spring.data.redis.ssl.enabled:false}")
    private var sslEnabled: Boolean = false

    @Bean
    fun lettuceConnectionFactory(): LettuceConnectionFactory {
        val redisConfig = RedisStandaloneConfiguration(host, port).apply {
            if (this@RedisConfig.password.isNotBlank()) {
                setPassword(this@RedisConfig.password)
            }
        }

        val socketOptions = SocketOptions.builder()
            .keepAlive(true)
            .connectTimeout(Duration.ofSeconds(10))
            .build()

        val clientOptions = ClientOptions.builder()
            .socketOptions(socketOptions)
            .autoReconnect(true)
            .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
            .timeoutOptions(TimeoutOptions.enabled(Duration.ofSeconds(5)))
            .build()

        val clientConfigBuilder = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofSeconds(5))
            .clientOptions(clientOptions)

        if (sslEnabled) {
            clientConfigBuilder.useSsl()
        }

        return LettuceConnectionFactory(redisConfig, clientConfigBuilder.build())
    }

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