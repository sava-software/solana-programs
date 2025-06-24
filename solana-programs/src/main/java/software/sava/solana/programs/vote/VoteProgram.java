package software.sava.solana.programs.vote;

import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.programs.Discriminator;
import software.sava.core.tx.Instruction;

import java.util.List;

import static software.sava.core.accounts.PublicKey.PUBLIC_KEY_LENGTH;
import static software.sava.core.accounts.meta.AccountMeta.createReadOnlySigner;
import static software.sava.core.accounts.meta.AccountMeta.createWrite;
import static software.sava.core.encoding.ByteUtil.putInt64LE;
import static software.sava.core.programs.Discriminator.NATIVE_DISCRIMINATOR_LENGTH;
import static software.sava.core.programs.Discriminator.serializeDiscriminator;
import static software.sava.core.tx.Instruction.createInstruction;

// https://github.com/anza-xyz/solana-sdk/blob/master/vote-interface/src/instruction.rs#L27
public final class VoteProgram {

  public enum Instructions implements Discriminator {

    // Initialize a vote account
    //
    // # Account references
    //   0. `[WRITE]` Uninitialized vote account
    //   1. `[]` Rent sysvar
    //   2. `[]` Clock sysvar
    //   3. `[SIGNER]` New validator identity (node_pubkey)
    InitializeAccount,

    // Authorize a key to send votes or issue a withdrawal
    //
    // # Account references
    //   0. `[WRITE]` Vote account to be updated with the Pubkey for authorization
    //   1. `[]` Clock sysvar
    //   2. `[SIGNER]` Vote or withdraw authority
    Authorize,

    // A Vote instruction with recent votes
    //
    // # Account references
    //   0. `[WRITE]` Vote account to vote with
    //   1. `[]` Slot hashes sysvar
    //   2. `[]` Clock sysvar
    //   3. `[SIGNER]` Vote authority
    Vote,

    // Withdraw some amount of funds
    //
    // # Account references
    //   0. `[WRITE]` Vote account to withdraw from
    //   1. `[WRITE]` Recipient account
    //   2. `[SIGNER]` Withdraw authority
    Withdraw,

    // Update the vote account's validator identity (node_pubkey)
    //
    // # Account references
    //   0. `[WRITE]` Vote account to be updated with the given authority public key
    //   1. `[SIGNER]` New validator identity (node_pubkey)
    //   2. `[SIGNER]` Withdraw authority
    UpdateValidatorIdentity,

    // Update the commission for the vote account
    //
    // # Account references
    //   0. `[WRITE]` Vote account to be updated
    //   1. `[SIGNER]` Withdraw authority
    UpdateCommission,

    // A Vote instruction with recent votes
    //
    // # Account references
    //   0. `[WRITE]` Vote account to vote with
    //   1. `[]` Slot hashes sysvar
    //   2. `[]` Clock sysvar
    //   3. `[SIGNER]` Vote authority
    VoteSwitch,

    // Authorize a key to send votes or issue a withdrawal
    //
    // This instruction behaves like `Authorize` with the additional requirement that the new vote
    // or withdraw authority must also be a signer.
    //
    // # Account references
    //   0. `[WRITE]` Vote account to be updated with the Pubkey for authorization
    //   1. `[]` Clock sysvar
    //   2. `[SIGNER]` Vote or withdraw authority
    //   3. `[SIGNER]` New vote or withdraw authority
    AuthorizeChecked,

    // Update the onchain vote state for the signer.
    //
    // # Account references
    //   0. `[Write]` Vote account to vote with
    //   1. `[SIGNER]` Vote authority
    UpdateVoteState,

    // Update the onchain vote state for the signer along with a switching proof.
    //
    // # Account references
    //   0. `[Write]` Vote account to vote with
    //   1. `[SIGNER]` Vote authority
    UpdateVoteStateSwitch,

    // Given that the current Voter or Withdrawer authority is a derived key,
    // this instruction allows someone who can sign for that derived key's
    // base key to authorize a new Voter or Withdrawer for a vote account.
    //
    // # Account references
    //   0. `[Write]` Vote account to be updated
    //   1. `[]` Clock sysvar
    //   2. `[SIGNER]` Base key of current Voter or Withdrawer authority's derived key
    AuthorizeWithSeed,

    // Given that the current Voter or Withdrawer authority is a derived key,
    // this instruction allows someone who can sign for that derived key's
    // base key to authorize a new Voter or Withdrawer for a vote account.
    //
    // This instruction behaves like `AuthorizeWithSeed` with the additional requirement
    // that the new vote or withdraw authority must also be a signer.
    //
    // # Account references
    //   0. `[Write]` Vote account to be updated
    //   1. `[]` Clock sysvar
    //   2. `[SIGNER]` Base key of current Voter or Withdrawer authority's derived key
    //   3. `[SIGNER]` New vote or withdraw authority
    AuthorizeCheckedWithSeed,

    // Update the onchain vote state for the signer.
    //
    // # Account references
    //   0. `[Write]` Vote account to vote with
    //   1. `[SIGNER]` Vote authority
    CompactUpdateVoteState,

    // Update the onchain vote state for the signer along with a switching proof.
    //
    // # Account references
    //   0. `[Write]` Vote account to vote with
    //   1. `[SIGNER]` Vote authority
    CompactUpdateVoteStateSwitch,

    // Sync the onchain vote state with local tower
    //
    // # Account references
    //   0. `[Write]` Vote account to vote with
    //   1. `[SIGNER]` Vote authority
    TowerSync,

    // Sync the onchain vote state with local tower along with a switching proof
    //
    // # Account references
    //   0. `[Write]` Vote account to vote with
    //   1. `[SIGNER]` Vote authority
    TowerSyncSwitch;

    private final byte[] data;

    Instructions() {
      this.data = serializeDiscriminator(this);
    }

    public byte[] data() {
      return this.data;
    }
  }

  public static Instruction initializeAccount(final SolanaAccounts solanaAccounts,
                                              final PublicKey voteAccount,
                                              final PublicKey validatorIdentity,
                                              final VoteInit voteInit) {
    final var keys = List.of(
        createWrite(voteAccount),
        solanaAccounts.readRentSysVar(),
        solanaAccounts.readClockSysVar(),
        createReadOnlySigner(validatorIdentity)
    );

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + VoteInit.BYTES];
    int offset = Instructions.InitializeAccount.write(data, 0);
    voteInit.write(data, offset);

    return createInstruction(solanaAccounts.invokedVoteProgram(), keys, data);
  }

  public static Instruction authorize(final SolanaAccounts solanaAccounts,
                                      final PublicKey voteAccount,
                                      final PublicKey currentAuthority,
                                      final PublicKey newAuthority,
                                      final VoteAuthorize voteAuthorize) {
    final var keys = List.of(
        createWrite(voteAccount),
        solanaAccounts.readClockSysVar(),
        createReadOnlySigner(currentAuthority)
    );

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + PUBLIC_KEY_LENGTH + voteAuthorize.l()];
    int offset = Instructions.Authorize.write(data, 0);
    offset += newAuthority.write(data, offset);
    voteAuthorize.write(data, offset);

    return createInstruction(solanaAccounts.invokedVoteProgram(), keys, data);
  }

  public static Instruction authorizeChecked(final SolanaAccounts solanaAccounts,
                                             final PublicKey voteAccount,
                                             final PublicKey currentAuthority,
                                             final PublicKey newAuthority,
                                             final VoteAuthorize voteAuthorize) {
    final var keys = List.of(
        createWrite(voteAccount),
        solanaAccounts.readClockSysVar(),
        createReadOnlySigner(currentAuthority),
        createReadOnlySigner(newAuthority)
    );

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + voteAuthorize.l()];
    Instructions.AuthorizeChecked.write(data, 0);
    voteAuthorize.write(data, NATIVE_DISCRIMINATOR_LENGTH);

    return createInstruction(solanaAccounts.invokedVoteProgram(), keys, data);
  }

  public static Instruction authorizeWithSeed(final SolanaAccounts solanaAccounts,
                                              final PublicKey voteAccount,
                                              final PublicKey baseKey,
                                              final VoteAuthorizeWithSeedArgs args) {
    final var keys = List.of(
        createWrite(voteAccount),
        solanaAccounts.readClockSysVar(),
        createReadOnlySigner(baseKey)
    );

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + args.l()];
    Instructions.AuthorizeWithSeed.write(data, 0);
    args.write(data, NATIVE_DISCRIMINATOR_LENGTH);

    return createInstruction(solanaAccounts.invokedVoteProgram(), keys, data);
  }

  public static Instruction authorizeCheckedWithSeed(final SolanaAccounts solanaAccounts,
                                                     final PublicKey voteAccount,
                                                     final PublicKey baseKey,
                                                     final PublicKey newAuthority,
                                                     final VoteAuthorizeCheckedWithSeedArgs args) {
    final var keys = List.of(
        createWrite(voteAccount),
        solanaAccounts.readClockSysVar(),
        createReadOnlySigner(baseKey),
        createReadOnlySigner(newAuthority)
    );

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + args.l()];
    Instructions.AuthorizeCheckedWithSeed.write(data, 0);
    args.write(data, NATIVE_DISCRIMINATOR_LENGTH);

    return createInstruction(solanaAccounts.invokedVoteProgram(), keys, data);
  }

  public static Instruction withdraw(final AccountMeta invokedProgram,
                                     final PublicKey voteAccount,
                                     final PublicKey recipientAccount,
                                     final PublicKey withdrawAuthority,
                                     final long lamports) {
    final var keys = List.of(
        createWrite(voteAccount),
        createWrite(recipientAccount),
        createReadOnlySigner(withdrawAuthority)
    );

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + Long.BYTES];
    Instructions.Withdraw.write(data, 0);
    putInt64LE(data, NATIVE_DISCRIMINATOR_LENGTH, lamports);

    return createInstruction(invokedProgram, keys, data);
  }

  public static Instruction updateValidatorIdentity(final AccountMeta invokedProgram,
                                                    final PublicKey voteAccount,
                                                    final PublicKey newValidatorIdentity,
                                                    final PublicKey withdrawAuthority) {
    final var keys = List.of(
        createWrite(voteAccount),
        createReadOnlySigner(newValidatorIdentity),
        createReadOnlySigner(withdrawAuthority)
    );

    return createInstruction(invokedProgram, keys, Instructions.UpdateValidatorIdentity.data());
  }

  public static Instruction updateCommission(final AccountMeta invokedProgram,
                                             final PublicKey voteAccount,
                                             final PublicKey withdrawAuthority,
                                             final int commission) {
    final var keys = List.of(
        createWrite(voteAccount),
        createReadOnlySigner(withdrawAuthority)
    );

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + 1];
    Instructions.UpdateCommission.write(data, 0);
    data[NATIVE_DISCRIMINATOR_LENGTH] = (byte) commission;

    return createInstruction(invokedProgram, keys, data);
  }

  private VoteProgram() {
  }
}
