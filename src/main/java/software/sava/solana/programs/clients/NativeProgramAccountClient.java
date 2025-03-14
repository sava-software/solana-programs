package software.sava.solana.programs.clients;

import software.sava.core.accounts.*;
import software.sava.core.accounts.lookup.AddressLookupTable;
import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.accounts.meta.LookupTableAccountMeta;
import software.sava.core.accounts.token.TokenAccount;
import software.sava.core.tx.Instruction;
import software.sava.core.tx.Transaction;
import software.sava.rpc.json.http.client.SolanaRpcClient;
import software.sava.rpc.json.http.response.AccountInfo;
import software.sava.solana.programs.stake.StakeAccount;
import software.sava.solana.programs.stake.StakeAuthorize;
import software.sava.solana.programs.stake.StakeState;
import software.sava.solana.programs.token.AssociatedTokenProgram;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NativeProgramAccountClient {

  static NativeProgramAccountClient createClient(final SolanaAccounts accounts,
                                                 final PublicKey owner,
                                                 final AccountMeta feePayer) {
    final var nativeClient = NativeProgramClient.createClient(accounts);
    return new NativeProgramAccountClientImpl(nativeClient, owner, feePayer);
  }

  static NativeProgramAccountClient createClient(final SolanaAccounts accounts, final AccountMeta ownerAndFeePayer) {
    return createClient(accounts, ownerAndFeePayer.publicKey(), ownerAndFeePayer);
  }

  static NativeProgramAccountClient createClient(final SolanaAccounts accounts, final PublicKey ownerAndFeePayer) {
    return createClient(accounts, AccountMeta.createFeePayer(ownerAndFeePayer));
  }

  static NativeProgramAccountClient createClient(final SolanaAccounts accounts, final Signer ownerAndFeePayer) {
    return createClient(accounts, AccountMeta.createFeePayer(ownerAndFeePayer.publicKey()));
  }

  static NativeProgramAccountClient createClient(final AccountMeta ownerAndFeePayer) {
    return createClient(SolanaAccounts.MAIN_NET, ownerAndFeePayer);
  }

  static NativeProgramAccountClient createClient(final PublicKey ownerAndFeePayer) {
    return createClient(SolanaAccounts.MAIN_NET, ownerAndFeePayer);
  }

  static NativeProgramAccountClient createClient(final Signer ownerAndFeePayer) {
    return createClient(SolanaAccounts.MAIN_NET, ownerAndFeePayer);
  }

  static NativeProgramAccountClient createClient(final SolanaAccounts accounts,
                                                 final PublicKey owner,
                                                 final PublicKey feePayer) {
    return createClient(accounts, owner, AccountMeta.createFeePayer(feePayer));
  }

  static NativeProgramAccountClient createClient(final SolanaAccounts accounts,
                                                 final PublicKey owner,
                                                 final Signer feePayer) {
    return createClient(accounts, owner, AccountMeta.createFeePayer(feePayer.publicKey()));
  }

  static NativeProgramAccountClient createClient(final PublicKey owner, final AccountMeta feePayer) {
    return createClient(SolanaAccounts.MAIN_NET, owner, feePayer);
  }

  static NativeProgramAccountClient createClient(final PublicKey owner, final PublicKey feePayer) {
    return createClient(SolanaAccounts.MAIN_NET, owner, feePayer);
  }

  static NativeProgramAccountClient createClient(final PublicKey owner, final Signer feePayer) {
    return createClient(SolanaAccounts.MAIN_NET, owner, feePayer);
  }

  PublicKey ownerPublicKey();

  AccountMeta feePayer();

  SolanaAccounts solanaAccounts();

  ProgramDerivedAddress wrappedSolPDA();

  NativeProgramClient nativeProgramClient();

  Transaction createTransaction(final PublicKey feePayer,
                                final int computeUnitLimit,
                                final long microLamportComputeUnitPrice,
                                final Instruction instruction);

  Transaction createTransaction(final AccountMeta feePayer,
                                final int computeUnitLimit,
                                final long microLamportComputeUnitPrice,
                                final Instruction instruction);

  Transaction createTransaction(final Instruction instruction);

  Transaction createTransaction(final List<Instruction> instructions);

  Transaction createTransaction(final int computeUnitLimit,
                                final long microLamportComputeUnitPrice,
                                final Instruction instruction);

  Transaction createTransaction(final int computeUnitLimit,
                                final long microLamportComputeUnitPrice,
                                final List<Instruction> instructions);

  Transaction createTransaction(final int computeUnitLimit,
                                final long microLamportComputeUnitPrice,
                                final Transaction instruction);

  Transaction createTransaction(final PublicKey feePayer,
                                final int computeUnitLimit,
                                final long microLamportComputeUnitPrice,
                                final Instruction instruction,
                                final AddressLookupTable lookupTable);

  Transaction createTransaction(final AccountMeta feePayer,
                                final int computeUnitLimit,
                                final long microLamportComputeUnitPrice,
                                final Instruction instruction,
                                final AddressLookupTable lookupTable);

  Transaction createTransaction(final Instruction instruction,
                                final AddressLookupTable lookupTable);

  Transaction createTransaction(final List<Instruction> instructions,
                                final AddressLookupTable lookupTable);

  Transaction createTransaction(final int computeUnitLimit,
                                final long microLamportComputeUnitPrice,
                                final Instruction instruction,
                                final AddressLookupTable lookupTable);

  Transaction createTransaction(final int computeUnitLimit,
                                final long microLamportComputeUnitPrice,
                                final List<Instruction> instructions,
                                final AddressLookupTable lookupTable);

  Transaction createTransaction(final PublicKey feePayer,
                                final int computeUnitLimit,
                                final long microLamportComputeUnitPrice,
                                final Instruction instruction,
                                final LookupTableAccountMeta[] tableAccountMetas);

  Transaction createTransaction(final AccountMeta feePayer,
                                final int computeUnitLimit,
                                final long microLamportComputeUnitPrice,
                                final Instruction instruction,
                                final LookupTableAccountMeta[] tableAccountMetas);

  Transaction createTransaction(final Instruction instruction,
                                final LookupTableAccountMeta[] tableAccountMetas);

  Transaction createTransaction(final List<Instruction> instructions,
                                final LookupTableAccountMeta[] tableAccountMetas);

  Transaction createTransaction(final int computeUnitLimit,
                                final long microLamportComputeUnitPrice,
                                final Instruction instruction,
                                final LookupTableAccountMeta[] tableAccountMetas);

  Transaction createTransaction(final int computeUnitLimit,
                                final long microLamportComputeUnitPrice,
                                final List<Instruction> instructions,
                                final LookupTableAccountMeta[] tableAccountMetas);

  ProgramDerivedAddress findATA(final PublicKey mint);

  ProgramDerivedAddress findATA(final PublicKey tokenProgram, final PublicKey mint);

  ProgramDerivedAddress findATAForFeePayer(final PublicKey mint);

  ProgramDerivedAddress findATAForFeePayer(final PublicKey tokenProgram, final PublicKey mint);

  CompletableFuture<List<AccountInfo<TokenAccount>>> fetchTokenAccounts(final SolanaRpcClient rpcClient,
                                                                        final PublicKey tokenMintAddress);

  CompletableFuture<List<AccountInfo<TokenAccount>>> fetchTokenAccounts(final SolanaRpcClient rpcClient);

  CompletableFuture<List<AccountInfo<TokenAccount>>> fetchToken2022Accounts(final SolanaRpcClient rpcClient);

  Instruction syncNative();

  List<Instruction> wrapSOL(final long lamports);

  Instruction unwrapSOL();

  Instruction createAccount(final PublicKey newAccountPublicKey,
                            final long lamports,
                            final long space,
                            final PublicKey programOwner);

  Instruction createAccountWithSeed(final AccountWithSeed accountWithSeed,
                                    final long lamports,
                                    final long space,
                                    final PublicKey programOwner);

  Instruction transferSolLamports(final PublicKey toPublicKey, final long lamports);

  Instruction allocateAccountSpaceWithSeed(final AccountWithSeed accountWithSeed,
                                           final long space,
                                           final PublicKey programOwner);

  AccountWithSeed createOffCurveAccountWithSeed(final String asciiSeed, final PublicKey programId);

  AccountWithSeed createOffCurveStakeAccountWithSeed(final String asciiSeed);

  CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsByStakeAuthority(final SolanaRpcClient rpcClient,
                                                                                        final StakeState stakeState);

  default CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsByStakeAuthority(final SolanaRpcClient rpcClient) {
    return fetchStakeAccountsByStakeAuthority(rpcClient, StakeState.Stake);
  }

  CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsByWithdrawAuthority(final SolanaRpcClient rpcClient,
                                                                                           final StakeState stakeState);

  default CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsByWithdrawAuthority(final SolanaRpcClient rpcClient) {
    return fetchStakeAccountsByWithdrawAuthority(rpcClient, StakeState.Stake);
  }

  CompletableFuture<List<AccountInfo<AddressLookupTable>>> fetchLookupTableAccountsByAuthority(final SolanaRpcClient rpcClient);

  Instruction createStakeAccount(final PublicKey newAccountPublicKey, final long lamports);

  Instruction createStakeAccountWithSeed(final AccountWithSeed accountWithSeed, final long lamports);

  Instruction allocateStakeAccountWithSeed(final AccountWithSeed accountWithSeed);

  Instruction transferSolLamportsWithSeed(final AccountWithSeed accountWithSeed,
                                          final PublicKey recipientAccount,
                                          final long lamports,
                                          final PublicKey programOwner);

  default Instruction transferSolLamportsWithSeed(final AccountWithSeed accountWithSeed,
                                                  final long lamports,
                                                  final PublicKey programOwner) {
    return transferSolLamportsWithSeed(accountWithSeed, ownerPublicKey(), lamports, programOwner);
  }

  Instruction transferToken(final AccountMeta invokedTokenProgram,
                            final PublicKey fromTokenAccount,
                            final PublicKey toTokenAccount,
                            final long scaledAmount);

  default Instruction transferToken(final PublicKey fromTokenAccount,
                                    final PublicKey toTokenAccount,
                                    final long scaledAmount) {
    return transferToken(
        solanaAccounts().invokedTokenProgram(),
        fromTokenAccount,
        toTokenAccount,
        scaledAmount
    );
  }

  Instruction transferTokenChecked(final AccountMeta invokedTokenProgram,
                                   final PublicKey fromTokenAccount,
                                   final PublicKey toTokenAccount,
                                   final long scaledAmount,
                                   final int decimals,
                                   final PublicKey tokenMint);

  default Instruction transferTokenChecked(final PublicKey fromTokenAccount,
                                           final PublicKey toTokenAccount,
                                           final long scaledAmount,
                                           final int decimals,
                                           final PublicKey tokenMint) {
    return transferTokenChecked(
        solanaAccounts().invokedTokenProgram(),
        fromTokenAccount,
        toTokenAccount,
        scaledAmount,
        decimals,
        tokenMint
    );
  }

  Instruction closeTokenAccount(final AccountMeta invokedTokenProgram, final PublicKey tokenAccount);

  default Instruction closeTokenAccount(final PublicKey tokenAccount) {
    return closeTokenAccount(solanaAccounts().invokedTokenProgram(), tokenAccount);
  }

  default Instruction createATAForFundedBy(boolean idempotent,
                                           final PublicKey fundingAccount,
                                           final PublicKey pda,
                                           final PublicKey owner,
                                           final PublicKey mint,
                                           final AccountMeta tokenProgram) {
    return AssociatedTokenProgram.createATAForProgram(
        idempotent, solanaAccounts(), fundingAccount, pda, owner, mint, tokenProgram
    );
  }

  default Instruction createATAForFundedBy(final boolean idempotent,
                                           final PublicKey fundingAccount,
                                           final PublicKey owner,
                                           final PublicKey mint,
                                           final AccountMeta tokenProgram) {
    return AssociatedTokenProgram.createATAForProgram(
        idempotent, solanaAccounts(), fundingAccount, owner, mint, tokenProgram
    );
  }

  default Instruction createATAForFundedByFeePayer(final boolean idempotent,
                                                   final PublicKey pda,
                                                   final PublicKey owner,
                                                   final PublicKey mint,
                                                   final AccountMeta tokenProgram) {
    return createATAForFundedBy(
        idempotent, feePayer().publicKey(), pda, owner, mint, tokenProgram);
  }

  default Instruction createATAForFundedByFeePayer(final boolean idempotent,
                                                   final PublicKey owner,
                                                   final PublicKey mint,
                                                   final AccountMeta tokenProgram) {
    return createATAForFundedBy(idempotent, feePayer().publicKey(), owner, mint, tokenProgram);
  }

  default Instruction createATAForFundedByOwner(final boolean idempotent,
                                                final PublicKey pda,
                                                final PublicKey owner,
                                                final PublicKey mint,
                                                final AccountMeta tokenProgram) {
    return createATAForFundedBy(idempotent, ownerPublicKey(), pda, owner, mint, tokenProgram);
  }

  default Instruction createATAForFundedByOwner(final boolean idempotent,
                                                final PublicKey owner,
                                                final PublicKey mint,
                                                final AccountMeta tokenProgram) {
    return createATAForFundedBy(idempotent, ownerPublicKey(), owner, mint, tokenProgram);
  }

  default Instruction createATAForOwnerFundedByOwner(final boolean idempotent,
                                                     final PublicKey pda,
                                                     final PublicKey mint,
                                                     final AccountMeta tokenProgram) {
    return createATAForFundedByOwner(idempotent, pda, ownerPublicKey(), mint, tokenProgram);
  }

  default Instruction createATAForOwnerFundedByOwner(final boolean idempotent,
                                                     final PublicKey mint,
                                                     final AccountMeta tokenProgram) {
    return createATAForFundedByOwner(idempotent, ownerPublicKey(), mint, tokenProgram);
  }

  default Instruction createATAForOwnerFundedByFeePayer(final boolean idempotent,
                                                        final PublicKey pda,
                                                        final PublicKey mint,
                                                        final AccountMeta tokenProgram) {
    return createATAForFundedByFeePayer(idempotent, pda, ownerPublicKey(), mint, tokenProgram);
  }

  default Instruction createATAForOwnerFundedByFeePayer(final boolean idempotent,
                                                        final PublicKey mint,
                                                        final AccountMeta tokenProgram) {
    return createATAForFundedByFeePayer(idempotent, ownerPublicKey(), mint, tokenProgram);
  }

  default Instruction createATAForFeePayerFundedByOwner(final boolean idempotent,
                                                        final PublicKey pda,
                                                        final PublicKey mint,
                                                        final AccountMeta tokenProgram) {
    return createATAForFundedByOwner(idempotent, pda, feePayer().publicKey(), mint, tokenProgram);
  }

  default Instruction createATAForFeePayerFundedByOwner(final boolean idempotent,
                                                        final PublicKey mint,
                                                        final AccountMeta tokenProgram) {
    return createATAForFundedByOwner(idempotent, feePayer().publicKey(), mint, tokenProgram);
  }

  default Instruction createATAForFeePayerFundedByFeePayer(final boolean idempotent,
                                                           final PublicKey pda,
                                                           final PublicKey mint,
                                                           final AccountMeta tokenProgram) {
    return createATAForFundedByFeePayer(idempotent, pda, feePayer().publicKey(), mint, tokenProgram);
  }

  default Instruction createATAForFeePayerFundedByFeePayer(final boolean idempotent,
                                                           final PublicKey mint,
                                                           final AccountMeta tokenProgram) {
    return createATAForFundedByFeePayer(idempotent, feePayer().publicKey(), mint, tokenProgram);
  }

  default Instruction createATAForFundedBy(final boolean idempotent,
                                           final PublicKey fundingAccount,
                                           final PublicKey pda,
                                           final PublicKey owner,
                                           final PublicKey mint) {
    return AssociatedTokenProgram.createATA(idempotent, solanaAccounts(), fundingAccount, pda, owner, mint);
  }

  default Instruction createATAForFundedBy(final boolean idempotent,
                                           final PublicKey fundingAccount,
                                           final PublicKey owner,
                                           final PublicKey mint) {
    return createATAForFundedBy(idempotent, fundingAccount, owner, mint, solanaAccounts().readTokenProgram());
  }

  default Instruction createATAForFundedByFeePayer(final boolean idempotent,
                                                   final PublicKey pda,
                                                   final PublicKey owner,
                                                   final PublicKey mint) {
    return createATAForFundedByFeePayer(idempotent, pda, owner, mint, solanaAccounts().readTokenProgram());
  }

  default Instruction createATAForFundedByFeePayer(final boolean idempotent,
                                                   final PublicKey owner,
                                                   final PublicKey mint) {
    return createATAForFundedByFeePayer(idempotent, owner, mint, solanaAccounts().readTokenProgram());
  }

  default Instruction createATAForFundedByOwner(final boolean idempotent,
                                                final PublicKey pda,
                                                final PublicKey owner,
                                                final PublicKey mint) {
    return createATAForFundedByOwner(idempotent, pda, owner, mint, solanaAccounts().readTokenProgram());
  }

  default Instruction createATAForFundedByOwner(final boolean idempotent, final PublicKey owner, final PublicKey mint) {
    return createATAForFundedByOwner(idempotent, owner, mint, solanaAccounts().readTokenProgram());
  }

  default Instruction createATAForOwnerFundedByOwner(final boolean idempotent,
                                                     final PublicKey pda,
                                                     final PublicKey mint) {
    return createATAForOwnerFundedByOwner(idempotent, pda, mint, solanaAccounts().readTokenProgram());
  }

  default Instruction createATAForOwnerFundedByOwner(final boolean idempotent, final PublicKey mint) {
    return createATAForOwnerFundedByOwner(idempotent, mint, solanaAccounts().readTokenProgram());
  }

  default Instruction createATAForOwnerFundedByFeePayer(final boolean idempotent,
                                                        final PublicKey pda,
                                                        final PublicKey mint) {
    return createATAForOwnerFundedByFeePayer(idempotent, pda, mint, solanaAccounts().readTokenProgram());
  }

  default Instruction createATAForOwnerFundedByFeePayer(final boolean idempotent, final PublicKey mint) {
    return createATAForOwnerFundedByFeePayer(idempotent, mint, solanaAccounts().readTokenProgram());
  }

  default Instruction createATAForFeePayerFundedByOwner(final boolean idempotent,
                                                        final PublicKey pda,
                                                        final PublicKey mint) {
    return createATAForFeePayerFundedByOwner(idempotent, pda, mint, solanaAccounts().readTokenProgram());
  }

  default Instruction createATAForFeePayerFundedByOwner(final boolean idempotent, final PublicKey mint) {
    return createATAForFeePayerFundedByOwner(idempotent, mint, solanaAccounts().readTokenProgram());
  }

  default Instruction createATAForFeePayerFundedByFeePayer(final boolean idempotent,
                                                           final PublicKey pda,
                                                           final PublicKey mint) {
    return createATAForFeePayerFundedByFeePayer(idempotent, pda, mint, solanaAccounts().readTokenProgram());
  }

  default Instruction createATAForFeePayerFundedByFeePayer(final boolean idempotent, final PublicKey mint) {
    return createATAForFeePayerFundedByFeePayer(idempotent, mint, solanaAccounts().readTokenProgram());
  }

  Instruction initializeStakeAccount(final PublicKey unInitializedStakeAccount, final PublicKey staker);

  Instruction initializeStakeAccount(final PublicKey unInitializedStakeAccount);

  Instruction initializeStakeAccountChecked(final PublicKey unInitializedStakeAccount, final PublicKey staker);

  Instruction initializeStakeAccountChecked(final PublicKey unInitializedStakeAccount);

  Instruction authorizeStakeAccount(final PublicKey stakeAccount,
                                    final PublicKey stakeOrWithdrawAuthority,
                                    final PublicKey lockupAuthority,
                                    final StakeAuthorize stakeAuthorize);

  Instruction authorizeStakeAccount(final PublicKey stakeAccount,
                                    final PublicKey stakeOrWithdrawAuthority,
                                    final StakeAuthorize stakeAuthorize);

  default Instruction authorizeStakeAccount(final StakeAccount stakeAccount, final StakeAuthorize stakeAuthorize) {
    return authorizeStakeAccount(
        stakeAccount.address(),
        stakeAuthorize == StakeAuthorize.Staker
            ? stakeAccount.stakeAuthority()
            : stakeAccount.withdrawAuthority(),
        stakeAuthorize
    );
  }

  Instruction authorizeStakeAccountChecked(final PublicKey stakeAccount,
                                           final PublicKey stakeOrWithdrawAuthority,
                                           final PublicKey newStakeOrWithdrawAuthority,
                                           final StakeAuthorize stakeAuthorize);

  Instruction authorizeStakeAccountChecked(final PublicKey stakeAccount,
                                           final PublicKey stakeOrWithdrawAuthority,
                                           final StakeAuthorize stakeAuthorize);

  default Instruction authorizeStakeAccountChecked(final StakeAccount stakeAccount,
                                                   final StakeAuthorize stakeAuthorize) {
    return authorizeStakeAccountChecked(
        stakeAccount.address(),
        stakeAuthorize == StakeAuthorize.Staker
            ? stakeAccount.stakeAuthority()
            : stakeAccount.withdrawAuthority(),
        stakeAuthorize
    );
  }

  Instruction delegateStakeAccount(final PublicKey initializedStakeAccount,
                                   final PublicKey validatorVoteAccount);

  Instruction deactivateStakeAccount(final PublicKey delegatedStakeAccount);

  Instruction withdrawStakeAccount(final StakeAccount stakeAccount, final long lamports);

  default Instruction closeStakeAccount(final AccountInfo<StakeAccount> stakeAccountInfo) {
    final var stakeAccount = stakeAccountInfo.data();
    return withdrawStakeAccount(stakeAccount, stakeAccountInfo.lamports());
  }

  default List<Instruction> closeStakeAccounts(final Collection<AccountInfo<StakeAccount>> stakeAccounts) {
    return stakeAccounts.stream().map(this::closeStakeAccount).toList();
  }

  ProgramDerivedAddress findLookupTableAddress(final long recentSlot);

  Instruction createLookupTable(final ProgramDerivedAddress uninitializedTableAccount, final long recentSlot);

  Instruction freezeLookupTable(final PublicKey tableAccount);

  Instruction extendLookupTable(final PublicKey tableAccount, final List<PublicKey> newAddresses);

  Instruction deactivateLookupTable(final PublicKey tableAccount);

  Instruction closeLookupTable(final PublicKey tableAccount);
}
