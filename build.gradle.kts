import com.google.protobuf.gradle.*

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.30"
    id("org.jetbrains.kotlin.kapt") version "1.4.30"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.4.30"
    id("org.jetbrains.kotlin.plugin.jpa") version "1.4.30"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("io.micronaut.application") version "1.4.2"
    id("com.google.protobuf") version "0.8.13"
}

version = "0.1"
group = "br.com.zup"

val kotlinVersion = project.properties.get("kotlinVersion")
repositories {
    mavenCentral()
}

micronaut {
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("br.com.zup.*")
    }
}

dependencies {
    kapt("io.micronaut.data:micronaut-data-processor")
    implementation("io.micronaut:micronaut-runtime")
    //JPA e Banco
    implementation("io.micronaut.beanvalidation:micronaut-hibernate-validator")
    implementation("org.hibernate:hibernate-validator:6.1.6.Final")
    implementation("io.micronaut.data:micronaut-data-hibernate-jpa")
    implementation("io.micronaut.sql:micronaut-hibernate-jpa")
    annotationProcessor("io.micronaut.data:micronaut-data-processor")
    implementation("io.micronaut:micronaut-validation")
    runtimeOnly("org.postgresql:postgresql")
    //runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    testImplementation("com.h2database:h2")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")

    implementation("io.micronaut.grpc:micronaut-grpc-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.netflix:micronaut-netflix-hystrix")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    implementation("io.micronaut.kafka:micronaut-kafka")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut.xml:micronaut-jackson-xml")
    runtimeOnly("ch.qos.logback:logback-classic")
    testImplementation("org.hamcrest:hamcrest")
    testImplementation("org.mockito:mockito-core")
    testImplementation("io.micronaut:micronaut-http-client")
}


application {
    mainClass.set("br.com.zup.ApplicationKt")
}
java {
    sourceCompatibility = JavaVersion.toVersion("11")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }


}
sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.14.0"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.33.1"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without options.
                id("grpc")
            }
        }
    }
}
