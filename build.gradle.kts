plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.panda-lang.org/releases")
}

dependencies {
    compileOnly(libs.paper.api)
    implementation(libs.kotlin.stdlib)
    implementation("de.exlll:configlib-paper:4.8.1")
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("org.jetbrains.exposed:exposed-core:1.3.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.3.0")
    implementation("org.xerial:sqlite-jdbc:3.53.2.0")
    implementation("org.flywaydb:flyway-core:12.8.1")
    implementation("dev.rollczi:litecommands-bukkit:3.10.9")
    implementation("dev.rollczi:litecommands-jakarta:3.10.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("dev.rollczi:litecommands-adventure:3.10.9")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

tasks.compileJava {
    options.compilerArgs.add("-parameters")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion(libs.versions.minecraft.get())
        jvmArgs(
            "-Xms2G",
            "-Xmx2G",
            "-Dcom.mojang.eula.agree=true"
        )
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
