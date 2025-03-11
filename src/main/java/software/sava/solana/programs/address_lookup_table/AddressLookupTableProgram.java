package software.sava.solana.programs.address_lookup_table;

import software.sava.core.accounts.ProgramDerivedAddress;
import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.encoding.ByteUtil;
import software.sava.core.programs.Discriminator;
import software.sava.core.tx.Instruction;

import java.util.List;

import static software.sava.core.accounts.PublicKey.PUBLIC_KEY_LENGTH;
import static software.sava.core.accounts.meta.AccountMeta.*;
import static software.sava.core.encoding.ByteUtil.putInt64LE;
import static software.sava.core.programs.Discriminator.NATIVE_DISCRIMINATOR_LENGTH;
import static software.sava.core.programs.Discriminator.serializeDiscriminator;
import static software.sava.core.tx.Instruction.createInstruction;

// https://github.com/solana-program/address-lookup-table
public final class AddressLookupTableProgram {

  public enum Instructions implements Discriminator {

    // Create an address lookup table
    //
    // # Account references
    //   0. '[WRITE]' Uninitialized address lookup table account
    //   1. '[SIGNER]' Account used to derive and control the new address lookup table.
    //   2. '[SIGNER, WRITE]' Account that will fund the new address lookup table.
    //   3. '[]' System program for CPI.
    CreateLookupTable {
      // A recent slot must be used in the derivation path
      // for each initialized table. When closing table accounts,
      // the initialization slot must no longer be "recent" to prevent
      // address tables from being recreated with reordered or
      // otherwise malicious addresses.
//      recent_slot: Slot,
//      // Address tables are always initialized at program-derived
//      // addresses using the funding address, recent blockhash, and
//      // the user-passed 'bump_seed'.
//      bump_seed: u8,
    },

    // Permanently freeze an address lookup table, making it immutable.
    //
    // # Account references
    //   0. '[WRITE]' Address lookup table account to freeze
    //   1. '[SIGNER]' Current authority
    FreezeLookupTable,

    // Extend an address lookup table with new addresses. Funding account and
    // system program account references are only required if the lookup table
    // account requires additional lamports to cover the rent-exempt balance
    // after being extended.
    //
    // # Account references
    //   0. '[WRITE]' Address lookup table account to extend
    //   1. '[SIGNER]' Current authority
    //   2. '[SIGNER, WRITE, OPTIONAL]' Account that will fund the table reallocation
    //   3. '[OPTIONAL]' System program for CPI.
    ExtendLookupTable {
//      new_addresses: Vec<Pubkey>
    },

    // Deactivate an address lookup table, making it unusable and
    // eligible for closure after a short period of time.
    //
    // # Account references
    //   0. '[WRITE]' Address lookup table account to deactivate
    //   1. '[SIGNER]' Current authority
    DeactivateLookupTable,

    // Close an address lookup table account
    //
    // # Account references
    //   0. '[WRITE]' Address lookup table account to close
    //   1. '[SIGNER]' Current authority
    //   2. '[WRITE]' Recipient of closed account lamports
    CloseLookupTable;

    private final byte[] data;

    Instructions() {
      this.data = serializeDiscriminator(this);
    }

    public byte[] data() {
      return this.data;
    }
  }

  public static ProgramDerivedAddress findLookupTableAddress(final SolanaAccounts solanaAccounts,
                                                             final PublicKey authorityAccount,
                                                             final long recentSlot) {
    final byte[] recentSlotBytes = new byte[Long.BYTES];
    ByteUtil.putInt64LE(recentSlotBytes, 0, recentSlot);
    return PublicKey.findProgramAddress(List.of(
        authorityAccount.toByteArray(),
        recentSlotBytes
    ), solanaAccounts.addressLookupTableProgram());
  }

  public static Instruction createLookupTable(final SolanaAccounts solanaAccounts,
                                              final PublicKey uninitializedTableAccount,
                                              final PublicKey baseAndAuthorityKey,
                                              final PublicKey funderAccount,
                                              final long recentSlot,
                                              final int bumpSeed) {
    final var keys = List.of(
        createWrite(uninitializedTableAccount),
        createReadOnlySigner(baseAndAuthorityKey),
        createWritableSigner(funderAccount),
        solanaAccounts.readSystemProgram()
    );

    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + Long.BYTES + 1];
    Instructions.CreateLookupTable.write(data, 0);
    putInt64LE(data, NATIVE_DISCRIMINATOR_LENGTH, recentSlot);
    data[NATIVE_DISCRIMINATOR_LENGTH + Long.BYTES] = (byte) bumpSeed;

    return createInstruction(solanaAccounts.invokedAddressLookupTableProgram(), keys, data);
  }

  public static Instruction freezeLookupTable(final SolanaAccounts solanaAccounts,
                                              final PublicKey tableAccount,
                                              final PublicKey authorityAccount) {
    final var keys = List.of(
        createWrite(tableAccount),
        createReadOnlySigner(authorityAccount)
    );

    return createInstruction(solanaAccounts.invokedAddressLookupTableProgram(), keys, Instructions.FreezeLookupTable.data);
  }

  private static byte[] createExtendTableData(final List<PublicKey> newAddresses) {
    final byte[] data = new byte[NATIVE_DISCRIMINATOR_LENGTH + Long.BYTES + (PUBLIC_KEY_LENGTH * newAddresses.size())];
    Instructions.ExtendLookupTable.write(data, 0);
    ByteUtil.putInt64LE(data, NATIVE_DISCRIMINATOR_LENGTH, newAddresses.size());
    int i = NATIVE_DISCRIMINATOR_LENGTH + Long.BYTES;
    for (final var a : newAddresses) {
      i += a.write(data, i);
    }
    return data;
  }

  public static Instruction extendLookupTable(final SolanaAccounts solanaAccounts,
                                              final PublicKey tableAccount,
                                              final PublicKey authorityAccount,
                                              final List<PublicKey> newAddresses) {
    final var keys = List.of(
        createWrite(tableAccount),
        createReadOnlySigner(authorityAccount)
    );
    return createInstruction(solanaAccounts.invokedAddressLookupTableProgram(), keys, createExtendTableData(newAddresses));
  }

  public static Instruction extendLookupTable(final SolanaAccounts solanaAccounts,
                                              final PublicKey tableAccount,
                                              final PublicKey authorityAccount,
                                              final PublicKey funderAccount,
                                              final List<PublicKey> newAddresses) {
    final var keys = List.of(
        createWrite(tableAccount),
        createReadOnlySigner(authorityAccount),
        createWritableSigner(funderAccount),
        solanaAccounts.readSystemProgram()
    );
    return createInstruction(solanaAccounts.invokedAddressLookupTableProgram(), keys, createExtendTableData(newAddresses));
  }

  public static Instruction deactivateLookupTable(final SolanaAccounts solanaAccounts,
                                                  final PublicKey tableAccount,
                                                  final PublicKey authorityAccount) {
    final var keys = List.of(
        createWrite(tableAccount),
        createReadOnlySigner(authorityAccount)
    );

    return createInstruction(solanaAccounts.invokedAddressLookupTableProgram(), keys, Instructions.DeactivateLookupTable.data);
  }

  public static Instruction closeLookupTable(final SolanaAccounts solanaAccounts,
                                             final PublicKey tableAccount,
                                             final PublicKey authorityAccount,
                                             final PublicKey lamportRecipient) {
    final var keys = List.of(
        createWrite(tableAccount),
        createReadOnlySigner(authorityAccount),
        createWrite(lamportRecipient)
    );

    return createInstruction(solanaAccounts.invokedAddressLookupTableProgram(), keys, Instructions.CloseLookupTable.data);
  }

  private AddressLookupTableProgram() {
  }
}
