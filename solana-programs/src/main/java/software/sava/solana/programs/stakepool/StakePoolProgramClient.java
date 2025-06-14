package software.sava.solana.programs.stakepool;

import software.sava.core.accounts.ProgramDerivedAddress;
import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.tx.Instruction;
import software.sava.rpc.json.http.client.SolanaRpcClient;
import software.sava.rpc.json.http.response.AccountInfo;
import software.sava.solana.programs.clients.NativeProgramAccountClient;

import java.util.concurrent.CompletableFuture;

public interface StakePoolProgramClient {

  static StakePoolProgramClient createClient(final NativeProgramAccountClient nativeProgramClient,
                                             final StakePoolAccounts stakePoolAccounts) {
    return new StakePoolProgramClientImpl(nativeProgramClient, stakePoolAccounts);
  }

  static StakePoolProgramClient createClient(final NativeProgramAccountClient nativeProgramClient) {
    return createClient(nativeProgramClient, StakePoolAccounts.MAIN_NET);
  }

  NativeProgramAccountClient nativeProgramAccountClient();

  SolanaAccounts solanaAccounts();

  StakePoolAccounts stakePoolAccounts();

  static CompletableFuture<AccountInfo<StakePoolState>> fetchProgramState(final SolanaRpcClient rpcClient,
                                                                          final PublicKey stakePoolPublicKey) {
    return rpcClient.getAccountInfo(stakePoolPublicKey, StakePoolState.FACTORY);
  }

  static CompletableFuture<AccountInfo<ValidatorList>> fetchValidatorList(final SolanaRpcClient rpcClient,
                                                                          final StakePoolState programState) {
    final var destinationValidatorList = programState.validatorList();
    return rpcClient.getAccountInfo(destinationValidatorList, ValidatorList.FACTORY);
  }

  static ProgramDerivedAddress findStakePoolWithdrawAuthority(final AccountInfo<StakePoolState> stakePoolStateAccountInfo) {
    return StakePoolProgram.findStakePoolWithdrawAuthority(stakePoolStateAccountInfo.pubKey(), stakePoolStateAccountInfo.owner());
  }

  PublicKey ownerPublicKey();

  Instruction depositSol(final PublicKey stakePoolProgram,
                         final StakePoolState stakePoolState,
                         final PublicKey poolTokenATA,
                         final long lamportsIn);

  default Instruction depositSol(final AccountInfo<StakePoolState> stakePoolStateAccountInfo,
                                 final PublicKey poolTokenATA,
                                 final long lamportsIn) {
    return depositSol(
        stakePoolStateAccountInfo.owner(),
        stakePoolStateAccountInfo.data(),
        poolTokenATA,
        lamportsIn
    );
  }

  Instruction depositSolWithSlippage(final PublicKey stakePoolProgram,
                                     final StakePoolState stakePoolState,
                                     final PublicKey poolTokenATA,
                                     final long lamportsIn,
                                     final long minimumPoolTokensOut);

  default Instruction depositSolWithSlippage(final AccountInfo<StakePoolState> stakePoolStateAccountInfo,
                                             final PublicKey poolTokenATA,
                                             final long lamportsIn,
                                             final long minimumPoolTokensOut) {
    return depositSolWithSlippage(
        stakePoolStateAccountInfo.owner(),
        stakePoolStateAccountInfo.data(),
        poolTokenATA,
        lamportsIn,
        minimumPoolTokensOut
    );
  }

  Instruction depositStake(final PublicKey stakePoolProgram,
                           final StakePoolState stakePoolState,
                           final PublicKey depositStakeAccount,
                           final PublicKey validatorStakeAccount,
                           final PublicKey poolTokenATA);

  default Instruction depositStake(final AccountInfo<StakePoolState> stakePoolStateAccountInfo,
                                   final PublicKey depositStakeAccount,
                                   final PublicKey validatorStakeAccount,
                                   final PublicKey poolTokenATA) {
    return depositStake(
        stakePoolStateAccountInfo.owner(),
        stakePoolStateAccountInfo.data(),
        depositStakeAccount,
        validatorStakeAccount,
        poolTokenATA
    );
  }

  Instruction depositStakeWithSlippage(final PublicKey stakePoolProgram,
                                       final StakePoolState stakePoolState,
                                       final PublicKey depositStakeAccount,
                                       final PublicKey validatorStakeAccount,
                                       final PublicKey poolTokenATA,
                                       final long minimumPoolTokensOut);

  default Instruction depositStakeWithSlippage(final AccountInfo<StakePoolState> stakePoolStateAccountInfo,
                                               final PublicKey depositStakeAccount,
                                               final PublicKey validatorStakeAccount,
                                               final PublicKey poolTokenATA,
                                               final long minimumPoolTokensOut) {
    return depositStakeWithSlippage(
        stakePoolStateAccountInfo.owner(),
        stakePoolStateAccountInfo.data(),
        depositStakeAccount,
        validatorStakeAccount,
        poolTokenATA,
        minimumPoolTokensOut
    );
  }

  Instruction withdrawSol(final PublicKey stakePoolProgram,
                          final StakePoolState stakePoolState,
                          final PublicKey poolTokenATA,
                          final long poolTokenAmount);

  default Instruction withdrawSol(final AccountInfo<StakePoolState> stakePoolStateAccountInfo,
                                  final PublicKey poolTokenATA,
                                  final long poolTokenAmount) {
    return withdrawSol(
        stakePoolStateAccountInfo.owner(),
        stakePoolStateAccountInfo.data(),
        poolTokenATA,
        poolTokenAmount
    );
  }

  Instruction withdrawSolWithSlippage(final PublicKey stakePoolProgram,
                                      final StakePoolState stakePoolState,
                                      final PublicKey poolTokenATA,
                                      final long poolTokenAmount,
                                      final long lamportsOut);

  default Instruction withdrawSolWithSlippage(final AccountInfo<StakePoolState> stakePoolStateAccountInfo,
                                              final PublicKey poolTokenATA,
                                              final long poolTokenAmount,
                                              final long lamportsOut) {
    return withdrawSolWithSlippage(
        stakePoolStateAccountInfo.owner(),
        stakePoolStateAccountInfo.data(),
        poolTokenATA,
        poolTokenAmount,
        lamportsOut
    );
  }

  Instruction withdrawStake(final PublicKey poolProgram,
                            final StakePoolState stakePoolState,
                            final PublicKey validatorOrReserveStakeAccount,
                            final PublicKey uninitializedStakeAccount,
                            final PublicKey stakeAccountWithdrawalAuthority,
                            final PublicKey poolTokenATA,
                            final long poolTokenAmount);

  default Instruction withdrawStake(final AccountInfo<StakePoolState> stakePoolStateAccountInfo,
                                    final PublicKey validatorOrReserveStakeAccount,
                                    final PublicKey uninitializedStakeAccount,
                                    final PublicKey stakeAccountWithdrawalAuthority,
                                    final PublicKey poolTokenATA,
                                    final long poolTokenAmount) {
    return withdrawStake(
        stakePoolStateAccountInfo.owner(),
        stakePoolStateAccountInfo.data(),
        validatorOrReserveStakeAccount,
        uninitializedStakeAccount,
        stakeAccountWithdrawalAuthority,
        poolTokenATA,
        poolTokenAmount
    );
  }

  Instruction withdrawStakeWithSlippage(final PublicKey poolProgram,
                                        final StakePoolState stakePoolState,
                                        final PublicKey validatorOrReserveStakeAccount,
                                        final PublicKey uninitializedStakeAccount,
                                        final PublicKey stakeAccountWithdrawalAuthority,
                                        final PublicKey poolTokenATA,
                                        final long poolTokenAmount,
                                        final long lamportsOut);

  default Instruction withdrawStakeWithSlippage(final AccountInfo<StakePoolState> stakePoolStateAccountInfo,
                                                final PublicKey validatorOrReserveStakeAccount,
                                                final PublicKey uninitializedStakeAccount,
                                                final PublicKey stakeAccountWithdrawalAuthority,
                                                final PublicKey poolTokenATA,
                                                final long poolTokenAmount,
                                                final long lamportsOut) {
    return withdrawStakeWithSlippage(
        stakePoolStateAccountInfo.owner(),
        stakePoolStateAccountInfo.data(),
        validatorOrReserveStakeAccount,
        uninitializedStakeAccount,
        stakeAccountWithdrawalAuthority,
        poolTokenATA,
        poolTokenAmount,
        lamportsOut
    );
  }

  default Instruction withdrawStakeWithSlippage(final AccountInfo<StakePoolState> stakePoolStateAccountInfo,
                                                final PublicKey validatorOrReserveStakeAccount,
                                                final PublicKey uninitializedStakeAccount,
                                                final PublicKey poolTokenATA,
                                                final long poolTokenAmount,
                                                final long lamportsOut) {
    return withdrawStakeWithSlippage(
        stakePoolStateAccountInfo,
        validatorOrReserveStakeAccount,
        uninitializedStakeAccount,
        ownerPublicKey(),
        poolTokenATA,
        poolTokenAmount,
        lamportsOut
    );
  }

  default Instruction updateStakePoolBalance(final PublicKey poolProgram, final StakePoolState stakePoolState) {
    return StakePoolProgram.updateStakePoolBalance(
        AccountMeta.createInvoked(poolProgram),
        stakePoolState.address(),
        stakePoolState.validatorList(),
        stakePoolState.reserveStake(),
        stakePoolState.managerFeeAccount(),
        stakePoolState.poolMint(),
        stakePoolState.tokenProgramId()
    );
  }

  default Instruction updateStakePoolBalance(final AccountInfo<StakePoolState> stakePoolStateAccountInfo) {
    return updateStakePoolBalance(stakePoolStateAccountInfo.owner(), stakePoolStateAccountInfo.data());
  }
}
