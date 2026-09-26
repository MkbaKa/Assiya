group = "github.mkbaka.bukkitksp"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":platform:bukkit:ksp-annotations"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.6")
}
