# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

## [25.0.2] - 2026-03-31

### Added
- Missing Token2022Program extensions (#3).
- Constant `MAX_COMPUTE_BUDGET_IX` for simulation purposes.

### Changed
- Formatting nits; split tests between original token program and 2022.
- Refactored to replace `ByteBuffer` with `ByteUtil`.

## [25.0.1] - 2026-03-26

### Fixed
- Missing signer privileges for `TokenProgram` multisig variants.

## [25.0.0] - 2025-09-17

### Changed
- Move to Java 25.

## [24.20.3] - 2025-06-24

### Added
- `publishToGitHubPackages` task.

## [24.20.2] - 2025-06-14

### Fixed
- Missing `--sign` flag in release pipeline.

## [24.20.1] - 2025-06-14

### Changed
- Publish to both GitHub Packages and Maven Central.
- Move Gradle Groovy build to Kotlin.

### Added
- Administrative Vote Program instructions.

### Fixed
- License information.

## [21.0.2] - 2026-03-26

### Fixed
- GitHub Action trigger for java-21 branch.

## [1.20.0] - 2025-05-27

### Added
- Ed25519SignatureOffsets support with tests.

### Changed
- Updated dependencies.

## [1.19.2] - 2025-05-21

### Fixed
- `initializeMint` instruction.

## [1.19.1] - 2025-05-01

### Changed
- Updated Gradle.
- Relaxed `List` interface methods.

## [1.19.0] - 2025-04-19

### Changed
- Move to Java 24.

## [1.18.11] - 2025-04-12

### Changed
- Updated dependencies.

## [1.18.10] - 2025-03-21

### Added
- Instruction data parsers for the system program.

### Fixed
- `withSeeds` reads/writes in stake program.

## [1.18.9] - 2025-03-20

### Added
- Instruction data parsers for the stake program.

## [1.18.8] - 2025-03-19

### Added
- Utility methods for creating memo program instructions.

## [1.18.7] - 2025-03-15

### Added
- Complete stake pool program instructions.

## [1.18.6] - 2025-03-14

### Changed
- Updated stake and stake pool methods.

## [1.18.5] - 2025-03-11

### Added
- Move stake and lamports program methods.

## [1.18.4] - 2025-03-08

### Added
- Token Program 2022 instructions.

## [1.18.3] - 2025-03-06

### Added
- Token program methods and multisig variants.

## [1.18.2] - 2025-02-28

### Changed
- Move to Apache-2.0 license.
- Exposed program instruction discriminators.

## [1.18.1] - 2025-02-12

### Changed
- Default base account and from key to fee payer.

## [1.18.0] - 2025-02-12

### Changed
- Default base account and from key to fee payer.

## [1.17.15] - 2025-02-01

### Added
- System program nonce account methods.
- Convenience methods for creating advance nonce instruction.
- Documentation for durable nonce transactions.

## [1.7.14] - 2025-01-25

### Fixed
- Typo in release action.

### Changed
- Cleaned up build.

## [1.7.12] - 2025-01-20

### Changed
- Bumped sava and dependencies.

## [1.7.10] - 2025-01-08

### Changed
- Bumped dependencies.

### Fixed
- `closeStakeAccount`.

## [1.7.9] - 2025-01-01

### Added
- More flexible `withdrawStake` method.

## [1.7.8] - 2025-01-01

### Added
- More flexible `withdrawSol` method.

## [1.7.7] - 2024-12-31

### Added
- Stake pool state offset.

## [1.7.6] - 2024-11-27

### Fixed
- `createDepositSolKeys`.

## [1.7.5] - 2024-11-16

### Added
- Compute budget `setLoadedAccountsDataSizeLimit` instruction.

## [1.7.4] - 2024-11-11

### Added
- Max compute unit constant.

## [1.7.3] - 2024-10-03

### Changed
- Bumped sava.

## [1.7.2] - 2024-09-20

### Changed
- Bumped sava.

## [1.7.0] - 2024-09-17

### Changed
- Bumped sava.

## [1.5.1] - 2024-09-06

### Changed
- More generic `delegateStakeAccount` methods.

## [1.5.0] - 2024-09-06

### Changed
- Removed non-account-state related methods.

## [1.4.5] - 2024-08-23

### Changed
- Consistent create ATA params.

## [1.4.3] - 2024-08-22

### Changed
- Shortened ATA method names.

## [1.4.2] - 2024-08-22

### Added
- Expanded `findAssociatedTokenProgramAddress` methods.

## [1.4.1] - 2024-08-22

### Changed
- Pulled create ATA methods up.

## [1.4.0] - 2024-08-22

### Added
- Expanded ATA creation methods.

## [1.3.6] - 2024-08-19

### Changed
- Pulled up methods with no state.
- Pulled stateless methods up.
- Authorize stake account methods.
- Assume owner has stake authority on `deactivateStakeAccount`.

## [1.3.1] - 2024-08-18

### Added
- Deactivate stake accounts to account client to match GLAM interface.

## [1.3.0] - 2024-08-18

### Changed
- Removed non-state code from implementation.

## [1.2.0] - 2024-08-18

### Changed
- Separated fee payer and owner.

## [1.1.0] - 2024-08-15

### Added
- `fetchClockSysVar`.

## [1.0.10] - 2024-08-15

### Changed
- Synced sava changes.

## [1.0.9] - 2024-08-15

### Changed
- Use `writeOrdinal`.

## [1.0.8] - 2024-08-15

### Added
- (Un)wrap SOL convenience methods.

## [1.0.7] - 2024-08-13

### Added
- Stake pool `fetchProgramState`.

## [1.0.5] - 2024-08-12

### Added
- Merge stake accounts into.

## [1.0.4] - 2024-08-11

### Added
- `StakeAccount` state utility.

### Changed
- Aligned `PublicKey` serialization with Borsh.

## [1.0.3] - 2024-08-11

### Fixed
- Merge instruction.

## [1.0.2] - 2024-08-10

### Changed
- Minor nits.

## [1.0.1] - 2024-08-10

### Added
- Exported system program.

## [1.0.0] - 2024-08-10

### Added
- Initial release with Solana program instruction builders for System, Stake, Stake Pool, Token, Token 2022, Compute Budget, Associated Token, and Memo programs.
