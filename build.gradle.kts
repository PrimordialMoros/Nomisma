plugins {
    java
    alias(libs.plugins.checker)
    alias(libs.plugins.shadow)
}

group = "me.moros"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    if (!isSnapshot()) {
        withJavadocJar()
    }
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.essentialsx.net/releases/")
    maven("https://jitpack.io")
    flatDir { dirs("$rootDir/libs") }
}

dependencies {
    implementation(libs.storage)
    implementation(libs.tasker.bukkit)
    implementation(libs.caffeine) { exclude(module = "checker-qual") }
    implementation(libs.jdbi) { exclude(module = "caffeine") }
    implementation(libs.hikari) { exclude(module = "slf4j-api") }
    implementation(libs.bundles.drivers) { isTransitive = false }
    implementation(libs.bstats.bukkit)
    implementation(libs.cloud.paper)
    implementation(libs.cloud.minecraft) { isTransitive = false}
    implementation(libs.configurate.hocon)
    compileOnly(libs.paper)
    compileOnly(libs.papi)
    compileOnly(libs.vault)
    compileOnly(libs.bossshoppro)
    compileOnly(libs.gemseconomy)
    compileOnly(libs.essentials)
}

configurations.implementation {
    exclude(module = "error_prone_annotations")
    exclude(module = "slf4j-api")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveBaseName.set(project.name)
        dependencies {
            relocate("com.github.benmanes.caffeine", "me.moros.nomisma.internal.caffeine")
            relocate("com.zaxxer.hikari", "me.moros.nomisma.internal.hikari")
            relocate("me.moros.storage", "me.moros.nomisma.internal.storage")
            relocate("me.moros.tasker", "me.moros.nomisma.internal.tasker")
            relocate("org.bstats", "me.moros.nomisma.bstats")
            relocate("org.h2", "me.moros.nomisma.internal.h2")
            relocate("org.hsqldb", "me.moros.nomisma.internal.hsqldb")
            relocate("org.jdbi", "me.moros.nomisma.internal.jdbi")
            relocate("org.mariadb", "me.moros.nomisma.internal.mariadb")
            relocate("org.postgresql", "me.moros.nomisma.internal.postgresql")
            relocate("cloud.commandframework", "me.moros.nomisma.internal.cf")
            relocate("com.typesafe", "me.moros.nomisma.internal.typesafe")
            relocate("io.leangen", "me.moros.nomisma.internal.leangen")
            relocate("org.spongepowered.configurate", "me.moros.nomisma.internal.configurate")
        }
        //minimize()
    }
    build {
        dependsOn(shadowJar)
    }
    withType<JavaCompile> {
        options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
        options.encoding = "UTF-8"
    }
    withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    named<Copy>("processResources") {
        filesMatching("plugin.yml") {
            expand("pluginVersion" to project.version)
        }
        from("LICENSE") {
            rename { "${project.name.uppercase()}_${it}"}
        }
    }
}

fun isSnapshot() = project.version.toString().endsWith("-SNAPSHOT")
