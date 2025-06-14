package software.sava.solana.programs.system;

import software.sava.core.accounts.AccountWithSeed;
import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.encoding.ByteUtil;
import software.sava.core.programs.Discriminator;
import software.sava.core.tx.Instruction;
import software.sava.solana.programs.serde.SerdeUtil;

import java.util.List;

import static software.sava.core.accounts.PublicKey.PUBLIC_KEY_LENGTH;
import static software.sava.core.accounts.meta.AccountMeta.*;
import static software.sava.core.encoding.ByteUtil.getInt64LE;
import static software.sava.core.encoding.ByteUtil.putInt64LE;
import static software.sava.core.programs.Discriminator.NATIVE_DISCRIMINATOR_LENGTH;
import static software.sava.core.programs.Discriminator.serializeDiscriminator;
import static software.sava.core.tx.Instruction.createInstruction;
import static software.sava.solana.programs.serde.SerdeUtil.readDiscriminator;

// https://github.com/solana-labs/solana-program-library?tab=readme-ov-file#migrated-packages
// https://github.com/anza-xyz/pinocchio/tree/main/programs
// https://github.com/solana-labs/solana/blob/master/sdk/program/src/system_instruction.rs
// https://github.com/solana-program/system
public final class SystemProgram {

  public enum Instructions implements Discriminator {
    // Create a new account
    //
    // # Account references
    //   0. '[WRITE, SIGNER]' Funding account
    //   1. '[WRITE, SIGNER]' New account
    CreateAccount {
      // Number of lamports to transfer to the new account
//      lamports: u64,
//      // Number of bytes of memory to allocate
//      space: u64,
//      // Address of program that will own the new account
//      owner: Pubkey,
    },

    // Assign account to a program
    //
    // # Account references
    //   0. '[WRITE, SIGNER]' Assigned account public key
    Assign {
      // Owner program account
//      owner: Pubkey,
    },

    // Transfer lamports
    //
    // # Account references
    //   0. '[WRITE, SIGNER]' Funding account
    //   1. '[WRITE]' Recipient account
    Transfer {
//      lamports: u64
    },

    // Create a new account at an address derived from a base pubkey and a seed
    //
    // # Account references
    //   0. '[WRITE, SIGNER]' Funding account
    //   1. '[WRITE]' Created account
    //   2. '[SIGNER]' (optional) Base account; the account matching the base Pubkey below must be
    //                          provided as a signer, but may be the same as the funding account
    //                          and provided as account 0
    CreateAccountWithSeed {
      // Base public key
//      base: Pubkey,
//
//      // String of ASCII chars, no longer than 'Pubkey::MAX_SEED_LEN'
//      seed: String,
//
//      // Number of lamports to transfer to the new account
//      lamports: u64,
//
//      // Number of bytes of memory to allocate
//      space: u64,
//
//      // Owner program account address
//      owner: Pubkey,
    },

    // Consumes a stored nonce, replacing it with a successor
    //
    // # Account references
    //   0. '[WRITE]' Nonce account
    //   1. '[]' RecentBlockhashes sysvar
    //   2. '[SIGNER]' Nonce authority
    AdvanceNonceAccount,

    // Withdraw funds from a nonce account
    //
    // # Account references
    //   0. '[WRITE]' Nonce account
    //   1. '[WRITE]' Recipient account
    //   2. '[]' RecentBlockhashes sysvar
    //   3. '[]' Rent sysvar
    //   4. '[SIGNER]' Nonce authority
    //
    // The 'u64' parameter is the lamports to withdraw, which must leave the
    // account balance above the rent exempt reserve or at zero.
    WithdrawNonceAccount(
//        u64
    ),

    // Drive state of Uninitialized nonce account to Initialized, setting the nonce value
    //
    // # Account references
    //   0. '[WRITE]' Nonce account
    //   1. '[]' RecentBlockhashes sysvar
    //   2. '[]' Rent sysvar
    //
    // The 'Pubkey' parameter specifies the entity authorized to execute nonce
    // instruction on the account
    //
    // No signatures are required to execute this instruction, enabling derived
    // nonce account addresses
    InitializeNonceAccount(
//        Pubkey
    ),

    // Change the entity authorized to execute nonce instructions on the account
    //
    // # Account references
    //   0. '[WRITE]' Nonce account
    //   1. '[SIGNER]' Nonce authority
    //
    // The 'Pubkey' parameter identifies the entity to authorize
    AuthorizeNonceAccount(
//        Pubkey
    ),

    // Allocate space in a (possibly new) account without funding
    //
    // # Account references
    //   0. '[WRITE, SIGNER]' New account
    Allocate {
      // Number of bytes of memory to allocate
//      space: u64,
    },

    // Allocate space for and assign an account at an address
    //    derived from a base public key and a seed
    //
    // # Account references
    //   0. '[WRITE]' Allocated account
    //   1. '[SIGNER]' Base account
    AllocateWithSeed {
      // Base public key
//      base: Pubkey,
//
//      // String of ASCII chars, no longer than 'pubkey::MAX_SEED_LEN'
//      seed: String,
//
//      // Number of bytes of memory to allocate
//      space: u64,
//
//      // Owner program account
//      owner: Pubkey,
    },

    // Assign account to a program based on a seed
    //
    // # Account references
    //   0. '[WRITE]' Assigned account
    //   1. '[SIGNER]' Base account
    AssignWithSeed {
      // Base public key
//      base: Pubkey,
//
//      // String of ASCII chars, no longer than 'pubkey::MAX_SEED_LEN'
//      seed: String,
//
//      // Owner program account
//      owner: Pubkey,
    },

    // Transfer lamports from a derived address
    //
    // # Account references
    //   0. '[WRITE]' Funding account
    //   1. '[SIGNER]' Base for funding account
    //   2. '[WRITE]' Recipient account
    TransferWithSeed {
      // Amount to transfer
//      lamports: u64,
//
//      // Seed to use to derive the funding account address
//      from_seed: String,
//
//      // Owner to use to derive the funding account address
//      from_owner: Pubkey,
    },

    // One-time idempotent upgrade of legacy nonce versions in order to bump
    // them out of chain blockhash domain.
    //
    // # Account references
    //   0. '[WRITE]' Nonce account
    UpgradeNonceAccount;

    private final byte[] data;

    Instructions() {
      this.data = serializeDiscriminator(this);
    }

    public byte[] data() {
      return this.data;
    }
  }

  public static Instruction allocate(final AccountMeta invokedProgram,
                                     final PublicKey newAccount,
                                     final long space) {
    final var keys = List.of(createWritableSigner(newAccount));

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + Long.BYTES];
    Instructions.Allocate.write(data);
    putInt64LE(data, NATIVE_DISCRIMINATOR_LENGTH, space);

    return createInstruction(invokedProgram, keys, data);
  }

  public record Allocate(byte[] discriminator, long space) {

    public static Allocate read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static Allocate read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      final var space = getInt64LE(data, offset + discriminator.length);
      return new Allocate(discriminator, space);
    }
  }

  public static Instruction allocateWithSeed(final AccountMeta invokedProgram,
                                             final AccountWithSeed accountWithSeed,
                                             final long space,
                                             final PublicKey programOwner) {
    final var baseAccount = accountWithSeed.baseKey();
    final var keys = List.of(
        createWrite(accountWithSeed.publicKey()),
        createReadOnlySigner(baseAccount)
    );

    final byte[] seedBytes = accountWithSeed.asciiSeed();
    final byte[] data = new byte[
        NATIVE_DISCRIMINATOR_LENGTH
            + PUBLIC_KEY_LENGTH
            + (Long.BYTES + seedBytes.length)
            + Long.BYTES
            + PUBLIC_KEY_LENGTH
        ];
    int i = Instructions.AllocateWithSeed.write(data);
    i += baseAccount.write(data, i);
    i += SerdeUtil.writeString(seedBytes, data, i);
    putInt64LE(data, i, space);
    i += Long.BYTES;
    programOwner.write(data, i);

    return createInstruction(invokedProgram, keys, data);
  }

  public record AllocateWithSeed(byte[] discriminator,
                                 PublicKey baseAccount,
                                 byte[] seed,
                                 long space,
                                 PublicKey programOwner) {

    public static AllocateWithSeed read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static AllocateWithSeed read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = readDiscriminator(data, offset);
      int i = offset + discriminator.length;
      final var baseAccount = PublicKey.readPubKey(data, i);
      i += PUBLIC_KEY_LENGTH;
      final var seed = SerdeUtil.readString(data, i);
      i += Long.BYTES + seed.length;
      final long space = ByteUtil.getInt64LE(data, i);
      i += Long.BYTES;
      final var programOwner = PublicKey.readPubKey(data, i);

      return new AllocateWithSeed(discriminator, baseAccount, seed, space, programOwner);
    }
  }

  public static Instruction assign(final AccountMeta invokedProgram,
                                   final PublicKey newAccount,
                                   final PublicKey programOwner) {
    final var keys = List.of(createWritableSigner(newAccount));

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + PUBLIC_KEY_LENGTH];
    Instructions.Assign.write(data);
    programOwner.write(data, NATIVE_DISCRIMINATOR_LENGTH);

    return createInstruction(invokedProgram, keys, data);
  }

  public record Assign(byte[] discriminator, PublicKey programOwner) {

    public static Assign read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static Assign read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      return new Assign(discriminator, PublicKey.readPubKey(data, offset + discriminator.length));
    }
  }

  public static Instruction assignWithSeed(final AccountMeta invokedProgram,
                                           final AccountWithSeed accountWithSeed,
                                           final PublicKey baseAccount,
                                           final PublicKey programOwner) {
    final var keys = List.of(createWrite(accountWithSeed.publicKey()), createReadOnlySigner(baseAccount));

    final byte[] seedBytes = accountWithSeed.asciiSeed();
    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + PUBLIC_KEY_LENGTH + (Long.BYTES + seedBytes.length) + PUBLIC_KEY_LENGTH];
    int i = Instructions.AssignWithSeed.write(data);
    i += baseAccount.write(data, i);
    i += SerdeUtil.writeString(seedBytes, data, i);
    programOwner.write(data, i);

    return createInstruction(invokedProgram, keys, data);
  }

  public record AssignWithSeed(byte[] discriminator,
                               PublicKey baseAccount,
                               byte[] seed,
                               PublicKey programOwner) {

    public static AssignWithSeed read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static AssignWithSeed read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      int i = offset + discriminator.length;
      final var baseAccount = PublicKey.readPubKey(data, i);
      i += PUBLIC_KEY_LENGTH;
      final var seed = SerdeUtil.readString(data, i);
      i += Long.BYTES + seed.length;
      final var programOwner = PublicKey.readPubKey(data, i);
      return new AssignWithSeed(discriminator, baseAccount, seed, programOwner);
    }
  }

  public static Instruction createAccount(final AccountMeta invokedProgram,
                                          final PublicKey fromPublicKey,
                                          final PublicKey newAccountPublicKey,
                                          final long lamports,
                                          final long space,
                                          final PublicKey programOwner) {
    final var keys = List.of(createWritableSigner(fromPublicKey), createWritableSigner(newAccountPublicKey));

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + Long.BYTES + Long.BYTES + PUBLIC_KEY_LENGTH];
    int i = Instructions.CreateAccount.write(data);
    putInt64LE(data, i, lamports);
    i += Long.BYTES;
    putInt64LE(data, i, space);
    i += Long.BYTES;
    programOwner.write(data, i);

    return createInstruction(invokedProgram, keys, data);
  }

  public record CreateAccount(byte[] discriminator,
                              long lamports,
                              long space,
                              PublicKey programOwner) {

    public static CreateAccount read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static CreateAccount read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      int i = offset + discriminator.length;
      final long lamports = ByteUtil.getInt64LE(data, i);
      i += Long.BYTES;
      final long space = ByteUtil.getInt64LE(data, i);
      i += Long.BYTES;
      final var programOwner = PublicKey.readPubKey(data, i);
      return new CreateAccount(discriminator, lamports, space, programOwner);
    }
  }

  public static Instruction createAccountWithSeed(final AccountMeta invokedProgram,
                                                  final PublicKey fromPublicKey,
                                                  final AccountWithSeed accountWithSeed,
                                                  final long lamports,
                                                  final long space,
                                                  final PublicKey programOwner) {
    final var fromSigner = createWritableSigner(fromPublicKey);
    final var accountMeta = createWrite(accountWithSeed.publicKey());
    final var baseAccount = accountWithSeed.baseKey();
    final byte[] seedBytes = accountWithSeed.asciiSeed();
    final var keys = baseAccount.equals(fromPublicKey)
        ? List.of(fromSigner, accountMeta)
        : List.of(fromSigner, accountMeta, createReadOnlySigner(baseAccount));

    final byte[] data = new byte[
        NATIVE_DISCRIMINATOR_LENGTH
            + PUBLIC_KEY_LENGTH
            + (Long.BYTES + seedBytes.length)
            + Long.BYTES
            + Long.BYTES
            + PUBLIC_KEY_LENGTH
        ];
    int i = Instructions.CreateAccountWithSeed.write(data);
    baseAccount.write(data, i);
    i += PUBLIC_KEY_LENGTH;
    i += SerdeUtil.writeString(seedBytes, data, i);
    putInt64LE(data, i, lamports);
    i += Long.BYTES;
    putInt64LE(data, i, space);
    i += Long.BYTES;
    programOwner.write(data, i);

    return createInstruction(invokedProgram, keys, data);
  }

  public record CreateAccountWithSeed(byte[] discriminator,
                                      PublicKey baseAccount,
                                      byte[] seed,
                                      long lamports,
                                      long space,
                                      PublicKey programOwner) {

    public static CreateAccountWithSeed read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static CreateAccountWithSeed read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      int i = offset + discriminator.length;
      final var baseAccount = PublicKey.readPubKey(data, i);
      i += PUBLIC_KEY_LENGTH;
      final var seed = SerdeUtil.readString(data, i);
      i += Long.BYTES + seed.length;
      final long lamports = ByteUtil.getInt64LE(data, i);
      i += Long.BYTES;
      final long space = ByteUtil.getInt64LE(data, i);
      i += Long.BYTES;
      final var programOwner = PublicKey.readPubKey(data, i);
      return new CreateAccountWithSeed(discriminator, baseAccount, seed, lamports, space, programOwner);
    }
  }

  public static Instruction transfer(final AccountMeta invokedProgram,
                                     final PublicKey fromPublicKey,
                                     final PublicKey toPublicKey,
                                     final long lamports) {
    final var keys = List.of(
        createWritableSigner(fromPublicKey),
        createWrite(toPublicKey)
    );

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + Long.BYTES];
    Instructions.Transfer.write(data);
    putInt64LE(data, NATIVE_DISCRIMINATOR_LENGTH, lamports);

    return createInstruction(invokedProgram, keys, data);
  }

  public record Transfer(byte[] discriminator, long lamports) {

    public static Transfer read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static Transfer read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      return new Transfer(discriminator, ByteUtil.getInt64LE(data, offset + discriminator.length));
    }
  }

  public static Instruction transferWithSeed(final AccountMeta invokedProgram,
                                             final AccountWithSeed accountWithSeed,
                                             final PublicKey recipientAccount,
                                             final long lamports,
                                             final PublicKey programOwner) {
    final var keys = List.of(
        createWrite(accountWithSeed.publicKey()),
        createReadOnlySigner(accountWithSeed.baseKey()),
        createWrite(recipientAccount)
    );

    final byte[] seedBytes = accountWithSeed.asciiSeed();
    final byte[] data = new byte[Integer.BYTES + Long.BYTES + (Long.BYTES + seedBytes.length) + PUBLIC_KEY_LENGTH];
    int i = Instructions.TransferWithSeed.write(data);
    putInt64LE(data, i, lamports);
    i += Long.BYTES;
    i += SerdeUtil.writeString(seedBytes, data, i);
    programOwner.write(data, i);

    return createInstruction(invokedProgram, keys, data);
  }

  public record TransferWithSeed(byte[] discriminator,
                                 long lamports,
                                 byte[] seed,
                                 PublicKey programOwner) {

    public static TransferWithSeed read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static TransferWithSeed read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      int i = offset + discriminator.length;
      final long lamports = ByteUtil.getInt64LE(data, i);
      i += Long.BYTES;
      final var seed = SerdeUtil.readString(data, i);
      i += Long.BYTES + seed.length;
      final var programOwner = PublicKey.readPubKey(data, i);
      return new TransferWithSeed(discriminator, lamports, seed, programOwner);
    }
  }

  public static Instruction advanceNonceAccount(final SolanaAccounts solanaAccounts,
                                                final PublicKey nonceAccount,
                                                final PublicKey nonceAuthority) {
    final var keys = List.of(
        createWrite(nonceAccount),
        solanaAccounts.readRecentBlockhashesSysVar(),
        createReadOnlySigner(nonceAuthority)
    );

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH];
    Instructions.AdvanceNonceAccount.write(data);

    return createInstruction(solanaAccounts.invokedSystemProgram(), keys, data);
  }

  public static Instruction withdrawNonceAccount(final SolanaAccounts solanaAccounts,
                                                 final PublicKey nonceAccount,
                                                 final PublicKey recipient,
                                                 final PublicKey nonceAuthority,
                                                 final long lamports) {
    final var keys = List.of(
        createWrite(nonceAccount),
        createWrite(recipient),
        solanaAccounts.readRecentBlockhashesSysVar(),
        solanaAccounts.readRentSysVar(),
        createReadOnlySigner(nonceAuthority)
    );

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + Long.BYTES];
    Instructions.WithdrawNonceAccount.write(data);
    putInt64LE(data, NATIVE_DISCRIMINATOR_LENGTH, lamports);

    return createInstruction(solanaAccounts.invokedSystemProgram(), keys, data);
  }

  public record WithdrawNonceAccount(byte[] discriminator, long lamports) {

    public static WithdrawNonceAccount read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static WithdrawNonceAccount read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      return new WithdrawNonceAccount(discriminator, ByteUtil.getInt64LE(data, offset + discriminator.length));
    }
  }

  public static Instruction initializeNonceAccount(final SolanaAccounts solanaAccounts,
                                                   final PublicKey nonceAccount,
                                                   final PublicKey nonceAuthority) {
    final var keys = List.of(
        createWrite(nonceAccount),
        solanaAccounts.readRecentBlockhashesSysVar(),
        solanaAccounts.readRentSysVar()
    );

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + PUBLIC_KEY_LENGTH];
    Instructions.InitializeNonceAccount.write(data);
    nonceAuthority.write(data, NATIVE_DISCRIMINATOR_LENGTH);

    return createInstruction(solanaAccounts.invokedSystemProgram(), keys, data);
  }

  public record InitializeNonceAccount(byte[] discriminator, PublicKey nonceAuthority) {

    public static InitializeNonceAccount read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static InitializeNonceAccount read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      return new InitializeNonceAccount(discriminator, PublicKey.readPubKey(data, offset + discriminator.length));
    }
  }

  public static Instruction authorizeNonceAccount(final AccountMeta invokedProgram,
                                                  final PublicKey nonceAccount,
                                                  final PublicKey currentNonceAuthority,
                                                  final PublicKey newNonceAuthority) {
    final var keys = List.of(
        createWrite(nonceAccount),
        createReadOnlySigner(currentNonceAuthority)
    );

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + PUBLIC_KEY_LENGTH];
    Instructions.AuthorizeNonceAccount.write(data);
    newNonceAuthority.write(data, NATIVE_DISCRIMINATOR_LENGTH);

    return createInstruction(invokedProgram, keys, data);
  }

  public record AuthorizeNonceAccount(byte[] discriminator, PublicKey newNonceAuthority) {

    public static AuthorizeNonceAccount read(final Instruction instruction) {
      return read(instruction.data(), instruction.offset());
    }

    public static AuthorizeNonceAccount read(final byte[] data, final int offset) {
      if (data == null || data.length == 0) {
        return null;
      }
      final var discriminator = SerdeUtil.readDiscriminator(data, offset);
      return new AuthorizeNonceAccount(discriminator, PublicKey.readPubKey(data, offset + discriminator.length));
    }
  }

  public static Instruction authorizeNonceAccount(final SolanaAccounts solanaAccounts,
                                                  final PublicKey nonceAccount,
                                                  final PublicKey currentNonceAuthority,
                                                  final PublicKey newNonceAuthority) {
    return authorizeNonceAccount(
        solanaAccounts.invokedSystemProgram(),
        nonceAccount,
        currentNonceAuthority,
        newNonceAuthority
    );
  }

  public static Instruction upgradeNonceAccount(final AccountMeta invokedProgram, final PublicKey nonceAccount) {
    final var keys = List.of(createWrite(nonceAccount));

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH];
    Instructions.UpgradeNonceAccount.write(data);

    return createInstruction(invokedProgram, keys, data);
  }

  public static Instruction upgradeNonceAccount(final SolanaAccounts solanaAccounts, final PublicKey nonceAccount) {
    return upgradeNonceAccount(solanaAccounts.invokedSystemProgram(), nonceAccount);
  }

  private SystemProgram() {
  }
}
