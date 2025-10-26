plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.11"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.github.kusitms_bugi"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}


repositories {
    mavenCentral()
}

dependencies {
    kotlin()
    web()
    database()
    security()
    documentation()
    other()
}

fun DependencyHandlerScope.kotlin() {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

fun DependencyHandlerScope.web() {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}

fun DependencyHandlerScope.database() {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
}

fun DependencyHandlerScope.security() {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
}

fun DependencyHandlerScope.documentation() {
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
}

fun DependencyHandlerScope.other() {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-mail")
}
