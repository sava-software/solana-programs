package software.sava.solana.programs.clients;

import software.sava.core.accounts.AccountWithSeed;
import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.accounts.lookup.AddressLookupTable;
import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.accounts.sysvar.Clock;
import software.sava.core.rpc.Filter;
import software.sava.core.tx.Instruction;
import software.sava.rpc.json.http.client.SolanaRpcClient;
import software.sava.rpc.json.http.response.AccountInfo;
import software.sava.solana.programs.stake.*;
import software.sava.solana.programs.system.SystemProgram;
import software.sava.solana.programs.token.TokenProgram;

import java.time.Instant;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;

import static software.sava.core.rpc.Filter.createMemCompFilter;
import static software.sava.solana.programs.compute_budget.ComputeBudgetProgram.setComputeUnitLimit;
import static software.sava.solana.programs.compute_budget.ComputeBudgetProgram.setComputeUnitPrice;
import static software.sava.solana.programs.stake.StakeAccount.*;

record NativeProgramClientImpl(SolanaAccounts accounts) implements NativeProgramClient {

  @Override
  public NativeProgramAccountClient createAccountClient(final AccountMeta ownerAndFeePayer) {
    return new NativeProgramAccountClientImpl(this, ownerAndFeePayer.publicKey(), ownerAndFeePayer);
  }

  @Override
  public NativeProgramAccountClient createAccountClient(final PublicKey owner, final AccountMeta feePayer) {
    return new NativeProgramAccountClientImpl(this, owner, feePayer);
  }

  @Override
  public CompletableFuture<AccountInfo<Clock>> fetchClockSysVar(final SolanaRpcClient rpcClient) {
    return rpcClient.getAccountInfo(accounts.clockSysVar(), Clock.FACTORY);
  }

  @Override
  public Instruction computeUnitLimit(final int computeUnitLimit) {
    return setComputeUnitLimit(accounts.invokedComputeBudgetProgram(), computeUnitLimit);
  }

  @Override
  public Instruction computeUnitPrice(final long computeUnitPrice) {
    return setComputeUnitPrice(accounts.invokedComputeBudgetProgram(), computeUnitPrice);
  }

  @Override
  public Instruction allocateAccountSpace(final PublicKey newAccountPublicKey, final long space) {
    return SystemProgram.allocate(accounts.invokedSystemProgram(), newAccountPublicKey, space);
  }

  @Override
  public Instruction syncNative(final PublicKey tokenAccount) {
    return TokenProgram.syncNative(accounts.invokedTokenProgram(), tokenAccount);
  }

  private CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccounts(final SolanaRpcClient rpcClient,
                                                                                final List<Filter> filters) {
    return rpcClient.getProgramAccounts(accounts.stakeProgram(), filters, StakeAccount.FACTORY);
  }

  private CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccounts(final SolanaRpcClient rpcClient,
                                                                                final PublicKey key,
                                                                                final int offset) {
    return fetchStakeAccounts(rpcClient, List.of(
            StakeAccount.DATA_SIZE_FILTER,
            createMemCompFilter(offset, key)
        )
    );
  }

  private CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccounts(final SolanaRpcClient rpcClient,
                                                                                final StakeState stakeState,
                                                                                final PublicKey key,
                                                                                final int offset) {
    if (stakeState == null) {
      return fetchStakeAccounts(rpcClient, key, offset);
    } else {
      return fetchStakeAccounts(rpcClient, List.of(
              StakeAccount.DATA_SIZE_FILTER,
              createStateFilter(stakeState),
              createMemCompFilter(offset, key)
          )
      );
    }
  }

  @Override
  public CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsByStakeAuthority(final SolanaRpcClient rpcClient,
                                                                                               final StakeState stakeState,
                                                                                               final PublicKey staker) {
    return fetchStakeAccounts(rpcClient, stakeState, staker, STAKE_AUTHORITY_OFFSET);
  }

  @Override
  public CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsByWithdrawAuthority(final SolanaRpcClient rpcClient,
                                                                                                  final StakeState stakeState,
                                                                                                  final PublicKey withdrawer) {
    return fetchStakeAccounts(rpcClient, stakeState, withdrawer, WITHDRAW_AUTHORITY_OFFSET);
  }

  @Override
  public CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsByStakeAndWithdrawAuthority(final SolanaRpcClient rpcClient,
                                                                                                          final StakeState stakeState,
                                                                                                          final PublicKey withdrawAuthority) {
    return fetchStakeAccounts(rpcClient, List.of(
            StakeAccount.DATA_SIZE_FILTER,
            createStateFilter(stakeState),
            createMemCompFilter(STAKE_AUTHORITY_OFFSET, withdrawAuthority, withdrawAuthority)
        )
    );
  }

  @Override
  public CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsWithCustodian(final SolanaRpcClient rpcClient,
                                                                                            final StakeState stakeState,
                                                                                            final PublicKey custodian) {
    return fetchStakeAccounts(rpcClient, stakeState, custodian, LOCKUP_CUSTODIAN_OFFSET);
  }

  @Override
  public CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsForValidator(final SolanaRpcClient rpcClient,
                                                                                           final StakeState stakeState,
                                                                                           final PublicKey voteAccount) {
    return fetchStakeAccounts(rpcClient, stakeState, voteAccount, VOTER_PUBLIC_KEY_OFFSET);
  }

  @Override
  public CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsForValidatorAndWithdrawAuthority(final SolanaRpcClient rpcClient,
                                                                                                               final StakeState stakeState,
                                                                                                               final PublicKey voteAccount,
                                                                                                               final PublicKey withdrawAuthority) {
    return fetchStakeAccounts(rpcClient, List.of(
            StakeAccount.DATA_SIZE_FILTER,
            createStateFilter(stakeState),
            createMemCompFilter(WITHDRAW_AUTHORITY_OFFSET, withdrawAuthority),
            createMemCompFilter(VOTER_PUBLIC_KEY_OFFSET, voteAccount)
        )
    );
  }

  @Override
  public CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsForValidatorAndStakeAndWithdrawAuthority(
      final SolanaRpcClient rpcClient,
      final StakeState stakeState,
      final PublicKey voteAccount,
      final PublicKey withdrawAuthority) {
    return fetchStakeAccounts(rpcClient, List.of(
            StakeAccount.DATA_SIZE_FILTER,
            createStateFilter(stakeState),
            createMemCompFilter(STAKE_AUTHORITY_OFFSET, withdrawAuthority, withdrawAuthority),
            createMemCompFilter(VOTER_PUBLIC_KEY_OFFSET, voteAccount)
        )
    );
  }

  @Override
  public CompletableFuture<List<AccountInfo<AddressLookupTable>>> fetchLookupTableAccountsByAuthority(final SolanaRpcClient rpcClient,
                                                                                                      final PublicKey authority) {
    final var filters = List.of(createMemCompFilter(AddressLookupTable.AUTHORITY_OFFSET, authority));
    return rpcClient.getProgramAccounts(accounts.addressLookupTableProgram(), filters, AddressLookupTable.FACTORY);
  }

  @Override
  public Instruction deactivateDelinquentStake(final PublicKey delegatedStakeAccount,
                                               final PublicKey delinquentVoteAccount,
                                               final PublicKey referenceVoteAccount) {
    return StakeProgram.deactivateDelinquent(
        accounts,
        delegatedStakeAccount,
        delinquentVoteAccount,
        referenceVoteAccount
    );
  }

  @Override
  public Instruction setStakeAccountLockup(final PublicKey initializedStakeAccount,
                                           final PublicKey lockupOrWithdrawAuthority,
                                           final Instant timestamp,
                                           final OptionalLong epoch,
                                           final PublicKey custodian) {
    return StakeProgram.setLockup(
        accounts,
        initializedStakeAccount,
        lockupOrWithdrawAuthority,
        timestamp,
        epoch,
        custodian
    );
  }

  @Override
  public Instruction setStakeAccountLockupChecked(final PublicKey initializedStakeAccount,
                                                  final PublicKey lockupOrWithdrawAuthority,
                                                  final PublicKey newLockupAuthority,
                                                  final Instant timestamp,
                                                  final OptionalLong epoch) {
    return StakeProgram.setLockupChecked(
        accounts,
        initializedStakeAccount,
        lockupOrWithdrawAuthority,
        newLockupAuthority,
        timestamp,
        epoch
    );
  }

  @Override
  public Instruction authorizeStakeAccount(final PublicKey stakeAccount,
                                           final PublicKey stakeOrWithdrawAuthority,
                                           final PublicKey lockupAuthority,
                                           final PublicKey newAuthority,
                                           final StakeAuthorize stakeAuthorize) {
    return StakeProgram.authorize(
        accounts,
        stakeAccount,
        stakeOrWithdrawAuthority,
        lockupAuthority,
        newAuthority,
        stakeAuthorize
    );
  }

  @Override
  public Instruction authorizeStakeAccountChecked(final PublicKey stakeAccount,
                                                  final PublicKey stakeOrWithdrawAuthority,
                                                  final PublicKey newStakeOrWithdrawAuthority,
                                                  final PublicKey lockupAuthority,
                                                  final StakeAuthorize stakeAuthorize) {
    return StakeProgram.authorizeChecked(
        accounts,
        stakeAccount,
        stakeOrWithdrawAuthority,
        newStakeOrWithdrawAuthority,
        lockupAuthority,
        stakeAuthorize
    );
  }

  @Override
  public Instruction authorizeStakeAccountWithSeed(final PublicKey stakeAccount,
                                                   final AccountWithSeed baseKeyOrWithdrawAuthority,
                                                   final PublicKey lockupAuthority,
                                                   final PublicKey newAuthorizedPublicKey,
                                                   final StakeAuthorize stakeAuthorize,
                                                   final PublicKey authorityOwner) {
    return StakeProgram.authorizeWithSeed(
        accounts,
        stakeAccount,
        baseKeyOrWithdrawAuthority,
        lockupAuthority,
        newAuthorizedPublicKey,
        stakeAuthorize,
        authorityOwner
    );
  }

  @Override
  public Instruction authorizeStakeAccountCheckedWithSeed(final PublicKey stakeAccount,
                                                          final AccountWithSeed baseKeyOrWithdrawAuthority,
                                                          final PublicKey stakeOrWithdrawAuthority,
                                                          final PublicKey lockupAuthority,
                                                          final StakeAuthorize stakeAuthorize,
                                                          final PublicKey authorityOwner) {
    return StakeProgram.authorizeCheckedWithSeed(
        accounts,
        stakeAccount,
        baseKeyOrWithdrawAuthority,
        stakeOrWithdrawAuthority,
        lockupAuthority,
        stakeAuthorize,
        authorityOwner
    );
  }

  @Override
  public Instruction initializeStakeAccount(final PublicKey unInitializedStakeAccount,
                                            final PublicKey staker,
                                            final PublicKey withdrawer,
                                            final LockUp lockUp) {
    return StakeProgram.initialize(
        accounts,
        unInitializedStakeAccount,
        staker,
        withdrawer,
        lockUp
    );
  }

  @Override
  public Instruction initializeStakeAccountChecked(final PublicKey unInitializedStakeAccount,
                                                   final PublicKey staker,
                                                   final PublicKey withdrawer) {
    return StakeProgram.initializeChecked(
        accounts,
        unInitializedStakeAccount,
        staker,
        withdrawer
    );
  }


  @Override
  public Instruction delegateStakeAccount(final PublicKey initializedStakeAccount,
                                          final PublicKey validatorVoteAccount,
                                          final PublicKey stakeAuthority) {
    return StakeProgram.delegateStake(
        accounts,
        initializedStakeAccount,
        validatorVoteAccount,
        stakeAuthority
    );
  }

  @Override
  public Instruction reDelegateStakeAccount(final StakeAccount delegatedStakeAccount,
                                            final PublicKey uninitializedStakeAccount,
                                            final PublicKey validatorVoteAccount) {
    return StakeProgram.reDelegate(
        accounts,
        delegatedStakeAccount.address(),
        uninitializedStakeAccount,
        validatorVoteAccount,
        delegatedStakeAccount.stakeAuthority()
    );
  }

  @Override
  public Instruction splitStakeAccount(final StakeAccount splitStakeAccount,
                                       final PublicKey unInitializedStakeAccount,
                                       final long lamports) {
    return StakeProgram.split(
        accounts,
        splitStakeAccount.address(),
        unInitializedStakeAccount,
        splitStakeAccount.stakeAuthority(),
        lamports
    );
  }

  @Override
  public Instruction mergeStakeAccounts(final StakeAccount destinationStakeAccount,
                                        final PublicKey srcStakeAccount) {
    return StakeProgram.merge(
        accounts,
        destinationStakeAccount.address(),
        srcStakeAccount,
        destinationStakeAccount.stakeAuthority()
    );
  }

  public Instruction withdrawStakeAccount(final StakeAccount stakeAccount,
                                          final PublicKey recipient,
                                          final long lamports) {
    final var lockup = stakeAccount.lockup();
    if (lockup == null || lockup.equals(LockUp.NO_LOCKUP)) {
      return StakeProgram.withdraw(
          accounts,
          stakeAccount.address(),
          recipient,
          stakeAccount.withdrawAuthority(),
          lamports
      );
    } else {
      return StakeProgram.withdraw(
          accounts,
          stakeAccount.address(),
          recipient,
          stakeAccount.withdrawAuthority(),
          lockup.custodian(),
          lamports
      );
    }
  }

  @Override
  public Instruction deactivateStakeAccount(final PublicKey delegatedStakeAccount, final PublicKey stakeAuthority) {
    return StakeProgram.deactivate(accounts, delegatedStakeAccount, stakeAuthority);
  }

  @Override
  public Instruction moveStake(final StakeAccount sourceStakeAccount,
                               final PublicKey destinationStakeAccount,
                               final long lamports) {
    return StakeProgram.moveStake(
        accounts,
        sourceStakeAccount.address(),
        destinationStakeAccount,
        sourceStakeAccount.stakeAuthority(),
        lamports
    );
  }

  @Override
  public Instruction moveLamports(final StakeAccount sourceStakeAccount,
                                  final PublicKey destinationStakeAccount,
                                  final long lamports) {
    return StakeProgram.moveLamports(
        accounts,
        sourceStakeAccount.address(),
        destinationStakeAccount,
        sourceStakeAccount.stakeAuthority(),
        lamports
    );
  }
}
