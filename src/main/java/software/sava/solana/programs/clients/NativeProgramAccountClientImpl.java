package software.sava.solana.programs.clients;

import software.sava.core.accounts.AccountWithSeed;
import software.sava.core.accounts.ProgramDerivedAddress;
import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.accounts.lookup.AddressLookupTable;
import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.accounts.meta.LookupTableAccountMeta;
import software.sava.core.accounts.token.TokenAccount;
import software.sava.core.tx.Instruction;
import software.sava.core.tx.Transaction;
import software.sava.rpc.json.http.client.SolanaRpcClient;
import software.sava.rpc.json.http.response.AccountInfo;
import software.sava.solana.programs.address_lookup_table.AddressLookupTableProgram;
import software.sava.solana.programs.stake.StakeAccount;
import software.sava.solana.programs.stake.StakeAuthorize;
import software.sava.solana.programs.stake.StakeState;
import software.sava.solana.programs.system.SystemProgram;
import software.sava.solana.programs.token.AssociatedTokenProgram;
import software.sava.solana.programs.token.TokenProgram;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static software.sava.solana.programs.compute_budget.ComputeBudgetProgram.COMPUTE_UNITS_CONSUMED;

final class NativeProgramAccountClientImpl implements NativeProgramAccountClient {

  private final SolanaAccounts solanaAccounts;
  private final NativeProgramClient nativeProgramClient;
  private final PublicKey owner;
  private final AccountMeta feePayer;
  private final PublicKey feePayerPublicKey;
  private final ProgramDerivedAddress wrappedSolPDA;

  NativeProgramAccountClientImpl(final NativeProgramClient nativeProgramClient,
                                 final PublicKey owner,
                                 final AccountMeta feePayer) {
    this.solanaAccounts = nativeProgramClient.accounts();
    this.nativeProgramClient = nativeProgramClient;
    this.owner = owner;
    this.feePayer = feePayer;
    this.feePayerPublicKey = feePayer.publicKey();
    this.wrappedSolPDA = findATA(solanaAccounts.wrappedSolTokenMint());
  }

  @Override
  public PublicKey ownerPublicKey() {
    return owner;
  }

  @Override
  public AccountMeta feePayer() {
    return feePayer;
  }

  @Override
  public SolanaAccounts solanaAccounts() {
    return solanaAccounts;
  }

  @Override
  public ProgramDerivedAddress wrappedSolPDA() {
    return wrappedSolPDA;
  }

  @Override
  public NativeProgramClient nativeProgramClient() {
    return nativeProgramClient;
  }

  @Override
  public Transaction createTransaction(final PublicKey feePayer,
                                       final int computeUnitLimit,
                                       final long microLamportComputeUnitPrice,
                                       final Instruction instruction) {
    return Transaction.createTx(feePayer, List.of(
            nativeProgramClient.computeUnitLimit(computeUnitLimit + COMPUTE_UNITS_CONSUMED),
            nativeProgramClient.computeUnitPrice(microLamportComputeUnitPrice),
            instruction
        )
    );
  }

  @Override
  public Transaction createTransaction(final AccountMeta feePayer,
                                       final int computeUnitLimit,
                                       final long microLamportComputeUnitPrice,
                                       final Instruction instruction) {
    return Transaction.createTx(feePayer, List.of(
            nativeProgramClient.computeUnitLimit(computeUnitLimit + COMPUTE_UNITS_CONSUMED),
            nativeProgramClient.computeUnitPrice(microLamportComputeUnitPrice),
            instruction
        )
    );
  }

  @Override
  public Transaction createTransaction(final Instruction instruction) {
    return Transaction.createTx(feePayer, instruction);
  }

  @Override
  public Transaction createTransaction(final List<Instruction> instructions) {
    return Transaction.createTx(feePayer, instructions);
  }

  @Override
  public Transaction createTransaction(final int computeUnitLimit,
                                       final long microLamportComputeUnitPrice,
                                       final Instruction instruction) {
    return createTransaction(feePayer, computeUnitLimit, microLamportComputeUnitPrice, instruction);
  }

  @Override
  public Transaction createTransaction(final int computeUnitLimit,
                                       final long microLamportComputeUnitPrice,
                                       final List<Instruction> instructions) {
    return createTransaction(computeUnitLimit, microLamportComputeUnitPrice, Transaction.createTx(feePayer, instructions));
  }

  @Override
  public Transaction createTransaction(final int computeUnitLimit,
                                       final long microLamportComputeUnitPrice,
                                       final Transaction transaction) {
    return transaction.prependInstructions(
        nativeProgramClient.computeUnitLimit(computeUnitLimit + COMPUTE_UNITS_CONSUMED),
        nativeProgramClient.computeUnitPrice(microLamportComputeUnitPrice)
    );
  }

  @Override
  public Transaction createTransaction(final PublicKey feePayer,
                                       final int computeUnitLimit,
                                       final long microLamportComputeUnitPrice,
                                       final Instruction instruction,
                                       final AddressLookupTable lookupTable) {
    return Transaction.createTx(feePayer, List.of(
            nativeProgramClient.computeUnitLimit(computeUnitLimit + COMPUTE_UNITS_CONSUMED),
            nativeProgramClient.computeUnitPrice(microLamportComputeUnitPrice),
            instruction
        ),
        lookupTable
    );
  }

  @Override
  public Transaction createTransaction(final AccountMeta feePayer,
                                       final int computeUnitLimit,
                                       final long microLamportComputeUnitPrice,
                                       final Instruction instruction,
                                       final AddressLookupTable lookupTable) {
    return Transaction.createTx(feePayer, List.of(
            nativeProgramClient.computeUnitLimit(computeUnitLimit + COMPUTE_UNITS_CONSUMED),
            nativeProgramClient.computeUnitPrice(microLamportComputeUnitPrice),
            instruction
        ),
        lookupTable
    );
  }

  @Override
  public Transaction createTransaction(final Instruction instruction, final AddressLookupTable lookupTable) {
    return Transaction.createTx(feePayer, List.of(instruction), lookupTable);
  }

  @Override
  public Transaction createTransaction(final List<Instruction> instructions, final AddressLookupTable lookupTable) {
    return Transaction.createTx(feePayer, instructions, lookupTable);
  }

  @Override
  public Transaction createTransaction(final int computeUnitLimit,
                                       final long microLamportComputeUnitPrice,
                                       final Instruction instruction,
                                       final AddressLookupTable lookupTable) {
    return createTransaction(feePayer, computeUnitLimit, microLamportComputeUnitPrice, instruction, lookupTable);
  }


  @Override
  public Transaction createTransaction(final int computeUnitLimit,
                                       final long microLamportComputeUnitPrice,
                                       final List<Instruction> instructions,
                                       final AddressLookupTable lookupTable) {
    return createTransaction(
        computeUnitLimit,
        microLamportComputeUnitPrice,
        Transaction.createTx(feePayer, instructions, lookupTable)
    );
  }

  @Override
  public Transaction createTransaction(final PublicKey feePayer,
                                       final int computeUnitLimit,
                                       final long microLamportComputeUnitPrice,
                                       final Instruction instruction,
                                       final LookupTableAccountMeta[] tableAccountMetas) {
    return Transaction.createTx(feePayer, List.of(
            nativeProgramClient.computeUnitLimit(computeUnitLimit + COMPUTE_UNITS_CONSUMED),
            nativeProgramClient.computeUnitPrice(microLamportComputeUnitPrice),
            instruction
        ),
        tableAccountMetas
    );
  }

  @Override
  public Transaction createTransaction(final AccountMeta feePayer,
                                       final int computeUnitLimit,
                                       final long microLamportComputeUnitPrice,
                                       final Instruction instruction,
                                       final LookupTableAccountMeta[] tableAccountMetas) {
    return Transaction.createTx(feePayer, List.of(
            nativeProgramClient.computeUnitLimit(computeUnitLimit + COMPUTE_UNITS_CONSUMED),
            nativeProgramClient.computeUnitPrice(microLamportComputeUnitPrice),
            instruction
        ),
        tableAccountMetas
    );
  }

  @Override
  public Transaction createTransaction(final Instruction instruction,
                                       final LookupTableAccountMeta[] tableAccountMetas) {
    return Transaction.createTx(feePayer, List.of(instruction), tableAccountMetas);
  }

  @Override
  public Transaction createTransaction(final List<Instruction> instructions,
                                       final LookupTableAccountMeta[] tableAccountMetas) {
    return Transaction.createTx(feePayer, instructions, tableAccountMetas);
  }

  @Override
  public Transaction createTransaction(final int computeUnitLimit,
                                       final long microLamportComputeUnitPrice,
                                       final Instruction instruction,
                                       final LookupTableAccountMeta[] tableAccountMetas) {
    return createTransaction(feePayer, computeUnitLimit, microLamportComputeUnitPrice, instruction, tableAccountMetas);
  }

  @Override
  public Transaction createTransaction(final int computeUnitLimit,
                                       final long microLamportComputeUnitPrice,
                                       final List<Instruction> instructions,
                                       final LookupTableAccountMeta[] tableAccountMetas) {
    return createTransaction(
        computeUnitLimit,
        microLamportComputeUnitPrice,
        Transaction.createTx(feePayer, instructions, tableAccountMetas)
    );
  }

  @Override
  public ProgramDerivedAddress findATA(final PublicKey mint) {
    return AssociatedTokenProgram.findATA(solanaAccounts, owner, mint);
  }

  @Override
  public ProgramDerivedAddress findATA(final PublicKey tokenProgram, final PublicKey mint) {
    return AssociatedTokenProgram.findATA(solanaAccounts, owner, tokenProgram, mint);
  }

  @Override
  public ProgramDerivedAddress findATAForFeePayer(final PublicKey mint) {
    return AssociatedTokenProgram.findATA(solanaAccounts, feePayerPublicKey, mint);
  }

  @Override
  public ProgramDerivedAddress findATAForFeePayer(final PublicKey tokenProgram, final PublicKey mint) {
    return AssociatedTokenProgram.findATA(solanaAccounts, feePayerPublicKey, tokenProgram, mint);
  }

  @Override
  public CompletableFuture<List<AccountInfo<TokenAccount>>> fetchTokenAccounts(final SolanaRpcClient rpcClient,
                                                                               final PublicKey tokenMintAddress) {
    return rpcClient.getTokenAccountsForTokenMintByOwner(owner, tokenMintAddress);
  }

  @Override
  public CompletableFuture<List<AccountInfo<TokenAccount>>> fetchTokenAccounts(final SolanaRpcClient rpcClient) {
    return rpcClient.getTokenAccountsForProgramByOwner(owner, solanaAccounts.tokenProgram());
  }

  @Override
  public CompletableFuture<List<AccountInfo<TokenAccount>>> fetchToken2022Accounts(final SolanaRpcClient rpcClient) {
    return rpcClient.getTokenAccountsForProgramByOwner(owner, solanaAccounts.token2022Program());
  }

  @Override
  public Instruction syncNative() {
    return nativeProgramClient.syncNative(wrappedSolPDA.publicKey());
  }

  @Override
  public List<Instruction> wrapSOL(final long lamports) {
    final var transferIx = transferSolLamports(wrappedSolPDA.publicKey(), lamports);
    final var syncNativeIx = nativeProgramClient.syncNative(wrappedSolPDA.publicKey());
    return List.of(transferIx, syncNativeIx);
  }

  @Override
  public Instruction unwrapSOL() {
    return closeTokenAccount(wrappedSolPDA.publicKey());
  }

  @Override
  public Instruction createAccount(final PublicKey newAccountPublicKey,
                                   final long lamports,
                                   final long space,
                                   final PublicKey programOwner) {
    return SystemProgram.createAccount(
        solanaAccounts.invokedSystemProgram(),
        owner,
        newAccountPublicKey,
        lamports,
        space,
        programOwner
    );
  }

  @Override
  public Instruction createAccountWithSeed(final AccountWithSeed accountWithSeed,
                                           final long lamports,
                                           final long space,
                                           final PublicKey programOwner) {
    return SystemProgram.createAccountWithSeed(
        solanaAccounts.invokedSystemProgram(),
        feePayerPublicKey,
        accountWithSeed,
        lamports,
        space,
        programOwner
    );
  }

  @Override
  public Instruction allocateAccountSpaceWithSeed(final AccountWithSeed accountWithSeed,
                                                  final long space,
                                                  final PublicKey programOwner) {
    return SystemProgram.allocateWithSeed(
        solanaAccounts.invokedSystemProgram(),
        accountWithSeed,
        space,
        programOwner
    );
  }

  @Override
  public AccountWithSeed createOffCurveAccountWithSeed(final String asciiSeed, final PublicKey programId) {
    return PublicKey.createOffCurveAccountWithAsciiSeed(feePayerPublicKey, asciiSeed, programId);
  }

  @Override
  public AccountWithSeed createOffCurveStakeAccountWithSeed(final String asciiSeed) {
    return createOffCurveAccountWithSeed(asciiSeed, solanaAccounts.stakeProgram());
  }

  @Override
  public CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsByStakeAuthority(final SolanaRpcClient rpcClient,
                                                                                               final StakeState stakeState) {
    return nativeProgramClient.fetchStakeAccountsByStakeAuthority(rpcClient, stakeState, ownerPublicKey());
  }

  @Override
  public CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsByWithdrawAuthority(final SolanaRpcClient rpcClient,
                                                                                                  final StakeState stakeState) {
    return nativeProgramClient.fetchStakeAccountsByWithdrawAuthority(rpcClient, stakeState, ownerPublicKey());
  }

  @Override
  public CompletableFuture<List<AccountInfo<AddressLookupTable>>> fetchLookupTableAccountsByAuthority(final SolanaRpcClient rpcClient) {
    return nativeProgramClient.fetchLookupTableAccountsByAuthority(rpcClient, ownerPublicKey());
  }

  @Override
  public Instruction createStakeAccount(final PublicKey newAccountPublicKey, final long lamports) {
    return createAccount(
        newAccountPublicKey,
        lamports,
        StakeAccount.BYTES,
        solanaAccounts.stakeProgram()
    );
  }

  @Override
  public Instruction createStakeAccountWithSeed(final AccountWithSeed accountWithSeed, final long lamports) {
    return createAccountWithSeed(
        accountWithSeed,
        lamports,
        StakeAccount.BYTES,
        solanaAccounts.stakeProgram()
    );
  }

  @Override
  public Instruction allocateStakeAccountWithSeed(final AccountWithSeed accountWithSeed) {
    return allocateAccountSpaceWithSeed(
        accountWithSeed,
        StakeAccount.BYTES,
        solanaAccounts.stakeProgram()
    );
  }

  @Override
  public Instruction transferSolLamportsWithSeed(final AccountWithSeed accountWithSeed,
                                                 final PublicKey recipientAccount,
                                                 final long lamports,
                                                 final PublicKey programOwner) {
    return SystemProgram.transferWithSeed(
        solanaAccounts.invokedSystemProgram(),
        accountWithSeed,
        recipientAccount,
        lamports,
        programOwner
    );
  }

  @Override
  public Instruction transferSolLamports(final PublicKey toPublicKey, final long lamports) {
    return SystemProgram.transfer(
        solanaAccounts.invokedSystemProgram(),
        owner,
        toPublicKey,
        lamports
    );
  }

  @Override
  public Instruction transferToken(final AccountMeta invokedTokenProgram,
                                   final PublicKey fromTokenAccount,
                                   final PublicKey toTokenAccount,
                                   final long scaledAmount) {
    return TokenProgram.transfer(
        invokedTokenProgram,
        fromTokenAccount,
        toTokenAccount,
        scaledAmount,
        owner
    );
  }

  @Override
  public Instruction transferTokenChecked(final AccountMeta invokedTokenProgram,
                                          final PublicKey fromTokenAccount,
                                          final PublicKey toTokenAccount,
                                          final long scaledAmount,
                                          final int decimals,
                                          final PublicKey tokenMint) {
    return TokenProgram.transferChecked(
        invokedTokenProgram,
        fromTokenAccount,
        toTokenAccount,
        scaledAmount,
        decimals,
        owner,
        tokenMint
    );
  }

  @Override
  public Instruction closeTokenAccount(final AccountMeta invokedTokenProgram, final PublicKey tokenAccount) {
    return TokenProgram.closeAccount(
        invokedTokenProgram,
        tokenAccount,
        owner,
        owner
    );
  }


  @Override
  public Instruction initializeStakeAccount(final PublicKey unInitializedStakeAccount, final PublicKey staker) {
    return nativeProgramClient.initializeStakeAccount(
        unInitializedStakeAccount,
        staker,
        owner
    );
  }

  @Override
  public Instruction initializeStakeAccount(final PublicKey unInitializedStakeAccount) {
    return initializeStakeAccount(unInitializedStakeAccount, owner);
  }

  @Override
  public Instruction initializeStakeAccountChecked(final PublicKey unInitializedStakeAccount, final PublicKey staker) {
    return nativeProgramClient.initializeStakeAccountChecked(
        unInitializedStakeAccount,
        staker,
        owner
    );
  }

  @Override
  public Instruction initializeStakeAccountChecked(final PublicKey unInitializedStakeAccount) {
    return nativeProgramClient.initializeStakeAccountChecked(
        unInitializedStakeAccount,
        owner,
        owner
    );
  }

  @Override
  public Instruction authorizeStakeAccount(final PublicKey stakeAccount,
                                           final PublicKey stakeOrWithdrawAuthority,
                                           final PublicKey lockupAuthority,
                                           final StakeAuthorize stakeAuthorize) {
    return nativeProgramClient.authorizeStakeAccount(
        stakeAccount,
        stakeOrWithdrawAuthority,
        owner,
        lockupAuthority,
        stakeAuthorize
    );
  }

  @Override
  public Instruction authorizeStakeAccount(final PublicKey stakeAccount,
                                           final PublicKey stakeOrWithdrawAuthority,
                                           final StakeAuthorize stakeAuthorize) {
    return nativeProgramClient.authorizeStakeAccount(
        stakeAccount,
        stakeOrWithdrawAuthority,
        owner,
        stakeAuthorize
    );
  }

  @Override
  public Instruction authorizeStakeAccountChecked(final PublicKey stakeAccount,
                                                  final PublicKey stakeOrWithdrawAuthority,
                                                  final PublicKey newStakeOrWithdrawAuthority,
                                                  final StakeAuthorize stakeAuthorize) {
    return nativeProgramClient.authorizeStakeAccountChecked(
        stakeAccount,
        stakeOrWithdrawAuthority,
        owner,
        stakeAuthorize
    );
  }

  @Override
  public Instruction authorizeStakeAccountChecked(final PublicKey stakeAccount,
                                                  final PublicKey stakeOrWithdrawAuthority,
                                                  final StakeAuthorize stakeAuthorize) {
    return nativeProgramClient.authorizeStakeAccountChecked(
        stakeAccount,
        stakeOrWithdrawAuthority,
        owner,
        stakeAuthorize
    );
  }

  @Override
  public Instruction delegateStakeAccount(final PublicKey initializedStakeAccount,
                                          final PublicKey validatorVoteAccount) {
    return nativeProgramClient.delegateStakeAccount(initializedStakeAccount, validatorVoteAccount, owner);
  }

  @Override
  public Instruction deactivateStakeAccount(final PublicKey delegatedStakeAccount) {
    return nativeProgramClient.deactivateStakeAccount(delegatedStakeAccount, owner);
  }

  @Override
  public Instruction withdrawStakeAccount(final StakeAccount stakeAccount, final long lamports) {
    return nativeProgramClient.withdrawStakeAccount(
        stakeAccount,
        owner,
        lamports
    );
  }

  @Override
  public ProgramDerivedAddress findLookupTableAddress(final long recentSlot) {
    return AddressLookupTableProgram.findLookupTableAddress(solanaAccounts, feePayerPublicKey, recentSlot);
  }

  @Override
  public Instruction createLookupTable(final ProgramDerivedAddress uninitializedTableAccount, final long recentSlot) {
    return AddressLookupTableProgram.createLookupTable(
        solanaAccounts,
        uninitializedTableAccount.publicKey(),
        feePayerPublicKey,
        feePayerPublicKey,
        recentSlot,
        uninitializedTableAccount.nonce()
    );
  }

  @Override
  public Instruction freezeLookupTable(final PublicKey tableAccount) {
    return AddressLookupTableProgram.freezeLookupTable(
        solanaAccounts,
        tableAccount,
        feePayerPublicKey
    );
  }

  @Override
  public Instruction extendLookupTable(final PublicKey tableAccount, final List<PublicKey> newAddresses) {
    return AddressLookupTableProgram.extendLookupTable(
        solanaAccounts,
        tableAccount,
        feePayerPublicKey,
        feePayerPublicKey,
        newAddresses
    );
  }

  @Override
  public Instruction deactivateLookupTable(final PublicKey tableAccount) {
    return AddressLookupTableProgram.deactivateLookupTable(
        solanaAccounts,
        tableAccount,
        feePayerPublicKey
    );
  }

  @Override
  public Instruction closeLookupTable(final PublicKey tableAccount) {
    return AddressLookupTableProgram.closeLookupTable(
        solanaAccounts,
        tableAccount,
        feePayerPublicKey,
        feePayerPublicKey
    );
  }
}
