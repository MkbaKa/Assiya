plugins {
    `java-library`
    `maven-publish`
}

val isJitPack = providers.environmentVariable("JITPACK").orNull == "true"
group = if (isJitPack) providers.environmentVariable("GROUP").get() else rootProject.group
version = if (isJitPack) providers.environmentVariable("VERSION").get() else rootProject.version

val includedProjects = listOf(project(":common"), project(":platform:bukkit"))
for (includedProject in includedProjects) {
    evaluationDependsOn(includedProject.path)
}

val includedMainSources = includedProjects.map {
    it.extensions.getByType<SourceSetContainer>().named("main")
}

dependencies {
    api(kotlin("stdlib"))
    for (includedProject in includedProjects) {
        for (configurationName in listOf("api", "implementation")) {
            val configuration = includedProject.configurations.findByName(configurationName) ?: continue
            for (dependency in configuration.dependencies.withType<ExternalModuleDependency>()) {
                add("api", dependency.copy())
            }
        }
        for (dependency in includedProject.configurations.getByName("compileOnly")
            .dependencies.withType<ExternalModuleDependency>()) {
            add("compileOnlyApi", dependency.copy())
        }
    }
}

java {
    withSourcesJar()
}

tasks.jar {
    archiveBaseName.set("assiya-bukkit")
    duplicatesStrategy = DuplicatesStrategy.FAIL
    for (mainSources in includedMainSources) {
        from(mainSources.map { it.output.classesDirs })
    }
}

tasks.named<Jar>("sourcesJar") {
    archiveBaseName.set("assiya-bukkit")
    duplicatesStrategy = DuplicatesStrategy.FAIL
    for (mainSources in includedMainSources) {
        from(mainSources.map { it.allSource })
    }
}

publishing {
    publications {
        create<MavenPublication>("assiyaBukkit") {
            artifactId = if (isJitPack) {
                providers.environmentVariable("ARTIFACT").get()
            } else {
                "assiya-bukkit"
            }
            from(components["java"])
            pom {
                name.set("Assiya Bukkit")
                description.set("Assiya Bukkit and common library")
            }
        }
    }
    repositories {
        providers.gradleProperty("assiyaMavenUrl").orNull?.let { repositoryUrl ->
            maven {
                name = "assiya"
                url = uri(repositoryUrl)
                credentials {
                    username = providers.environmentVariable("ASSIYA_MAVEN_USERNAME").orNull
                    password = providers.environmentVariable("ASSIYA_MAVEN_PASSWORD").orNull
                }
            }
        }
    }
}
