# Solana Programs [![Gradle Check](https://github.com/sava-software/solana-programs/actions/workflows/build.yml/badge.svg)](https://github.com/sava-software/solana-programs/actions/workflows/build.yml) [![Publish Release](https://github.com/sava-software/solana-programs/actions/workflows/publish.yml/badge.svg)](https://github.com/sava-software/solana-programs/actions/workflows/publish.yml)

## Documentation

User documentation lives at [sava.software](https://sava.software/).

* [Dependency Configuration](https://sava.software/quickstart)
* [Programs](https://sava.software/libraries/programs)

## Contribution

Unit tests are needed and welcomed. Otherwise, please open a discussion, issue, or send an email before working on a
pull request.

## Build

[Generate a classic token](https://github.com/settings/tokens) with the `read:packages` scope needed to access
dependencies hosted on GitHub Package Repository.

#### ~/.gradle/gradle.properties

```properties
savaGithubPackagesUsername=GITHUB_USERNAME
savaGithubPackagesPassword=GITHUB_TOKEN
```

```shell
./gradlew check
```
