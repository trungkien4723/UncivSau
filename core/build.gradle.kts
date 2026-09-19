import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.unciv.build.BuildConfig

plugins {
    id("kotlin")
}

sourceSets {
    main {
        java.srcDir("src/")
    }
}

// Keep UncivGame.VERSION in sync with BuildConfig - upstream normally does this in CI,
// but without it every local/CI build shipped the stale hardcoded version string.
tasks.register("generateVersionData") {
    val sourceFile = file("src/com/unciv/UncivGame.kt")
    inputs.file(sourceFile)
    outputs.upToDateWhen { _ ->
        // Skip when the file already carries the current version
        sourceFile.readText().contains("Version(\"${BuildConfig.appVersion}\", ${BuildConfig.appCodeNumber})")
    }
    doLast {
        val text = sourceFile.readText()
        val updated = text.replace(
            Regex("""val VERSION = Version\("[^"]+", \d+\)"""),
            "val VERSION = Version(\"${BuildConfig.appVersion}\", ${BuildConfig.appCodeNumber})"
        )
        if (updated != text) {
            sourceFile.writeText(updated)
            logger.lifecycle("Updated UncivGame.VERSION to ${BuildConfig.appVersion} (${BuildConfig.appCodeNumber})")
        }
    }
}

tasks.named("compileKotlin") {
    dependsOn("generateVersionData")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
    }
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
}
