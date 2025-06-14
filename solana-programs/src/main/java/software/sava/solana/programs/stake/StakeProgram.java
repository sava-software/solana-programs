package software.sava.solana.programs.stake;

import software.sava.core.accounts.AccountWithSeed;
import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.borsh.Borsh;
import software.sava.core.encoding.ByteUtil;
import software.sava.core.programs.Discriminator;
import software.sava.core.tx.Instruction;
import software.sava.solana.programs.serde.SerdeUtil;

import java.time.Instant;
import java.util.List;
import java.util.OptionalLong;

import static software.sava.core.accounts.PublicKey.PUBLIC_KEY_LENGTH;
import static software.sava.core.accounts.meta.AccountMeta.*;
import static software.sava.core.encoding.ByteUtil.getInt64LE;
import static software.sava.core.programs.Discriminator.NATIVE_DISCRIMINATOR_LENGTH;
import static software.sava.core.programs.Discriminator.serializeDiscriminator;

// https://github.com/solana-program/stake
public final class StakeProgram {

  public enum Instructions implements Discriminator {
    //  Initialize a stake with lockup and authorization information
    // 
    //  # Account references
    //    0. '[WRITE]' Uninitialized stake account
    //    1. '[]' Rent sysvar
    // 
    //  Authorized carries pub keys that must sign staker transactions
    //    and withdrawer transactions.
    //  Lockup carries information about withdrawal restrictions
    Initialize(
//        Authorized, Lockup
    ),

    //  Authorize a key to manage stake or withdrawal
    // 
    //  # Account references
    //    0. '[WRITE]' Stake account to be updated
    //    1. '[]' Clock sysvar
    //    2. '[SIGNER]' The stake or withdraw authority
    //    3. Optional: '[SIGNER]' Lockup authority, if updating StakeAuthorize::Withdrawer before
    //       lockup expiration
    Authorize(
//        Pubkey, StakeAuthorize
    ),

    //  Delegate a stake to a particular vote account
    // 
    //  # Account references
    //    0. '[WRITE]' Initialized stake account to be delegated
    //    1. '[]' Vote account to which this stake will be delegated
    //    2. '[]' Clock sysvar
    //    3. '[]' Stake history sysvar that carries stake warmup/cooldown history
    //    4. '[]' Address of config account that carries stake config
    //    5. '[SIGNER]' Stake authority
    // 
    //  The entire balance of the staking account is staked.  DelegateStake
    //    can be called multiple times, but re-delegation is delayed
    //    by one epoch
    DelegateStake,

    //  Split u64 tokens and stake off a stake account into another stake account.
    // 
    //  # Account references
    //    0. '[WRITE]' Stake account to be split; must be in the Initialized or Stake state
    //    1. '[WRITE]' Uninitialized stake account that will take the split-off amount
    //    2. '[SIGNER]' Stake authority
    Split(
//        u64
    ),

    //  Withdraw unstaked lamports from the stake account
    // 
    //  # Account references
    //    0. '[WRITE]' Stake account from which to withdraw
    //    1. '[WRITE]' Recipient account
    //    2. '[]' Clock sysvar
    //    3. '[]' Stake history sysvar that carries stake warmup/cooldown history
    //    4. '[SIGNER]' Withdraw authority
    //    5. Optional: '[SIGNER]' Lockup authority, if before lockup expiration
    // 
    //  The u64 is the portion of the stake account balance to be withdrawn,
    //     must be '<= StakeAccount.lamports - staked_lamports'.
    Withdraw(
//        u64
    ),

    //  Deactivates the stake in the account
    // 
    //  # Account references
    //    0. '[WRITE]' Delegated stake account
    //    1. '[]' Clock sysvar
    //    2. '[SIGNER]' Stake authority
    Deactivate,

    //  Set stake lockup
    // 
    //  If a lockup is not active, the withdrawal authority may set a new lockup
    //  If a lockup is active, the lockup custodian may update the lockup parameters
    // 
    //  # Account references
    //    0. '[WRITE]' Initialized stake account
    //    1. '[SIGNER]' Lockup authority or withdraw authority
    SetLockup(
//        LockupArgs
    ),

    //  Merge two stake accounts.
    // 
    //  Both accounts must have identical lockup and authority keys. A merge
    //  is possible between two stakes in the following states with no additional
    //  conditions:
    // 
    //  * two deactivated stakes
    //  * an inactive stake into an activating stake during its activation epoch
    // 
    //  For the following cases, the voter pubkey and vote credits observed must match:
    // 
    //  * two activated stakes
    //  * two activating accounts that share an activation epoch, during the activation epoch
    // 
    //  All other combinations of stake states will fail to merge, including all
    //  "transient" states, where a stake is activating or deactivating with a
    //  non-zero effective stake.
    // 
    //  # Account references
    //    0. '[WRITE]' Destination stake account for the merge
    //    1. '[WRITE]' Source stake account for to merge.  This account will be drained
    //    2. '[]' Clock sysvar
    //    3. '[]' Stake history sysvar that carries stake warmup/cooldown history
    //    4. '[SIGNER]' Stake authority
    Merge,

    //  Authorize a key to manage stake or withdrawal with a derived key
    // 
    //  # Account references
    //    0. '[WRITE]' Stake account to be updated
    //    1. '[SIGNER]' Base key of stake or withdraw authority
    //    2. '[]' Clock sysvar
    //    3. Optional: '[SIGNER]' Lockup authority, if updating StakeAuthorize::Withdrawer before
    //       lockup expiration
    AuthorizeWithSeed(
//        AuthorizeWithSeedArgs
    ),

    //  Initialize a stake with authorization information
    // 
    //  This instruction is similar to 'Initialize' except that the withdrawal authority
    //  must be a signer, and no lockup is applied to the account.
    // 
    //  # Account references
    //    0. '[WRITE]' Uninitialized stake account
    //    1. '[]' Rent sysvar
    //    2. '[]' The stake authority
    //    3. '[SIGNER]' The withdrawal authority
    // 
    InitializeChecked,

    //  Authorize a key to manage stake or withdrawal
    // 
    //  This instruction behaves like 'Authorize' with the additional requirement that the new
    //  stake or withdraw authority must also be a signer.
    // 
    //  # Account references
    //    0. '[WRITE]' Stake account to be updated
    //    1. '[]' Clock sysvar
    //    2. '[SIGNER]' The stake or withdraw authority
    //    3. '[SIGNER]' The new stake or withdraw authority
    //    4. Optional: '[SIGNER]' Lockup authority, if updating StakeAuthorize::Withdrawer before
    //       lockup expiration
    AuthorizeChecked(
//        StakeAuthorize
    ),

    //  Authorize a key to manage stake or withdrawal with a derived key
    // 
    //  This instruction behaves like 'AuthorizeWithSeed' with the additional requirement that
    //  the new stake or withdraw authority must also be a signer.
    // 
    //  # Account references
    //    0. '[WRITE]' Stake account to be updated
    //    1. '[SIGNER]' Base key of stake or withdraw authority
    //    2. '[]' Clock sysvar
    //    3. '[SIGNER]' The new stake or withdraw authority
    //    4. Optional: '[SIGNER]' Lockup authority, if updating StakeAuthorize::Withdrawer before
    //       lockup expiration
    AuthorizeCheckedWithSeed(
//        AuthorizeCheckedWithSeedArgs
    ),

    //  Set stake lockup
    // 
    //  This instruction behaves like 'SetLockup' with the additional requirement that
    //  the new lockup authority also be a signer.
    // 
    //  If a lockup is not active, the withdrawal authority may set a new lockup
    //  If a lockup is active, the lockup custodian may update the lockup parameters
    // 
    //  # Account references
    //    0. '[WRITE]' Initialized stake account
    //    1. '[SIGNER]' Lockup authority or withdraw authority
    //    2. Optional: '[SIGNER]' New lockup authority
    SetLockupChecked(
//        LockupCheckedArgs
    ),

    //  Get the minimum stake delegation, in lamports
    // 
    //  # Account references
    //    None
    // 
    //  Returns the minimum delegation as a little-endian encoded u64 value.
    //  Programs can use the ['get_minimum_delegation()'] helper function to invoke and
    //  retrieve the return value for this instruction.
    // 
    //  ['get_minimum_delegation()']: super::tools::get_minimum_delegation
    GetMinimumDelegation,

    //  Deactivate stake delegated to a vote account that has been delinquent for at least
    //  'MINIMUM_DELINQUENT_EPOCHS_FOR_DEACTIVATION' epochs.
    // 
    //  No signer is required for this instruction as it is a common good to deactivate abandoned
    //  stake.
    // 
    //  # Account references
    //    0. '[WRITE]' Delegated stake account
    //    1. '[]' Delinquent vote account for the delegated stake account
    //    2. '[]' Reference vote account that has voted at least once in the last
    //       'MINIMUM_DELINQUENT_EPOCHS_FOR_DEACTIVATION' epochs
    DeactivateDelinquent,

    //  Relegate activated stake to another vote account.
    // 
    //  Upon success:
    //    * the balance of the delegated stake account will be reduced to the undelegated amount in
    //      the account (rent exempt minimum and any additional lamports not part of the delegation),
    //      and scheduled for deactivation.
    //    * the provided uninitialized stake account will receive the original balance of the
    //      delegated stake account, minus the rent exempt minimum, and scheduled for activation to
    //      the provided vote account. Any existing lamports in the uninitialized stake account
    //      will also be included in the re-delegation.
    // 
    //  # Account references
    //    0. '[WRITE]' Delegated stake account to be re-delegated. The account must be fully
    //       activated and carry a balance greater than or equal to the minimum delegation amount
    //       plus rent exempt minimum
    //    1. '[WRITE]' Uninitialized stake account that will hold the re-delegated stake
    //    2. '[]' Vote account to which this stake will be re-delegated
    //    3. '[]' Address of config account that carries stake config
    //    4. '[SIGNER]' Stake authority
    // 
    Redelegate,

    // Move stake between accounts with the same authorities and lockups, using Staker authority.
    //
    // The source account must be fully active. If its entire delegation is moved, it immediately
    // becomes inactive. Otherwise, at least the minimum delegation of active stake must remain.
    //
    // The destination account must be fully active or fully inactive. If it is active, it must
    // be delegated to the same vote account as the source. If it is inactive, it
    // immediately becomes active, and must contain at least the minimum delegation. The
    // destination must be pre-funded with the rent-exempt reserve.
    //
    // This instruction only affects or moves active stake. Additional unstaked lamports are never
    // moved, activated, or deactivated, and accounts are never deallocated.
    //
    // # Account references
    //   0. `[WRITE]` Active source stake account
    //   1. `[WRITE]` Active or inactive destination stake account
    //   2. `[SIGNER]` Stake authority
    //
    // The `u64` is the portion of the stake to move, which may be the entire delegation
    MoveStake, //(u64),

    // Move unstaked lamports between accounts with the same authorities and lockups, using Staker
    // authority.
    //
    // The source account must be fully active or fully inactive. The destination may be in any
    // mergeable state (active, inactive, or activating, but not in warmup cooldown). Only lamports that
    // are neither backing a delegation nor required for rent-exemption may be moved.
    //
    // # Account references
    //   0. `[WRITE]` Active or inactive source stake account
    //   1. `[WRITE]` Mergeable destination stake account
    //   2. `[SIGNER]` Stake authority
    //
    // The `u64` is the portion of available lamports to move
    MoveLamports; //(u64);

    private final byte[] data;

    Instructions() {
      this.data = serializeDiscriminator(this);
    }

    public byte[] data() {
      return this.data;
    }
  }

  public static Instruction initialize(final SolanaAccounts solanaAccounts,
                                       final PublicKey unInitializedStakeAccount,
                                       final PublicKey staker,
                                       final PublicKey withdrawer) {
    return initialize(solanaAccounts, unInitializedStakeAccount, staker, withdrawer, LockUp.NO_LOCKUP);
  }

  public static Instruction initialize(final SolanaAccounts solanaAccounts,
                                       final PublicKey unInitializedStakeAccount,
                                       final PublicKey staker,
                                       final PublicKey withdrawer,
                                       final LockUp lockUp) {
    final var keys = List.of(
        createWrite(unInitializedStakeAccount),
        solanaAccounts.readRentSysVar()
    );

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + PUBLIC_KEY_LENGTH + PUBLIC_KEY_LENGTH + LockUp.BYTES];
    int i = Instructions.Initialize.write(data);
    i += staker.write(data, i);
    i += withdrawer.write(data, i);
    lockUp.write(data, i);

    return Instruction.createInstruction(solanaAccounts.invokedStakeProgram(), keys, data);
  }

  public record Initialize(byte[] discriminator, PublicKey staker, PublicKey withdrawer, LockUp lockUp) {

    public static Initialize read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static Initialize read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      int i = offset + discriminator.length;
      final var staker = PublicKey.readPubKey(data, i);
      i += PUBLIC_KEY_LENGTH;
      final var withdrawer = PublicKey.readPubKey(data, i);
      i += PUBLIC_KEY_LENGTH;
      final var lockUp = LockUp.read(data, i);
      return new Initialize(discriminator, staker, withdrawer, lockUp);
    }
  }

  public static Instruction initializeChecked(final SolanaAccounts solanaAccounts,
                                              final PublicKey unInitializedStakeAccount,
                                              final PublicKey staker,
                                              final PublicKey withdrawer) {
    final var keys = List.of(
        createWrite(unInitializedStakeAccount),
        solanaAccounts.readRentSysVar(),
        createRead(staker),
        createReadOnlySigner(withdrawer)
    );

    return Instruction.createInstruction(solanaAccounts.invokedStakeProgram(), keys, Instructions.InitializeChecked.data);
  }

  public static Instruction authorize(final SolanaAccounts solanaAccounts,
                                      final List<AccountMeta> keys,
                                      final PublicKey newAuthority,
                                      final StakeAuthorize stakeAuthorize) {
    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + PUBLIC_KEY_LENGTH + stakeAuthorize.l()];
    Instructions.Authorize.write(data);
    newAuthority.write(data, NATIVE_DISCRIMINATOR_LENGTH);
    stakeAuthorize.write(data, NATIVE_DISCRIMINATOR_LENGTH + PUBLIC_KEY_LENGTH);

    return Instruction.createInstruction(solanaAccounts.invokedStakeProgram(), keys, data);
  }

  public record Authorize(byte[] discriminator, PublicKey newAuthority, StakeAuthorize stakeAuthorize) {

    public static Authorize read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static Authorize read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      int i = offset + discriminator.length;
      final var newAuthority = PublicKey.readPubKey(data, i);
      i += PUBLIC_KEY_LENGTH;
      final var stakeAuthorize = StakeAuthorize.read(data, i);
      return new Authorize(discriminator, newAuthority, stakeAuthorize);
    }
  }

  public static Instruction authorize(final SolanaAccounts solanaAccounts,
                                      final PublicKey stakeAccount,
                                      final PublicKey stakeOrWithdrawAuthority,
                                      final PublicKey newAuthority,
                                      final StakeAuthorize stakeAuthorize) {
    final var keys = List.of(
        createWrite(stakeAccount),
        solanaAccounts.readClockSysVar(),
        createReadOnlySigner(stakeOrWithdrawAuthority)
    );
    return authorize(solanaAccounts, keys, newAuthority, stakeAuthorize);
  }

  public static Instruction authorize(final SolanaAccounts solanaAccounts,
                                      final PublicKey stakeAccount,
                                      final PublicKey stakeOrWithdrawAuthority,
                                      final PublicKey lockupAuthority,
                                      final PublicKey newAuthority,
                                      final StakeAuthorize stakeAuthorize) {
    if (lockupAuthority == null) {
      return authorize(
          solanaAccounts, stakeAccount, stakeOrWithdrawAuthority,
          newAuthority, stakeAuthorize
      );
    }
    final var keys = List.of(
        createWrite(stakeAccount),
        solanaAccounts.readClockSysVar(),
        createReadOnlySigner(stakeOrWithdrawAuthority),
        createReadOnlySigner(lockupAuthority)
    );
    return authorize(solanaAccounts, keys, newAuthority, stakeAuthorize);
  }

  public static Instruction authorizeChecked(final SolanaAccounts solanaAccounts,
                                             final List<AccountMeta> keys,
                                             final StakeAuthorize stakeAuthorize) {
    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + stakeAuthorize.l()];
    Instructions.AuthorizeChecked.write(data);
    stakeAuthorize.write(data, NATIVE_DISCRIMINATOR_LENGTH);
    return Instruction.createInstruction(solanaAccounts.invokedStakeProgram(), keys, data);
  }

  public record AuthorizeChecked(byte[] discriminator, StakeAuthorize stakeAuthorize) {

    public static AuthorizeChecked read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static AuthorizeChecked read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      final var stakeAuthorize = StakeAuthorize.read(data, offset + discriminator.length);
      return new AuthorizeChecked(discriminator, stakeAuthorize);
    }
  }

  public static Instruction authorizeChecked(final SolanaAccounts solanaAccounts,
                                             final PublicKey stakeAccount,
                                             final PublicKey stakeOrWithdrawAuthority,
                                             final PublicKey newStakeOrWithdrawAuthority,
                                             final StakeAuthorize stakeAuthorize) {
    final var keys = List.of(
        createWrite(stakeAccount),
        solanaAccounts.readClockSysVar(),
        createReadOnlySigner(stakeOrWithdrawAuthority),
        createReadOnlySigner(newStakeOrWithdrawAuthority)
    );
    return authorizeChecked(solanaAccounts, keys, stakeAuthorize);
  }

  public static Instruction authorizeChecked(final SolanaAccounts solanaAccounts,
                                             final PublicKey stakeAccount,
                                             final PublicKey stakeOrWithdrawAuthority,
                                             final PublicKey newStakeOrWithdrawAuthority,
                                             final PublicKey lockupAuthority,
                                             final StakeAuthorize stakeAuthorize) {
    if (lockupAuthority == null) {
      return authorizeChecked(
          solanaAccounts, stakeAccount, stakeOrWithdrawAuthority, newStakeOrWithdrawAuthority,
          stakeAuthorize
      );
    }
    final var keys = List.of(
        createWrite(stakeAccount),
        solanaAccounts.readClockSysVar(),
        createReadOnlySigner(stakeOrWithdrawAuthority),
        createReadOnlySigner(newStakeOrWithdrawAuthority),
        createReadOnlySigner(lockupAuthority)
    );
    return authorizeChecked(solanaAccounts, keys, stakeAuthorize);
  }

  private static Instruction authorizeWithSeed(final SolanaAccounts solanaAccounts,
                                               final List<AccountMeta> keys,
                                               final PublicKey newAuthorizedPublicKey,
                                               final StakeAuthorize stakeAuthorize,
                                               final AccountWithSeed authoritySeed,
                                               final PublicKey authorityOwner) {
    final byte[] authoritySeedBytes = authoritySeed.asciiSeed();
    final byte[] data = new byte[
        NATIVE_DISCRIMINATOR_LENGTH
            + PUBLIC_KEY_LENGTH
            + stakeAuthorize.l()
            + (Long.BYTES + authoritySeedBytes.length)
            + PUBLIC_KEY_LENGTH
        ];
    int i = Instructions.AuthorizeWithSeed.write(data);
    i += newAuthorizedPublicKey.write(data, i);
    i += stakeAuthorize.write(data, i);
    i += SerdeUtil.writeString(authoritySeedBytes, data, i);
    authorityOwner.write(data, i);
    return Instruction.createInstruction(solanaAccounts.invokedStakeProgram(), keys, data);
  }

  public record AuthorizeWithSeed(byte[] discriminator,
                                  PublicKey newAuthorizedPublicKey,
                                  StakeAuthorize stakeAuthorize,
                                  byte[] seed,
                                  PublicKey authorityOwner) {

    public static AuthorizeWithSeed read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static AuthorizeWithSeed read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      int i = offset + discriminator.length;
      final var newAuthorizedPublicKey = PublicKey.readPubKey(data, i);
      i += PUBLIC_KEY_LENGTH;
      final var stakeAuthorize = StakeAuthorize.read(data, i);
      i += stakeAuthorize.l();
      final var seed = SerdeUtil.readString(data, i);
      i += Long.BYTES + seed.length;
      final var authorityOwner = PublicKey.readPubKey(data, i);

      return new AuthorizeWithSeed(discriminator, newAuthorizedPublicKey, stakeAuthorize, seed, authorityOwner);
    }
  }

  public static Instruction authorizeWithSeed(final SolanaAccounts solanaAccounts,
                                              final PublicKey stakeAccount,
                                              final AccountWithSeed baseKeyOrWithdrawAuthority,
                                              final PublicKey newAuthorizedPublicKey,
                                              final StakeAuthorize stakeAuthorize,
                                              final PublicKey authorityOwner) {
    final var keys = List.of(
        createWrite(stakeAccount),
        createReadOnlySigner(baseKeyOrWithdrawAuthority.publicKey()),
        solanaAccounts.readClockSysVar()
    );
    return authorizeWithSeed(solanaAccounts, keys, newAuthorizedPublicKey, stakeAuthorize, baseKeyOrWithdrawAuthority, authorityOwner);
  }

  public static Instruction authorizeWithSeed(final SolanaAccounts solanaAccounts,
                                              final PublicKey stakeAccount,
                                              final AccountWithSeed baseKeyOrWithdrawAuthority,
                                              final PublicKey lockupAuthority,
                                              final PublicKey newAuthorizedPublicKey,
                                              final StakeAuthorize stakeAuthorize,
                                              final PublicKey authorityOwner) {
    if (lockupAuthority == null) {
      return authorizeWithSeed(
          solanaAccounts, stakeAccount, baseKeyOrWithdrawAuthority,
          newAuthorizedPublicKey, stakeAuthorize, authorityOwner
      );
    }
    final var keys = List.of(
        createWrite(stakeAccount),
        createReadOnlySigner(baseKeyOrWithdrawAuthority.publicKey()),
        solanaAccounts.readClockSysVar(),
        createReadOnlySigner(lockupAuthority)
    );
    return authorizeWithSeed(solanaAccounts, keys, newAuthorizedPublicKey, stakeAuthorize, baseKeyOrWithdrawAuthority, authorityOwner);
  }

  public static Instruction authorizeCheckedWithSeed(final SolanaAccounts solanaAccounts,
                                                     final List<AccountMeta> keys,
                                                     final StakeAuthorize stakeAuthorize,
                                                     final AccountWithSeed authoritySeed,
                                                     final PublicKey authorityOwner) {
    final byte[] authoritySeedBytes = authoritySeed.asciiSeed();
    final byte[] data = new byte[
        NATIVE_DISCRIMINATOR_LENGTH
            + stakeAuthorize.l()
            + (Long.BYTES + authoritySeedBytes.length)
            + PUBLIC_KEY_LENGTH
        ];

    int i = Instructions.AuthorizeCheckedWithSeed.write(data);
    i += stakeAuthorize.write(data, i);
    i += SerdeUtil.writeString(authoritySeedBytes, data, i);
    authorityOwner.write(data, i);
    return Instruction.createInstruction(solanaAccounts.invokedStakeProgram(), keys, data);
  }

  public record AuthorizeCheckedWithSeed(byte[] discriminator,
                                         StakeAuthorize stakeAuthorize,
                                         byte[] seed,
                                         PublicKey authorityOwner) {

    public static AuthorizeCheckedWithSeed read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static AuthorizeCheckedWithSeed read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      int i = offset + discriminator.length;
      final var stakeAuthorize = StakeAuthorize.read(data, i);
      i += stakeAuthorize.l();
      final var seed = SerdeUtil.readString(data, i);
      i += Long.BYTES + seed.length;
      final var authorityOwner = PublicKey.readPubKey(data, i);

      return new AuthorizeCheckedWithSeed(discriminator, stakeAuthorize, seed, authorityOwner);
    }
  }

  public static Instruction authorizeCheckedWithSeed(final SolanaAccounts solanaAccounts,
                                                     final PublicKey stakeAccount,
                                                     final AccountWithSeed baseKeyOrWithdrawAuthority,
                                                     final PublicKey stakeOrWithdrawAuthority,
                                                     final StakeAuthorize stakeAuthorize,
                                                     final PublicKey authorityOwner) {
    final var keys = List.of(
        createWrite(stakeAccount),
        createReadOnlySigner(baseKeyOrWithdrawAuthority.publicKey()),
        solanaAccounts.readClockSysVar(),
        createReadOnlySigner(stakeOrWithdrawAuthority)
    );
    return authorizeCheckedWithSeed(solanaAccounts, keys, stakeAuthorize, baseKeyOrWithdrawAuthority, authorityOwner);
  }

  public static Instruction authorizeCheckedWithSeed(final SolanaAccounts solanaAccounts,
                                                     final PublicKey stakeAccount,
                                                     final AccountWithSeed baseKeyOrWithdrawAuthority,
                                                     final PublicKey stakeOrWithdrawAuthority,
                                                     final PublicKey lockupAuthority,
                                                     final StakeAuthorize stakeAuthorize,
                                                     final PublicKey authorityOwner) {
    if (lockupAuthority == null) {
      return authorizeCheckedWithSeed(
          solanaAccounts, stakeAccount, baseKeyOrWithdrawAuthority, stakeOrWithdrawAuthority,
          stakeAuthorize, authorityOwner
      );
    }
    final var keys = List.of(
        createWrite(stakeAccount),
        createReadOnlySigner(baseKeyOrWithdrawAuthority.publicKey()),
        solanaAccounts.readClockSysVar(),
        createReadOnlySigner(stakeOrWithdrawAuthority),
        createReadOnlySigner(lockupAuthority)
    );
    return authorizeCheckedWithSeed(solanaAccounts, keys, stakeAuthorize, baseKeyOrWithdrawAuthority, authorityOwner);
  }

  public static Instruction delegateStake(final SolanaAccounts solanaAccounts,
                                          final PublicKey initializedStakeAccount,
                                          final PublicKey validatorVoteAccount,
                                          final PublicKey stakeAuthority) {
    final var keys = List.of(
        createWrite(initializedStakeAccount),
        createRead(validatorVoteAccount),
        solanaAccounts.readClockSysVar(),
        solanaAccounts.readStakeHistorySysVar(),
        solanaAccounts.readStakeConfig(),
        createReadOnlySigner(stakeAuthority)
    );
    return Instruction.createInstruction(
        solanaAccounts.invokedStakeProgram(),
        keys,
        Instructions.DelegateStake.data
    );
  }

  public static Instruction split(final SolanaAccounts solanaAccounts,
                                  final PublicKey splitStakeAccount,
                                  final PublicKey unInitializedStakeAccount,
                                  final PublicKey stakeAuthority,
                                  final long lamports) {
    final var keys = List.of(
        createWrite(splitStakeAccount),
        createWrite(unInitializedStakeAccount),
        createReadOnlySigner(stakeAuthority)
    );

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + Long.BYTES];
    Instructions.Split.write(data);
    ByteUtil.putInt64LE(data, NATIVE_DISCRIMINATOR_LENGTH, lamports);

    return Instruction.createInstruction(solanaAccounts.invokedStakeProgram(), keys, data);
  }

  public record Split(byte[] discriminator, long lamports) {

    public static Split read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static Split read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      final var lamports = getInt64LE(data, offset + discriminator.length);
      return new Split(discriminator, lamports);
    }
  }

  public static Instruction withdraw(final SolanaAccounts solanaAccounts,
                                     final List<AccountMeta> keys,
                                     final long lamports) {
    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + Long.BYTES];
    Instructions.Withdraw.write(data);
    ByteUtil.putInt64LE(data, NATIVE_DISCRIMINATOR_LENGTH, lamports);

    return Instruction.createInstruction(solanaAccounts.invokedStakeProgram(), keys, data);
  }

  public record Withdraw(byte[] discriminator, long lamports) {

    public static Withdraw read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static Withdraw read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      final var lamports = getInt64LE(data, offset + discriminator.length);
      return new Withdraw(discriminator, lamports);
    }
  }

  public static Instruction withdraw(final SolanaAccounts solanaAccounts,
                                     final PublicKey stakeAccount,
                                     final PublicKey recipient,
                                     final PublicKey withdrawAuthority,
                                     final long lamports) {
    final var keys = List.of(
        createWrite(stakeAccount),
        createWrite(recipient),
        solanaAccounts.readClockSysVar(),
        solanaAccounts.readStakeHistorySysVar(),
        createReadOnlySigner(withdrawAuthority)
    );
    return withdraw(solanaAccounts, keys, lamports);
  }

  public static Instruction withdraw(final SolanaAccounts solanaAccounts,
                                     final PublicKey stakeAccount,
                                     final PublicKey recipient,
                                     final PublicKey withdrawAuthority,
                                     final PublicKey lockupAuthority,
                                     final long lamports) {
    if (lockupAuthority == null) {
      return withdraw(solanaAccounts, stakeAccount, recipient, withdrawAuthority, lamports);
    }
    final var keys = List.of(
        createWrite(stakeAccount),
        createWrite(recipient),
        solanaAccounts.readClockSysVar(),
        solanaAccounts.readStakeHistorySysVar(),
        createReadOnlySigner(withdrawAuthority),
        createReadOnlySigner(lockupAuthority)
    );
    return withdraw(solanaAccounts, keys, lamports);
  }

  public static Instruction deactivate(final SolanaAccounts solanaAccounts,
                                       final PublicKey delegatedStakeAccount,
                                       final PublicKey stakeAuthority) {
    final var keys = List.of(
        createWrite(delegatedStakeAccount),
        solanaAccounts.readClockSysVar(),
        createReadOnlySigner(stakeAuthority)
    );
    return Instruction.createInstruction(
        solanaAccounts.invokedStakeProgram(),
        keys,
        Instructions.Deactivate.data
    );
  }

  public static Instruction setLockup(final SolanaAccounts solanaAccounts,
                                      final PublicKey initializedStakeAccount,
                                      final PublicKey lockupOrWithdrawAuthority,
                                      final Instant timestamp,
                                      final OptionalLong epoch,
                                      final PublicKey custodian) {
    final var keys = List.of(
        createWrite(initializedStakeAccount),
        createReadOnlySigner(lockupOrWithdrawAuthority)
    );

    final byte[] data = new byte[
        NATIVE_DISCRIMINATOR_LENGTH
            + (timestamp == null ? 1 : 1 + Long.BYTES)
            + (epoch.isEmpty() ? 1 : 1 + Long.BYTES)
            + (custodian == null ? 1 : 1 + PUBLIC_KEY_LENGTH)
        ];

    int i = Instructions.SetLockup.write(data);
    i += SerdeUtil.writeOptionalEpochSeconds(timestamp, data, i);
    i += Borsh.writeOptional(epoch, data, i);
    Borsh.writeOptional(custodian, data, i);

    return Instruction.createInstruction(solanaAccounts.invokedStakeProgram(), keys, data);
  }

  public record SetLockup(byte[] discriminator, Instant timestamp, OptionalLong epoch, PublicKey custodian) {

    public static SetLockup read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static SetLockup read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      int i = offset + discriminator.length;
      final Instant timestamp;
      if (data[i++] == 1) {
        timestamp = Instant.ofEpochSecond(getInt64LE(data, i));
        i += Long.BYTES;
      } else {
        timestamp = null;
      }
      final OptionalLong epoch;
      if (data[i++] == 1) {
        epoch = OptionalLong.of(getInt64LE(data, i));
        i += Long.BYTES;
      } else {
        epoch = OptionalLong.empty();
      }
      final PublicKey custodian;
      if (data[i++] == 1) {
        custodian = PublicKey.readPubKey(data, i);
      } else {
        custodian = null;
      }
      return new SetLockup(discriminator, timestamp, epoch, custodian);
    }
  }

  public static Instruction setLockupChecked(final SolanaAccounts solanaAccounts,
                                             final List<AccountMeta> keys,
                                             final Instant timestamp,
                                             final OptionalLong epoch) {
    final byte[] data = new byte[
        NATIVE_DISCRIMINATOR_LENGTH
            + (timestamp == null ? 1 : 1 + Long.BYTES)
            + (epoch.isEmpty() ? 1 : 1 + Long.BYTES)
        ];

    int i = Instructions.SetLockupChecked.write(data);
    i += SerdeUtil.writeOptionalEpochSeconds(timestamp, data, i);
    Borsh.writeOptional(epoch, data, i);

    return Instruction.createInstruction(solanaAccounts.invokedStakeProgram(), keys, data);
  }

  public record SetLockupChecked(byte[] discriminator, Instant timestamp, OptionalLong epoch) {

    public static SetLockupChecked read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static SetLockupChecked read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      int i = offset + discriminator.length;
      final Instant timestamp;
      if (data[i++] == 1) {
        timestamp = Instant.ofEpochSecond(getInt64LE(data, i));
        i += Long.BYTES;
      } else {
        timestamp = null;
      }
      final OptionalLong epoch;
      if (data[i++] == 1) {
        epoch = OptionalLong.of(getInt64LE(data, i));
      } else {
        epoch = OptionalLong.empty();
      }
      return new SetLockupChecked(discriminator, timestamp, epoch);
    }
  }

  public static Instruction setLockupChecked(final SolanaAccounts solanaAccounts,
                                             final PublicKey initializedStakeAccount,
                                             final PublicKey lockupOrWithdrawAuthority,
                                             final Instant timestamp,
                                             final OptionalLong epoch) {
    final var keys = List.of(
        createWrite(initializedStakeAccount),
        createReadOnlySigner(lockupOrWithdrawAuthority)
    );
    return setLockupChecked(solanaAccounts, keys, timestamp, epoch);
  }

  public static Instruction setLockupChecked(final SolanaAccounts solanaAccounts,
                                             final PublicKey initializedStakeAccount,
                                             final PublicKey lockupOrWithdrawAuthority,
                                             final PublicKey newLockupAuthority,
                                             final Instant timestamp,
                                             final OptionalLong epoch) {
    if (newLockupAuthority == null) {
      return setLockupChecked(
          solanaAccounts, initializedStakeAccount, lockupOrWithdrawAuthority,
          timestamp, epoch
      );
    }
    final var keys = List.of(
        createWrite(initializedStakeAccount),
        createReadOnlySigner(lockupOrWithdrawAuthority),
        createReadOnlySigner(newLockupAuthority)
    );
    return setLockupChecked(solanaAccounts, keys, timestamp, epoch);
  }

  public static Instruction merge(final SolanaAccounts solanaAccounts,
                                  final PublicKey destinationStakeAccount,
                                  final PublicKey srcStakeAccount,
                                  final PublicKey stakeAuthority) {
    final var keys = List.of(
        createWrite(destinationStakeAccount),
        createWrite(srcStakeAccount),
        solanaAccounts.readClockSysVar(),
        solanaAccounts.readStakeHistorySysVar(),
        createReadOnlySigner(stakeAuthority)
    );

    return Instruction.createInstruction(solanaAccounts.invokedStakeProgram(), keys, Instructions.Merge.data);
  }

  public static Instruction deactivateDelinquent(final SolanaAccounts solanaAccounts,
                                                 final PublicKey delegatedStakeAccount,
                                                 final PublicKey delinquentVoteAccount,
                                                 final PublicKey referenceVoteAccount) {
    final var keys = List.of(
        createWrite(delegatedStakeAccount),
        createRead(delinquentVoteAccount),
        createRead(referenceVoteAccount)
    );
    return Instruction.createInstruction(
        solanaAccounts.invokedStakeProgram(),
        keys,
        Instructions.DeactivateDelinquent.data
    );
  }

  public static Instruction reDelegate(final SolanaAccounts solanaAccounts,
                                       final PublicKey delegatedStakeAccount,
                                       final PublicKey uninitializedStakeAccount,
                                       final PublicKey validatorVoteAccount,
                                       final PublicKey stakeAuthority) {
    final var keys = List.of(
        createWrite(delegatedStakeAccount),
        createWrite(uninitializedStakeAccount),
        createRead(validatorVoteAccount),
        solanaAccounts.readStakeConfig(),
        createReadOnlySigner(stakeAuthority)
    );
    return Instruction.createInstruction(
        solanaAccounts.invokedStakeProgram(),
        keys,
        Instructions.Redelegate.data
    );
  }

  public static Instruction moveStake(final SolanaAccounts solanaAccounts,
                                      final PublicKey sourceStakeAccount,
                                      final PublicKey destinationStakeAccount,
                                      final PublicKey stakeAuthority,
                                      final long lamports) {
    final var keys = List.of(
        createWrite(sourceStakeAccount),
        createWrite(destinationStakeAccount),
        createReadOnlySigner(stakeAuthority)
    );

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + Long.BYTES];
    Instructions.MoveStake.write(data);
    ByteUtil.putInt64LE(data, NATIVE_DISCRIMINATOR_LENGTH, lamports);

    return Instruction.createInstruction(solanaAccounts.invokedStakeProgram(), keys, data);
  }

  public record MoveStake(byte[] discriminator, long lamports) {

    public static MoveStake read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static MoveStake read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      final var lamports = getInt64LE(data, offset + discriminator.length);
      return new MoveStake(discriminator, lamports);
    }
  }

  public static Instruction moveLamports(final SolanaAccounts solanaAccounts,
                                         final PublicKey sourceStakeAccount,
                                         final PublicKey destinationStakeAccount,
                                         final PublicKey stakeAuthority,
                                         final long lamports) {
    final var keys = List.of(
        createWrite(sourceStakeAccount),
        createWrite(destinationStakeAccount),
        createReadOnlySigner(stakeAuthority)
    );

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + Long.BYTES];
    Instructions.MoveLamports.write(data);
    ByteUtil.putInt64LE(data, NATIVE_DISCRIMINATOR_LENGTH, lamports);

    return Instruction.createInstruction(solanaAccounts.invokedStakeProgram(), keys, data);
  }

  public record MoveLamports(byte[] discriminator, long lamports) {

    public static MoveLamports read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static MoveLamports read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      final var lamports = getInt64LE(data, offset + discriminator.length);
      return new MoveLamports(discriminator, lamports);
    }
  }

  private StakeProgram() {
  }
}
