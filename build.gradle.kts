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
    implementation("org.xerial:sqlite-jdbc:3.45.2.0")
    val jUnitVersion = "5.10.2"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

application {
    mainClass.set("app.Main")
}
