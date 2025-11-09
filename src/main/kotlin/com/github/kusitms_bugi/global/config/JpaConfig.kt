package com.github.kusitms_bugi.global.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
    basePackages = ["com.github.kusitms_bugi.domain"],
    includeFilters = [ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = [".*JpaRepository"]
    )]
)
class JpaConfig