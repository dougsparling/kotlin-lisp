plugins {
    kotlin("jvm") version "1.5.10"
    id("application")
}

group = "dev.cyberdeck"
version = "1.0"


repositories {
    mavenCentral()
}

application {
    mainClass.set("dev.cyberdeck.lisp.RunnerKt")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

task("repl", type = JavaExec::class) {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("dev.cyberdeck.lisp.ReplKt")
    standardInput = System.`in`
}

dependencies {
    implementation(kotlin("stdlib"))

    val cotestVersion = "5.1.0"
    testImplementation("io.kotest:kotest-runner-junit5:$cotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$cotestVersion")
    testImplementation("io.kotest:kotest-property:$cotestVersion")
}