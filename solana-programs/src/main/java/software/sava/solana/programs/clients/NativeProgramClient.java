package software.sava.solana.programs.clients;

import software.sava.core.accounts.AccountWithSeed;
import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.accounts.lookup.AddressLookupTable;
import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.accounts.sysvar.Clock;
import software.sava.core.tx.Instruction;
import software.sava.rpc.json.http.client.SolanaRpcClient;
import software.sava.rpc.json.http.response.AccountInfo;
import software.sava.solana.programs.stake.LockUp;
import software.sava.solana.programs.stake.StakeAccount;
import software.sava.solana.programs.stake.StakeAuthorize;
import software.sava.solana.programs.stake.StakeState;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;

public interface NativeProgramClient {

  static NativeProgramClient createClient(final SolanaAccounts programs) {
    return new NativeProgramClientImpl(programs);
  }

  static NativeProgramClient createClient() {
    return createClient(SolanaAccounts.MAIN_NET);
  }

  static CompletableFuture<Long> getMinimumBalanceForStakeAccount(final SolanaRpcClient rpcClient) {
    return rpcClient.getMinimumBalanceForRentExemption(StakeAccount.BYTES);
  }

  SolanaAccounts accounts();

  NativeProgramAccountClient createAccountClient(final AccountMeta ownerAndFeePayer);

  NativeProgramAccountClient createAccountClient(final PublicKey owner, final AccountMeta feePayer);

  CompletableFuture<AccountInfo<Clock>> fetchClockSysVar(final SolanaRpcClient rpcClient);

  Instruction computeUnitLimit(final int computeUnitLimit);

  Instruction computeUnitPrice(final long computeUnitPrice);

  Instruction allocateAccountSpace(final PublicKey newAccountPublicKey, final long space);

  Instruction syncNative(final PublicKey tokenAccount);

  default Instruction allocateStakeAccount(final PublicKey newAccountPublicKey) {
    return allocateAccountSpace(newAccountPublicKey, StakeAccount.BYTES);
  }

  CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsByStakeAuthority(final SolanaRpcClient rpcClient,
                                                                                        final StakeState stakeState,
                                                                                        final PublicKey staker);

  default CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsByStakeAuthority(final SolanaRpcClient rpcClient,
                                                                                                final PublicKey staker) {
    return fetchStakeAccountsByStakeAuthority(rpcClient, StakeState.Stake, staker);
  }

  CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsByWithdrawAuthority(final SolanaRpcClient rpcClient,
                                                                                           final StakeState stakeState,
                                                                                           final PublicKey withdrawer);

  default CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsByWithdrawAuthority(final SolanaRpcClient rpcClient,
                                                                                                   final PublicKey withdrawer) {
    return fetchStakeAccountsByWithdrawAuthority(rpcClient, StakeState.Stake, withdrawer);
  }

  CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsByStakeAndWithdrawAuthority(final SolanaRpcClient rpcClient,
                                                                                                   final StakeState stakeState,
                                                                                                   final PublicKey withdrawer);

  default CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsByStakeAndWithdrawAuthority(final SolanaRpcClient rpcClient,
                                                                                                           final PublicKey withdrawer) {
    return fetchStakeAccountsByStakeAndWithdrawAuthority(rpcClient, StakeState.Stake, withdrawer);
  }

  CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsWithCustodian(final SolanaRpcClient rpcClient,
                                                                                     final StakeState stakeState,
                                                                                     final PublicKey custodian);

  default CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsWithCustodian(final SolanaRpcClient rpcClient,
                                                                                             final PublicKey custodian) {
    return fetchStakeAccountsWithCustodian(rpcClient, StakeState.Stake, custodian);
  }

  CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsForValidator(final SolanaRpcClient rpcClient,
                                                                                    final StakeState stakeState,
                                                                                    final PublicKey voteAccount);

  default CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsForValidator(final SolanaRpcClient rpcClient,
                                                                                            final PublicKey voteAccount) {
    return fetchStakeAccountsForValidator(rpcClient, StakeState.Stake, voteAccount);
  }

  CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsForValidatorAndWithdrawAuthority(final SolanaRpcClient rpcClient,
                                                                                                        final StakeState stakeState,
                                                                                                        final PublicKey voteAccount,
                                                                                                        final PublicKey withdrawAuthority);

  default CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsForValidatorAndWithdrawAuthority(final SolanaRpcClient rpcClient,
                                                                                                                final PublicKey voteAccount,
                                                                                                                final PublicKey withdrawAuthority) {
    return fetchStakeAccountsForValidatorAndWithdrawAuthority(rpcClient, StakeState.Stake, voteAccount, withdrawAuthority);
  }

  CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsForValidatorAndStakeAndWithdrawAuthority(final SolanaRpcClient rpcClient,
                                                                                                                final StakeState stakeState,
                                                                                                                final PublicKey voteAccount,
                                                                                                                final PublicKey withdrawAuthority);

  default CompletableFuture<List<AccountInfo<StakeAccount>>> fetchStakeAccountsForValidatorAndStakeAndWithdrawAuthority(
      final SolanaRpcClient rpcClient,
      final PublicKey voteAccount,
      final PublicKey withdrawAuthority) {
    return fetchStakeAccountsForValidatorAndStakeAndWithdrawAuthority(rpcClient, StakeState.Stake, voteAccount, withdrawAuthority);
  }

  CompletableFuture<List<AccountInfo<AddressLookupTable>>> fetchLookupTableAccountsByAuthority(final SolanaRpcClient rpcClient,
                                                                                               final PublicKey authority);

  Instruction deactivateDelinquentStake(final PublicKey delegatedStakeAccount,
                                        final PublicKey delinquentVoteAccount,
                                        final PublicKey referenceVoteAccount);

  Instruction setStakeAccountLockup(final PublicKey initializedStakeAccount,
                                    final PublicKey lockupOrWithdrawAuthority,
                                    final Instant timestamp,
                                    final OptionalLong epoch,
                                    final PublicKey custodian);

  default Instruction setStakeAccountLockupChecked(final PublicKey initializedStakeAccount,
                                                   final PublicKey lockupOrWithdrawAuthority,
                                                   final Instant timestamp,
                                                   final OptionalLong epoch) {
    return setStakeAccountLockupChecked(initializedStakeAccount, lockupOrWithdrawAuthority, null, timestamp, epoch);
  }

  Instruction setStakeAccountLockupChecked(final PublicKey initializedStakeAccount,
                                           final PublicKey lockupOrWithdrawAuthority,
                                           final PublicKey newLockupAuthority,
                                           final Instant timestamp,
                                           final OptionalLong epoch);

  Instruction authorizeStakeAccount(final PublicKey stakeAccount,
                                    final PublicKey stakeOrWithdrawAuthority,
                                    final PublicKey lockupAuthority,
                                    final PublicKey newAuthority,
                                    final StakeAuthorize stakeAuthorize);

  default Instruction authorizeStakeAccount(final PublicKey stakeAccount,
                                            final PublicKey stakeOrWithdrawAuthority,
                                            final PublicKey newAuthority,
                                            final StakeAuthorize stakeAuthorize) {
    return authorizeStakeAccount(
        stakeAccount,
        stakeOrWithdrawAuthority,
        null,
        newAuthority,
        stakeAuthorize
    );
  }

  default Instruction authorizeStakeAccount(final StakeAccount stakeAccount,
                                            final PublicKey newAuthority,
                                            final StakeAuthorize stakeAuthorize) {
    return authorizeStakeAccount(
        stakeAccount.address(),
        stakeAuthorize == StakeAuthorize.Staker
            ? stakeAccount.stakeAuthority()
            : stakeAccount.withdrawAuthority(),
        newAuthority,
        stakeAuthorize
    );
  }

  Instruction authorizeStakeAccountChecked(final PublicKey stakeAccount,
                                           final PublicKey stakeOrWithdrawAuthority,
                                           final PublicKey newStakeOrWithdrawAuthority,
                                           final PublicKey lockupAuthority,
                                           final StakeAuthorize stakeAuthorize);

  default Instruction authorizeStakeAccountChecked(final PublicKey stakeAccount,
                                                   final PublicKey stakeOrWithdrawAuthority,
                                                   final PublicKey newStakeOrWithdrawAuthority,
                                                   final StakeAuthorize stakeAuthorize) {
    return authorizeStakeAccountChecked(
        stakeAccount,
        stakeOrWithdrawAuthority,
        newStakeOrWithdrawAuthority,
        null,
        stakeAuthorize
    );
  }

  default Instruction authorizeStakeAccountChecked(final StakeAccount stakeAccount,
                                                   final PublicKey newAuthority,
                                                   final StakeAuthorize stakeAuthorize) {
    return authorizeStakeAccountChecked(
        stakeAccount.address(),
        stakeAuthorize == StakeAuthorize.Staker
            ? stakeAccount.stakeAuthority()
            : stakeAccount.withdrawAuthority(),
        newAuthority,
        stakeAuthorize
    );
  }

  Instruction authorizeStakeAccountWithSeed(final PublicKey stakeAccount,
                                            final AccountWithSeed baseKeyOrWithdrawAuthority,
                                            final PublicKey lockupAuthority,
                                            final PublicKey newAuthorizedPublicKey,
                                            final StakeAuthorize stakeAuthorize,
                                            final PublicKey authorityOwner);

  default Instruction authorizeStakeAccountWithSeed(final PublicKey stakeAccount,
                                                    final AccountWithSeed baseKeyOrWithdrawAuthority,
                                                    final PublicKey newAuthorizedPublicKey,
                                                    final StakeAuthorize stakeAuthorize,
                                                    final PublicKey authorityOwner) {
    return authorizeStakeAccountWithSeed(
        stakeAccount,
        baseKeyOrWithdrawAuthority,
        null,
        newAuthorizedPublicKey,
        stakeAuthorize,
        authorityOwner
    );
  }

  Instruction authorizeStakeAccountCheckedWithSeed(final PublicKey stakeAccount,
                                                   final AccountWithSeed baseKeyOrWithdrawAuthority,
                                                   final PublicKey stakeOrWithdrawAuthority,
                                                   final PublicKey lockupAuthority,
                                                   final StakeAuthorize stakeAuthorize,
                                                   final PublicKey authorityOwner);

  Instruction initializeStakeAccount(final PublicKey unInitializedStakeAccount,
                                     final PublicKey staker,
                                     final PublicKey withdrawer,
                                     final LockUp lockUp);

  default Instruction initializeStakeAccount(final PublicKey unInitializedStakeAccount,
                                             final PublicKey staker,
                                             final PublicKey withdrawer) {
    return initializeStakeAccount(unInitializedStakeAccount, staker, withdrawer, LockUp.NO_LOCKUP);
  }

  Instruction initializeStakeAccountChecked(final PublicKey unInitializedStakeAccount,
                                            final PublicKey staker,
                                            final PublicKey withdrawer);

  Instruction delegateStakeAccount(final PublicKey initializedStakeAccount,
                                   final PublicKey validatorVoteAccount,
                                   final PublicKey stakeAuthority);

  default Instruction delegateStakeAccount(final StakeAccount initializedStakeAccount,
                                           final PublicKey validatorVoteAccount) {
    return delegateStakeAccount(
        initializedStakeAccount.address(),
        validatorVoteAccount,
        initializedStakeAccount.stakeAuthority()
    );
  }

  Instruction reDelegateStakeAccount(final StakeAccount delegatedStakeAccount,
                                     final PublicKey uninitializedStakeAccount,
                                     final PublicKey validatorVoteAccount);

  Instruction splitStakeAccount(final StakeAccount splitStakeAccount,
                                final PublicKey unInitializedStakeAccount,
                                final long lamports);

  Instruction mergeStakeAccounts(final StakeAccount destinationStakeAccount,
                                 final PublicKey srcStakeAccount);

  default Instruction mergeStakeAccounts(final StakeAccount destinationStakeAccount,
                                         final StakeAccount srcStakeAccount) {
    return mergeStakeAccounts(destinationStakeAccount, srcStakeAccount.address());
  }

  default List<Instruction> mergeStakeAccountKeysInto(final StakeAccount destinationStakeAccount,
                                                      final Collection<PublicKey> stakeAccounts) {
    return stakeAccounts.stream()
        .map(stakeAccount -> mergeStakeAccounts(destinationStakeAccount, stakeAccount))
        .toList();
  }

  default List<Instruction> mergeStakeAccountsInto(final StakeAccount destinationStakeAccount,
                                                   final Collection<StakeAccount> stakeAccounts) {
    return stakeAccounts.stream()
        .map(StakeAccount::address)
        .map(stakeAccount -> mergeStakeAccounts(destinationStakeAccount, stakeAccount))
        .toList();
  }

  default List<Instruction> mergeStakeAccountInfosInto(final StakeAccount destinationStakeAccount,
                                                       final Collection<AccountInfo<StakeAccount>> stakeAccounts) {
    return stakeAccounts.stream()
        .map(AccountInfo::data)
        .map(StakeAccount::address)
        .map(stakeAccount -> mergeStakeAccounts(destinationStakeAccount, stakeAccount))
        .toList();
  }

  default List<Instruction> mergeStakeAccounts(final List<StakeAccount> stakeAccounts) {
    if (stakeAccounts.size() < 2) {
      return List.of();
    } else {
      final var mergeInto = stakeAccounts.getFirst();
      return stakeAccounts.stream().skip(1)
          .map(StakeAccount::address)
          .map(stakeAccount -> mergeStakeAccounts(mergeInto, stakeAccount))
          .toList();
    }
  }

  default List<Instruction> mergeStakeAccountInfos(final List<AccountInfo<StakeAccount>> stakeAccounts) {
    if (stakeAccounts.size() < 2) {
      return List.of();
    } else {
      final var mergeInto = stakeAccounts.getFirst().data();
      return stakeAccounts.stream().skip(1)
          .map(AccountInfo::data)
          .map(StakeAccount::address)
          .map(stakeAccount -> mergeStakeAccounts(mergeInto, stakeAccount))
          .toList();
    }
  }

  default List<Instruction> mergeStakeAccounts(final Collection<StakeAccount> stakeAccounts) {
    if (stakeAccounts.size() < 2) {
      return List.of();
    } else {
      final var array = stakeAccounts.toArray(StakeAccount[]::new);
      final var mergeInto = array[0];
      return Arrays.stream(array, 1, array.length)
          .map(StakeAccount::address)
          .map(stakeAccount -> mergeStakeAccounts(mergeInto, stakeAccount))
          .toList();
    }
  }

  default List<Instruction> mergeStakeAccountInfos(final Collection<AccountInfo<StakeAccount>> stakeAccounts) {
    @SuppressWarnings("unchecked") final AccountInfo<StakeAccount>[] array = stakeAccounts.toArray(AccountInfo[]::new);
    final var mergeInto = (StakeAccount) array[0].data();
    return Arrays.stream(array, 1, array.length)
        .map(AccountInfo::data)
        .map(StakeAccount::address)
        .map(stakeAccount -> mergeStakeAccounts(mergeInto, stakeAccount))
        .toList();
  }

  Instruction withdrawStakeAccount(final StakeAccount stakeAccount,
                                   final PublicKey recipient,
                                   final long lamports);

  Instruction deactivateStakeAccount(final PublicKey delegatedStakeAccount, final PublicKey stakeAuthority);

  default Instruction deactivateStakeAccount(final StakeAccount delegatedStakeAccount) {
    return deactivateStakeAccount(delegatedStakeAccount.address(), delegatedStakeAccount.stakeAuthority());
  }

  default List<Instruction> deactivateStakeAccountInfos(final Collection<AccountInfo<StakeAccount>> delegatedStakeAccounts) {
    return delegatedStakeAccounts.stream().map(AccountInfo::data).map(this::deactivateStakeAccount).toList();
  }

  default List<Instruction> deactivateStakeAccounts(final Collection<StakeAccount> delegatedStakeAccounts) {
    return delegatedStakeAccounts.stream().map(this::deactivateStakeAccount).toList();
  }

  Instruction moveStake(final StakeAccount sourceStakeAccount,
                        final PublicKey destinationStakeAccount,
                        final long lamports);

  Instruction moveLamports(final StakeAccount sourceStakeAccount,
                           final PublicKey destinationStakeAccount,
                           final long lamports);
}
