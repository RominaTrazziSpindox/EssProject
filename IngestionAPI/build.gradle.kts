plugins {
    id("java")
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.spx"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {

    // --- Spring ---
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    // --- Serialization ---
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // --- Database ---
    runtimeOnly("org.postgresql:postgresql")

    // --- Lombok ---
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // --- Testing ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

    testImplementation(platform("org.testcontainers:testcontainers-bom:1.19.7"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:rabbitmq")
    testImplementation("org.testcontainers:postgresql")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed", "standardError", "standardOut")
        showStandardStreams = true
    }
}