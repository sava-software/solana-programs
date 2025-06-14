pluginManagement {
  includeBuild("gradle/plugins")
}
plugins {
  id("software.sava.gradle.build")
}

rootProject.name = "root"

javaModules {
  directory(".") {
    group = "software.sava"
    plugin("software.sava.gradle.java-module")
  }
  versions("gradle/versions")
}
