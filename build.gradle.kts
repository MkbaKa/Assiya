import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    kotlin("jvm") version "2.4.0"
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    apply<JavaPlugin>()
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        maven("https://maven.aliyun.com/nexus/content/groups/public/")
    }

    dependencies {
        compileOnly(kotlin("stdlib"))
    }

    tasks {
        kotlin {
            jvmToolchain(21)
            compilerOptions {
                freeCompilerArgs.addAll(
                    "-Xintrinsic-const-evaluation"
                )
                jvmTarget.set(JvmTarget.JVM_1_8)
            }
        }

        withType<JavaCompile> {
            options.encoding = "UTF-8"
            sourceCompatibility = "1.8"
            targetCompatibility = "1.8"
            options.compilerArgs.add("-Xlint:-options")
        }
    }

}