plugins {
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(17)
    sourceSets.main {
        kotlin.srcDir("voice")
    }
}

dependencies {
    implementation(project(":agent"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("com.openai:openai-java:4.69.2")
}
