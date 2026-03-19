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

const val mapstructVersion = "1.6.0"
const val lombokMapstructBindingVersion = "0.2.0"

dependencies {

    // --- Spring ---
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-validation")


    // --- Mapping ---
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    // --- Database ---
    runtimeOnly("org.postgresql:postgresql")

    // --- Lombok ---
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:$lombokMapstructBindingVersion")

    // --- Testing ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation(platform("org.testcontainers:testcontainers-bom:1.19.7"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:rabbitmq")
    testImplementation("org.testcontainers:postgresql")

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed", "standardError", "standardOut")
        showStandardStreams = true
    }
}
