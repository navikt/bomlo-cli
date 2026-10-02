plugins {
    alias(libs.plugins.sykepenger.kotlin)
}

dependencies {
    implementation(libs.rapidsAndRiversCli)
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
}
