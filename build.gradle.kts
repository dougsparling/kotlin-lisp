plugins {
    kotlin("jvm") version "1.5.10"
}

group = "dev.cyberdeck"
version = "1.0"
val cotestVersion = "5.1.0"

repositories {
    mavenCentral()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation("io.kotest:kotest-runner-junit5:$cotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$cotestVersion")
    testImplementation("io.kotest:kotest-property:$cotestVersion")
}