package software.sava.solana.programs.stakepool;

import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.accounts.PublicKey;

public record StakePoolAccountsRecord(PublicKey stakePoolProgram,
                                      AccountMeta invokedStakePoolProgram,
                                      AccountMeta readStakePoolProgram,
                                      PublicKey singleValidatorStakePoolProgram,
                                      AccountMeta invokedSingleValidatorStakePoolProgram,
                                      AccountMeta readSingleValidatorStakePoolProgram,
                                      PublicKey sanctumMultiValidatorStakePoolProgram,
                                      PublicKey sanctumSingleValidatorStakePoolProgram) implements StakePoolAccounts {
}
