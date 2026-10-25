plugins {
    application
    id("com.diffplug.spotless") version "8.0.0"
    id("com.google.cloud.tools.jib") version "3.5.4"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.iceberg:iceberg-core:1.12.0")
    runtimeOnly("org.apache.iceberg:iceberg-aws:1.12.0")
    runtimeOnly("org.apache.iceberg:iceberg-aws-bundle:1.12.0")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.17")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "com.github.kinolaev.iceberg.RemoveMissingFileReferences"
}

spotless {
    java {
        googleJavaFormat()
    }
}

jib {
    from {
        image = "gcr.io/distroless/java21-debian13:nonroot"
    }
}
