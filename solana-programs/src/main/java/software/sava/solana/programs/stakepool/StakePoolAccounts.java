package software.sava.solana.programs.stakepool;

import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.accounts.PublicKey;

import static software.sava.core.accounts.meta.AccountMeta.createInvoked;
import static software.sava.core.accounts.meta.AccountMeta.createRead;
import static software.sava.core.accounts.PublicKey.fromBase58Encoded;

public interface StakePoolAccounts {

  StakePoolAccounts MAIN_NET = createAddressConstants(
      "SPoo1Ku8WFXoNDMHPsrGSTSG1Y47rzgn41SLUNakuHy",
      "SVSPxpvHdN29nkVg9rPapPNDddN5DipNLRUFhyjFThE",
      "SPMBzsVUuoHA4Jm6KunbsotaahvVikZs1JyTW6iJvbn",
      "SP12tWFxD9oJsVWNavTTBZvMbA6gkAmxtVgxdqvyvhY"
  );

  static StakePoolAccounts createAddressConstants(final PublicKey stakePoolProgram,
                                                  final PublicKey singleValidatorStakePoolProgram,
                                                  final PublicKey sanctumMultiValidatorStakePoolProgram,
                                                  final PublicKey sanctumSingleValidatorStakePoolProgram) {
    return new StakePoolAccountsRecord(
        stakePoolProgram,
        createInvoked(stakePoolProgram),
        createRead(stakePoolProgram),
        singleValidatorStakePoolProgram,
        createInvoked(singleValidatorStakePoolProgram),
        createRead(singleValidatorStakePoolProgram),
        sanctumMultiValidatorStakePoolProgram,
        sanctumSingleValidatorStakePoolProgram
    );
  }

  static StakePoolAccounts createAddressConstants(final String stakePoolProgram,
                                                  final String singleValidatorStakePoolProgram,
                                                  final String sanctumMultiValidatorStakePoolProgram,
                                                  final String sanctumSingleValidatorStakePoolProgram) {
    return StakePoolAccounts.createAddressConstants(
        fromBase58Encoded(stakePoolProgram),
        fromBase58Encoded(singleValidatorStakePoolProgram),
        fromBase58Encoded(sanctumMultiValidatorStakePoolProgram),
        fromBase58Encoded(sanctumSingleValidatorStakePoolProgram));
  }

  PublicKey stakePoolProgram();

  AccountMeta invokedStakePoolProgram();

  AccountMeta readStakePoolProgram();

  PublicKey singleValidatorStakePoolProgram();

  AccountMeta invokedSingleValidatorStakePoolProgram();

  AccountMeta readSingleValidatorStakePoolProgram();

  PublicKey sanctumMultiValidatorStakePoolProgram();

  PublicKey sanctumSingleValidatorStakePoolProgram();
}
