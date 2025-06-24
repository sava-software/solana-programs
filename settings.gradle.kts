plugins {
  id("software.sava.build") version "0.1.8"
  // id("software.sava.build.feature-jdk-provisioning") version "0.1.8"
}

rootProject.name = "solana-programs"

javaModules {
  directory(".") {
    group = "software.sava"
    plugin("software.sava.build.java-module")
  }
}
