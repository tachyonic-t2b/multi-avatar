// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

subprojects {
    group = "com.pobochii"
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
    tasks.withType(JavaCompile::class).all {
        with(options) {
            isIncremental = true
            isFork = true
            targetCompatibility = JavaVersion.VERSION_11.name
            sourceCompatibility = JavaVersion.VERSION_11.name
        }
    }

    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
        kotlinOptions {
            incremental = true
            jvmTarget = "11"
        }
    }
}

tasks {
    val clean by register("clean", Delete::class) {
        delete(rootProject.buildDir)
    }
}
