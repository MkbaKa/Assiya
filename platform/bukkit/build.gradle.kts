plugins {
    id("com.google.devtools.ksp") version "2.3.6"
    id("com.gradleup.shadow") version "9.6.1"
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
    // PacketEvents
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT") {
        attributes {
            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 21)
        }
    }
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0") {
        attributes {
            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 21)
        }
    }
    compileOnly("com.github.retrooper:packetevents-spigot:2.14.0")
    compileOnly(fileTree("libs"))
    compileOnly(project(":fake-api"))

    compileOnly(project(":platform:bukkit:ksp-annotations"))
    ksp(project(":platform:bukkit:ksp"))

    implementation(project(":common"))
    implementation(files("libs/RelocatedNashorn-15.4.jar"))

    implementation("org.apache.groovy:groovy:4.0.32")

    implementation("com.github.ben-manes.caffeine:caffeine:2.9.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    val colonelVersion = "1.0.13"
    implementation("pers.neige.colonel:colonel-common:$colonelVersion")
    implementation("pers.neige.colonel:colonel-kotlin:$colonelVersion")
    implementation("pers.neige.colonel:colonel-bukkit:$colonelVersion")
}

val compact by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

dependencies {
    compact(project(":platform:bukkit:compact-mythicmobs-v4"))
    compact(project(":platform:bukkit:compact-mythicmobs-v5"))
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
        dependsOn(compact)
        from({ compact.map { zipTree(it) } })
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")
        // KotlinModuleMetadataTransformer 需要看到所有 .kotlin_module 才能正确合并;
        // 其余条目保持默认 EXCLUDE, 避免重复的 META-INF/LICENSE 让 Paper PluginRemapper 拒绝该 jar.
        filesMatching("**/*.kotlin_module") {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

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
