plugins {
    java
    id("com.github.johnrengelman.shadow").version("7.1.2")
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
    implementation("me.moros", "storage", "2.1.0")
    implementation("com.github.ben-manes.caffeine", "caffeine", "3.0.6") {
        exclude(module = "checker-qual")
    }
    implementation("org.jdbi", "jdbi3-core", "3.28.0") {
        exclude(module = "caffeine")
        exclude(module = "slf4j-api")
    }
    implementation("com.zaxxer", "HikariCP", "5.0.1") {
        exclude(module = "slf4j-api")
    }
    implementation("org.postgresql", "postgresql", "42.3.3") {
        exclude(module = "checker-qual")
    }
    implementation("com.h2database", "h2", "2.1.212")
    implementation("org.bstats", "bstats-bukkit", "2.2.1")
    implementation("cloud.commandframework","cloud-paper", "1.7.0")
    implementation("cloud.commandframework","cloud-minecraft-extras", "1.7.0") {
        exclude(group = "net.kyori")
    }
    implementation("org.spongepowered", "configurate-hocon", "4.1.2")
    compileOnly("org.checkerframework", "checker-qual", "3.21.3")
    compileOnly("io.papermc.paper", "paper-api", "1.18.2-R0.1-SNAPSHOT")
    compileOnly("me.clip", "placeholderapi", "2.11.1")
    compileOnly("com.github.MilkBowl", "VaultAPI", "1.7")
    compileOnly("org.black_ixx", "BossShopPro", "2.0.9")
    compileOnly("me.xanium", "GemsEconomy", "4.9.2")
    compileOnly("net.essentialsx", "EssentialsX", "2.19.0")
}

configurations.implementation {
    exclude(module = "error_prone_annotations")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveBaseName.set(project.name)
        dependencies {
            relocate("com.github.benmanes.caffeine", "me.moros.nomisma.internal.caffeine")
            relocate("com.zaxxer.hikari", "me.moros.nomisma.internal.hikari")
            relocate("me.moros.storage", "me.moros.nomisma.internal.storage")
            relocate("org.bstats", "me.moros.nomisma.bstats")
            relocate("org.h2", "me.moros.nomisma.internal.h2")
            relocate("org.jdbi", "me.moros.nomisma.internal.jdbi")
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
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
        options.encoding = "UTF-8"
    }
    named<Copy>("processResources") {
        filesMatching("plugin.yml") {
            expand("pluginVersion" to project.version)
        }
        from("LICENSE") {
            rename { "${project.name.toUpperCase()}_${it}"}
        }
    }
}

fun isSnapshot() = project.version.toString().endsWith("-SNAPSHOT")
