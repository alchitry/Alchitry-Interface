plugins {
    kotlin("jvm") version "2.4.21"
    kotlin("plugin.serialization") version "2.4.21"
    `maven-publish`
}

group = "com.alchitry"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("com.fazecast:jSerialComm:2.11.4")
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    implementation("io.github.dsheirer:usb4java:1.3.5")
    implementation("io.github.dsheirer:usb4java-native-libraries:1.3.3")
    implementation("me.tongfei:progressbar:0.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
}

kotlin {
    jvmToolchain(23)
}

tasks.test {
    isEnabled = false
    useJUnitPlatform()
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "com.alchitry"
            artifactId = "alchitry-interface"
            version = project.version.toString()
        }
    }
}