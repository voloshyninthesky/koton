import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"
    application
	java
	id("io.ktor.plugin") version "2.2.2"
	id("com.google.cloud.tools.jib") version "2.1.0"

}

application {
	mainClass.set("com.koton.App")
}

ktor {
	fatJar {
		archiveFileName.set("koton.jar")
	}
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
	implementation("org.ton:ton-kotlin:0.2.4")
	implementation("io.javalin:javalin:4.6.4")
	implementation("tanukisoft:wrapper:3.2.3")
	implementation("org.slf4j:slf4j-simple:2.0.5")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
	implementation("org.apache.commons:commons-lang3:3.12.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

jib {
	to {
		image = "stujkee/koton"
	}
	from {
		image = "openjdk:11"
	}
	container {
		ports = listOf("8099")

		mainClass = "com.koton.App"
		jvmFlags = listOf(
			"--enable-preview",
			"-Dorg.slf4j.simpleLogger.showDateTime=true",
			"-Dorg.slf4j.simpleLogger.dateTimeFormat=MM/dd_HH:mm:ss,SSS",
			"-server",
			"-Djava.awt.headless=true",
			"-XX:InitialRAMFraction=2",
			"-XX:MinRAMFraction=2",
			"-XX:MaxRAMFraction=2",
			"-XX:+UseG1GC",
			"-XX:MaxGCPauseMillis=100",
			"-XX:+UseStringDeduplication"
		)
	}
}


