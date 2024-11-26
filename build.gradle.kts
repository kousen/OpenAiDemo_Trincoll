plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.kousenit"
version = "1.0-SNAPSHOT"

javafx {
    version = "21"
    modules = listOf("javafx.controls")
}

application {
    mainClass.set("edu.trincoll.ImageCarousel")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven("https://clojars.org/repo/")
}

dependencies {
    // JavaFX
    implementation("org.openjfx:javafx-controls:21.0.2")

    // Gson parser
    implementation("com.google.code.gson:gson:2.11.0")

    // Langchain4j
    implementation("dev.langchain4j:langchain4j-open-ai:0.36.2")
    implementation("dev.langchain4j:langchain4j-anthropic:0.36.2")
    implementation("dev.langchain4j:langchain4j-mistral-ai:0.36.2")
    implementation("dev.langchain4j:langchain4j-google-ai-gemini:0.36.2")
    implementation("dev.langchain4j:langchain4j-ollama:0.36.2")
    implementation("dev.langchain4j:langchain4j:0.36.2")

    // Jsoup
    implementation("org.jsoup:jsoup:1.18.1")

    // Audio
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("ch.qos.logback:logback-classic:1.5.3")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}