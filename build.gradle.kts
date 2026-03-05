plugins {
    kotlin("jvm") version "2.3.0"
}

// Sett opp repositories basert på om vi kjører i CI eller ikke
// Jf. https://github.com/navikt/utvikling/blob/main/docs/teknisk/Konsumere%20biblioteker%20fra%20Github%20Package%20Registry.md
repositories {
    mavenCentral()
    if (providers.environmentVariable("GITHUB_ACTIONS").orNull == "true") {
        maven {
            url = uri("https://maven.pkg.github.com/navikt/maven-release")
            credentials {
                username = "token"
                password = providers.environmentVariable("GITHUB_TOKEN").orNull!!
            }
        }
    } else {
        maven("https://repo.adeo.no/repository/github-package-registry-navikt/")
    }
}

val rapidsAndRiversCli = "1.2e828ce"
val junitJupiterVersion = "5.12.1"

dependencies {
    api("com.github.navikt:rapids-and-rivers-cli:$rapidsAndRiversCli")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of("21"))
    }
}

tasks {
    named<Jar>("jar") {
        archiveFileName.set("app.jar")
        manifest {
            attributes["Main-Class"] = "no.nav.helse.cli.ApplicationKt"
            archiveVersion.orNull?.also { version ->
                attributes["Implementation-Version"] = version
            }
            project.findProperty("repository")?.also { repo ->
                attributes["Implementation-Vendor"] = repo
            }
        }

        // unzips all runtime dependencies (jars) and add the files
        // to the application jar
        from(configurations.runtimeClasspath.get().map(::zipTree)) {
            // prints a warning to console to tell about duplicate files
            duplicatesStrategy = DuplicatesStrategy.WARN
            // don't copy any file matching logback.xml;
            filesMatching("logback.xml") { exclude() }
        }
    }

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("skipped", "failed")
        }
    }
}
