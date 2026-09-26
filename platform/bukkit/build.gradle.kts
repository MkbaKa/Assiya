plugins {
    id("com.google.devtools.ksp") version "2.3.6"
    id("com.gradleup.shadow") version "8.3.5"
}

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "spigotmc"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    // PlaceholderAPI
    maven {
        url = uri("https://repo.helpch.at/releases")
    }
    // Colonel
    maven("https://r.irepo.space/maven/")
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly(fileTree("libs"))
    compileOnly(project(":fake-api"))

    compileOnly(project(":platform:bukkit:ksp-annotations"))
    ksp(project(":platform:bukkit:ksp"))

    implementation(project(":common"))
    implementation(files("libs/RelocatedNashorn-15.4.jar"))

    implementation("org.apache.groovy:groovy:4.0.32")

    implementation("com.github.ben-manes.caffeine:caffeine:2.9.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    implementation("pers.neige.colonel:colonel-common:+")
    implementation("pers.neige.colonel:colonel-kotlin:+")
    implementation("pers.neige.colonel:colonel-bukkit:+")
}


ksp {
    arg("bukkitksp.generatedPackage", "github.mkbaka.assiya.platform.bukkit.internal")
}

val relocates = linkedMapOf<String, Boolean>()

fun register() {
    fun reg(group: String, always: Boolean = true) {
        relocates[group] = always
    }

    reg("kotlin.", always = false)
    reg("_COROUTINE.", always = false)
    reg("kotlinx.coroutines.", always = false)
    reg("pers.neige.colonel.")
    reg("org.neosearch.stringsearcher.")
    reg("com.github.benmanes.caffeine.")
    reg("com.google.errorprone.annotations.")

    // groovy relocate 有各种叽里咕噜的毛病导致爆炸
    // 开摆
//    reg("groovyjarjarasm.")
//    reg("groovyjarjarantlr4.")
//    reg("groovyjarjarpicocli.")
//    reg("org.codehaus.groovy.")
//    reg("org.apache.groovy.")
//    // 不加 "." 会把判断文件后缀的字符串都 relocate 过去
//    reg("groovy.")
}
register()

val isDev = rootProject.version.toString().endsWith("dev")

tasks {

    processResources {
        val properties = mapOf(
            "version" to rootProject.version.toString(),
            "name" to rootProject.name,
        )
        inputs.properties(properties)
        filesMatching(listOf("plugin.yml")) {
            expand(properties)
        }
    }

    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")

        exclude("org/intellij/lang/annotations/**")
        exclude("org/jetbrains/annotations/**")
        exclude("org/jspecify/annotations/**")
        exclude("org/checkerframework/**")

        for ((group, always) in relocates) {
            if (isDev && !always) continue
            relocate(group, "${rootProject.group}.libs.$group")
        }

        providers.gradleProperty("assiyaOutputDir").orNull?.let {
            destinationDirectory.set(file(it))
        }
    }

}
