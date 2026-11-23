plugins {
    id("com.gradleup.shadow") version "8.3.5"
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly(project(":platform:bukkit"))
    compileOnly(project(":common"))
    compileOnly(fileTree("libs"))
}

val isDev = rootProject.version.toString().endsWith("dev")

tasks {
    processResources {
        val properties = mapOf(
            "version" to rootProject.version.toString(),
            "name" to "AttributePlus",
        )
        inputs.properties(properties)
        filesMatching("plugin.yml") {
            expand(properties)
        }
    }

    shadowJar {
        archiveBaseName.set("Assiya-AttributePlus")
        archiveClassifier.set("")

        if (!isDev) {
            relocate("kotlin.", "${rootProject.group}.libs.kotlin.")
        }

        exclude("org/intellij/lang/annotations/**")
        exclude("org/jetbrains/annotations/**")
        exclude("org/jspecify/annotations/**")
        exclude("org/checkerframework/**")

        providers.gradleProperty("assiyaOutputDir").orNull?.let {
            destinationDirectory.set(file(it))
        }
    }
}
