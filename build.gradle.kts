plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.javamodularity.moduleplugin") version "2.0.0"
    id("com.gradleup.shadow") version "9.2.2"
}

repositories {
    mavenCentral()
}

javafx {
    modules("javafx.base", "javafx.controls", "javafx.graphics", "javafx.fxml")
    version  = "25"
}
dependencies {
    compileOnly("com.github.spotbugs:spotbugs-annotations:4.9.8")
    val jUnitVersion = "6.0.1"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

application {
    mainClass.set("app.Main")
}
