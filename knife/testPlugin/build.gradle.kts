import groovyjarjarantlr.Tool.version

plugins {
    id("groovy")
    id("kotlin")
    id("java-gradle-plugin")
    id("maven-publish")
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    implementation("com.android.tools.build:gradle-api:8.1.4")
    implementation("com.android.tools.build:gradle:8.1.4")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")

    implementation("org.ow2.asm:asm:9.6")
    implementation("org.ow2.asm:asm-tree:9.6")
}

group = "luyao.plugin.testPlugin"
version = "0.0.1"

gradlePlugin {
    plugins {
        create("testPlugin") {
            id = "testPlugin"
            implementationClass = "luyao.plugin.test.TestPlugin"
            displayName = "ASM Gradle Plugin"
            description = "ASM Gradle Plugin"
            tags.set(setOf("AGP", "ASM"))
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withJavadocJar()
    withSourcesJar()
}
