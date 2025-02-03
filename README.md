# Solana Programs [![Build](https://github.com/sava-software/solana-programs/actions/workflows/gradle.yml/badge.svg)](https://github.com/sava-software/solana-programs/actions/workflows/gradle.yml) [![Release](https://github.com/sava-software/solana-programs/actions/workflows/release.yml/badge.svg)](https://github.com/sava-software/solana-programs/actions/workflows/release.yml)

## Documentation

User documentation lives at [sava.software](https://sava.software/).

* [Programs](https://sava.software/libraries/programs)

## Contribution

Unit tests are needed and welcomed. Otherwise, please open a discussion, issue, or send an email before working on a
pull request.

## Build

[Generate a classic token](https://github.com/settings/tokens) with the `read:packages` scope needed to access
dependencies hosted on GitHub Package Repository.

Create a `gradle.properties` file in the sava project directory root or under `$HOME/.gradle/`.

### gradle.properties

```properties
gpr.user=GITHUB_USERNAME
gpr.token=GITHUB_TOKEN
```

```shell
./gradlew check
```
