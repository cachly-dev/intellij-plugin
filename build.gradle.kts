plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "dev.cachly"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.1")
        instrumentationTools()
    }
    implementation("com.google.code.gson:gson:2.11.0")
}

intellijPlatform {
    pluginConfiguration {
        id = "dev.cachly.brain"
        name = "Cachly Brain"
        version = project.version.toString()
        description = """
            <p>AI Brain health monitor for <a href="https://cachly.dev">Cachly</a>.</p>
            <ul>
              <li>Status bar widget showing lesson count and brain health</li>
              <li>Detailed lesson viewer with recall counts and token savings</li>
              <li>Works with the Cachly MCP server for AI-powered memory</li>
            </ul>
        """.trimIndent()
        changeNotes = """
            <h3>0.1.0</h3>
            <ul>
              <li>Initial release</li>
              <li>Status bar brain health widget</li>
              <li>Lesson viewer with recall tracking</li>
              <li>Token savings estimation</li>
            </ul>
        """.trimIndent()
        ideaVersion {
            sinceBuild = "241"
            untilBuild = "252.*"
        }
        vendor {
            name = "Cachly"
            email = "support@cachly.dev"
            url = "https://cachly.dev"
        }
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}
