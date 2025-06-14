package software.sava.solana.programs.token;

import software.sava.core.accounts.ProgramDerivedAddress;
import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.programs.Discriminator;
import software.sava.core.tx.Instruction;

import java.util.List;

import static software.sava.core.accounts.meta.AccountMeta.*;

// https://github.com/solana-program/associated-token-account/blob/main/program/src/instruction.rs
public final class AssociatedTokenProgram {

  public enum Instructions implements Discriminator {

    // Creates an associated token account for the given wallet address and
    // token mint Returns an error if the account exists.
    //
    //   0. '[writeable,signer]' Funding account (must be a system account)
    //   1. '[writeable]' Associated token account address to be created
    //   2. '[]' Wallet address for the new associated token account
    //   3. '[]' The token mint for the new associated token account
    //   4. '[]' System program
    //   5. '[]' SPL Token program
    Create,
    // Creates an associated token account for the given wallet address and
    // token mint, if it doesn't already exist.  Returns an error if the
    // account exists, but with a different owner.
    //
    //   0. '[writeable,signer]' Funding account (must be a system account)
    //   1. '[writeable]' Associated token account address to be created
    //   2. '[]' Wallet address for the new associated token account
    //   3. '[]' The token mint for the new associated token account
    //   4. '[]' System program
    //   5. '[]' SPL Token program
    CreateIdempotent,
    // Transfers from and closes a nested associated token account: an
    // associated token account owned by an associated token account.
    //
    // The tokens are moved from the nested associated token account to the
    // wallet's associated token account, and the nested account lamports are
    // moved to the wallet.
    //
    // Note: Nested token accounts are an anti-pattern, and almost always
    // created unintentionally, so this instruction should only be used to
    // recover from errors.
    //
    //   0. '[writeable]' Nested associated token account, must be owned by '3'
    //   1. '[]' Token mint for the nested associated token account
    //   2. '[writeable]' Wallet's associated token account
    //   3. '[]' Owner associated token account address, must be owned by '5'
    //   4. '[]' Token mint for the owner associated token account
    //   5. '[writeable, signer]' Wallet address for the owner associated token
    //      account
    //   6. '[]' SPL Token program
    RecoverNested;

    private final byte[] discriminatorBytes;

    Instructions() {
      this.discriminatorBytes = new byte[]{(byte) this.ordinal()};
    }

    @Override
    public byte[] data() {
      return discriminatorBytes;
    }

    @Override
    public int write(final byte[] bytes, final int i) {
      bytes[i] = (byte) this.ordinal();
      return 1;
    }

    @Override
    public int length() {
      return 1;
    }
  }

  public static ProgramDerivedAddress findATA(final SolanaAccounts solanaAccounts,
                                              final PublicKey owner,
                                              final PublicKey tokenProgram,
                                              final PublicKey mint) {
    return PublicKey.findProgramAddress(List.of(
            owner.toByteArray(),
            tokenProgram.toByteArray(),
            mint.toByteArray()
        ), solanaAccounts.associatedTokenAccountProgram()
    );
  }

  public static ProgramDerivedAddress findATA(final SolanaAccounts solanaAccounts,
                                              final PublicKey owner,
                                              final PublicKey mint) {
    return findATA(solanaAccounts, owner, solanaAccounts.tokenProgram(), mint);
  }

  public static ProgramDerivedAddress findATA2022(final SolanaAccounts solanaAccounts,
                                                  final PublicKey owner,
                                                  final PublicKey mint) {
    return findATA(solanaAccounts, owner, solanaAccounts.token2022Program(), mint);
  }

  public static Instruction createATAForProgram(final boolean idempotent,
                                                final SolanaAccounts solanaAccounts,
                                                final PublicKey fundingAccount,
                                                final PublicKey pda,
                                                final PublicKey owner,
                                                final PublicKey mint,
                                                final AccountMeta tokenProgram) {
    final var keys = List.of(
        createWritableSigner(fundingAccount),
        createWrite(pda),
        createRead(owner),
        createRead(mint),
        solanaAccounts.readSystemProgram(),
        tokenProgram
    );
    return Instruction.createInstruction(
        solanaAccounts.invokedAssociatedTokenAccountProgram(),
        keys,
        idempotent
            ? Instructions.CreateIdempotent.discriminatorBytes
            : Instructions.Create.discriminatorBytes
    );
  }

  public static Instruction createATAForProgram(final boolean idempotent,
                                                final SolanaAccounts solanaAccounts,
                                                final PublicKey fundingAccount,
                                                final PublicKey pda,
                                                final PublicKey owner,
                                                final PublicKey mint,
                                                final PublicKey tokenProgram) {
    return createATAForProgram(
        idempotent,
        solanaAccounts,
        fundingAccount,
        pda,
        owner,
        mint,
        createRead(tokenProgram)
    );
  }

  public static Instruction createATAForProgram(final boolean idempotent,
                                                final SolanaAccounts solanaAccounts,
                                                final PublicKey fundingAccount,
                                                final PublicKey owner,
                                                final PublicKey mint,
                                                final AccountMeta tokenProgram) {
    final var pda = findATA(solanaAccounts, owner, tokenProgram.publicKey(), mint);
    return createATAForProgram(idempotent, solanaAccounts, fundingAccount, pda.publicKey(), owner, mint, tokenProgram);
  }

  public static Instruction createATAForProgram(final boolean idempotent,
                                                final SolanaAccounts solanaAccounts,
                                                final PublicKey fundingAccount,
                                                final PublicKey owner,
                                                final PublicKey mint,
                                                final PublicKey tokenProgram) {
    return createATAForProgram(idempotent, solanaAccounts, fundingAccount, owner, mint, createRead(tokenProgram));
  }

  public static Instruction createATA(final boolean idempotent,
                                      final SolanaAccounts solanaAccounts,
                                      final PublicKey fundingAccount,
                                      final PublicKey pda,
                                      final PublicKey owner,
                                      final PublicKey mint) {
    return createATAForProgram(
        idempotent,
        solanaAccounts,
        fundingAccount,
        pda,
        owner,
        mint,
        solanaAccounts.readTokenProgram()
    );
  }

  public static Instruction createATA(final boolean idempotent,
                                      final SolanaAccounts solanaAccounts,
                                      final PublicKey fundingAccount,
                                      final PublicKey owner,
                                      final PublicKey mint) {
    return createATAForProgram(
        idempotent,
        solanaAccounts,
        fundingAccount,
        owner,
        mint,
        solanaAccounts.readTokenProgram()
    );
  }

  public static Instruction createATA(final boolean idempotent,
                                      final SolanaAccounts solanaAccounts,
                                      final PublicKey fundingAccount,
                                      final PublicKey mint) {
    return createATA(idempotent, solanaAccounts, fundingAccount, fundingAccount, mint);
  }

  public static Instruction createATA2022(final boolean idempotent,
                                          final SolanaAccounts solanaAccounts,
                                          final PublicKey fundingAccount,
                                          final PublicKey pda,
                                          final PublicKey owner,
                                          final PublicKey mint) {
    return createATAForProgram(
        idempotent,
        solanaAccounts,
        fundingAccount,
        pda,
        owner,
        mint,
        solanaAccounts.readToken2022Program()
    );
  }

  public static Instruction createATA2022(final boolean idempotent,
                                          final SolanaAccounts solanaAccounts,
                                          final PublicKey fundingAccount,
                                          final PublicKey owner,
                                          final PublicKey mint) {
    return createATAForProgram(
        idempotent,
        solanaAccounts,
        fundingAccount,
        owner,
        mint,
        solanaAccounts.readToken2022Program()
    );
  }

  public static Instruction createATA2022(final boolean idempotent,
                                          final SolanaAccounts solanaAccounts,
                                          final PublicKey fundingAccount,
                                          final PublicKey mint) {
    return createATA2022(idempotent, solanaAccounts, fundingAccount, fundingAccount, mint);
  }

  public static Instruction recoverNested(final SolanaAccounts solanaAccounts,
                                          final PublicKey nestedTokenAccount,
                                          final PublicKey nestedTokenMint,
                                          final PublicKey walletTokenAccount,
                                          final PublicKey ownerTokenAccount,
                                          final PublicKey ownerTokenMint,
                                          final PublicKey owner,
                                          final AccountMeta tokenProgram) {
    final var keys = List.of(
        createWrite(nestedTokenAccount),
        createRead(nestedTokenMint),
        createWrite(walletTokenAccount),
        createRead(ownerTokenAccount),
        createRead(ownerTokenMint),
        createWritableSigner(owner),
        solanaAccounts.readSystemProgram(),
        tokenProgram
    );
    return Instruction.createInstruction(
        solanaAccounts.invokedAssociatedTokenAccountProgram(),
        keys,
        Instructions.RecoverNested.discriminatorBytes
    );
  }

  private AssociatedTokenProgram() {
  }
}
