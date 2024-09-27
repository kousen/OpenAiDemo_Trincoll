plugins {
    id("java")
}

group = "com.kousenit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Gson parser
    implementation("com.google.code.gson:gson:2.11.0")

    // Langchain4j
    implementation("dev.langchain4j:langchain4j-open-ai:0.34.0")
    implementation("dev.langchain4j:langchain4j-anthropic:0.34.0")
    implementation("dev.langchain4j:langchain4j:0.34.0")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("ch.qos.logback:logback-classic:1.5.3")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}