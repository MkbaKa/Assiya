dependencies {
    compileOnly(files("../platform/bukkit/compact-mythicmobs-v4/libs/MythicMobs-4.11.0.jar"))
}

gradle.buildFinished {
    buildDir.deleteRecursively()
}
