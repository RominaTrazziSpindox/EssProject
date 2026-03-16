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

    // Tomcat
    implementation ("org.springframework.boot:spring-boot-starter-web")

    // MapStruct
    implementation("org.mapstruct:mapstruct:1.6.0")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.0")

    // JPA (Hibernate)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Postgres driver
    implementation("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // RabbitMQ
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
    annotationProcessor("org.projectlombok:lombok")

    // Valid
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Testcontainers core
    testImplementation("org.testcontainers:junit-jupiter:1.19.7")

    // RabbitMQ container
    testImplementation("org.testcontainers:rabbitmq:1.19.7")

    // PostgreSQL container
    testImplementation("org.testcontainers:postgresql:1.19.7")
}

tasks.test {
    useJUnitPlatform()
}
