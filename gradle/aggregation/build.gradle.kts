plugins {
  id("software.sava.build.feature.publish-maven-central")
}

dependencies {
  nmcpAggregation(project(":solana-programs"))
}

tasks.register("publishToGitHubPackages") {
  group = "publishing"
  dependsOn(":solana-programs:publishMavenJavaPublicationToSavaGithubPackagesRepository")
}
