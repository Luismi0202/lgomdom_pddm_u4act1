import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.mavenCentral

pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
            metadataSources {
                mavenPom()
                artifact()
            }
        }
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
    }

}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}


rootProject.name = "My Application"
include(":app")
 