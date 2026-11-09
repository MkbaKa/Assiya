plugins {
    id("com.google.devtools.ksp") version "2.3.6"
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(project(":platform:bukkit:mythic-ksp-annotations"))
    ksp(project(":platform:bukkit:mythic-ksp"))
    compileOnly(project(":platform:bukkit"))
    compileOnly(project(":common"))
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly(fileTree("libs") { include("*.jar") })
}

ksp {
    arg("mythicksp.hookClass", "github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.MythicMobs5Hook")
    arg("mythicksp.generatedPackage", "github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.generated")
}
