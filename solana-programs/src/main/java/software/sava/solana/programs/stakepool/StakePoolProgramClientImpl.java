package software.sava.solana.programs.stakepool;

import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.tx.Instruction;
import software.sava.solana.programs.clients.NativeProgramAccountClient;

final class StakePoolProgramClientImpl implements StakePoolProgramClient {

  private final NativeProgramAccountClient nativeProgramClient;
  private final SolanaAccounts accounts;
  private final StakePoolAccounts stakePoolAccounts;
  private final PublicKey owner;

  StakePoolProgramClientImpl(final NativeProgramAccountClient nativeProgramClient,
                             final StakePoolAccounts stakePoolAccounts) {
    this.nativeProgramClient = nativeProgramClient;
    this.accounts = nativeProgramClient.solanaAccounts();
    this.stakePoolAccounts = stakePoolAccounts;
    this.owner = nativeProgramClient.ownerPublicKey();
  }

  @Override
  public NativeProgramAccountClient nativeProgramAccountClient() {
    return nativeProgramClient;
  }

  @Override
  public SolanaAccounts solanaAccounts() {
    return accounts;
  }

  @Override
  public StakePoolAccounts stakePoolAccounts() {
    return stakePoolAccounts;
  }

  @Override
  public PublicKey ownerPublicKey() {
    return owner;
  }

  @Override
  public Instruction depositSol(final PublicKey stakePoolProgram,
                                final StakePoolState stakePoolState,
                                final PublicKey poolTokenATA,
                                final long lamportsIn) {
    return StakePoolProgram.depositSol(
        accounts,
        AccountMeta.createInvoked(stakePoolProgram),
        stakePoolState.address(),
        stakePoolState.reserveStake(),
        owner,
        poolTokenATA,
        stakePoolState.managerFeeAccount(),
        poolTokenATA,
        stakePoolState.poolMint(),
        stakePoolState.tokenProgramId(),
        lamportsIn
    );
  }

  @Override
  public Instruction depositSolWithSlippage(final PublicKey stakePoolProgram,
                                            final StakePoolState stakePoolState,
                                            final PublicKey poolTokenATA,
                                            final long lamportsIn,
                                            final long minimumPoolTokensOut) {
    return StakePoolProgram.depositSolWithSlippage(
        accounts,
        AccountMeta.createInvoked(stakePoolProgram),
        stakePoolState.address(),
        stakePoolState.reserveStake(),
        owner,
        poolTokenATA,
        stakePoolState.managerFeeAccount(),
        poolTokenATA,
        stakePoolState.poolMint(),
        stakePoolState.tokenProgramId(),
        lamportsIn,
        minimumPoolTokensOut
    );
  }

  @Override
  public Instruction depositStake(final PublicKey stakePoolProgram,
                                  final StakePoolState stakePoolState,
                                  final PublicKey depositStakeAccount,
                                  final PublicKey validatorStakeAccount,
                                  final PublicKey poolTokenATA) {
    return StakePoolProgram.depositStake(
        accounts,
        AccountMeta.createInvoked(stakePoolProgram),
        stakePoolState.address(),
        stakePoolState.validatorList(),
        owner,
        depositStakeAccount,
        validatorStakeAccount,
        stakePoolState.reserveStake(),
        poolTokenATA,
        stakePoolState.managerFeeAccount(),
        poolTokenATA,
        stakePoolState.poolMint(),
        stakePoolState.tokenProgramId()
    );
  }

  @Override
  public Instruction depositStakeWithSlippage(final PublicKey stakePoolProgram,
                                              final StakePoolState stakePoolState,
                                              final PublicKey depositStakeAccount,
                                              final PublicKey validatorStakeAccount,
                                              final PublicKey poolTokenATA,
                                              final long minimumPoolTokensOut) {
    return StakePoolProgram.depositStakeWithSlippage(
        accounts,
        AccountMeta.createInvoked(stakePoolProgram),
        stakePoolState.address(),
        stakePoolState.validatorList(),
        owner,
        depositStakeAccount,
        validatorStakeAccount,
        stakePoolState.reserveStake(),
        poolTokenATA,
        stakePoolState.managerFeeAccount(),
        poolTokenATA,
        stakePoolState.poolMint(),
        stakePoolState.tokenProgramId(),
        minimumPoolTokensOut
    );
  }


  @Override
  public Instruction withdrawSolWithSlippage(final PublicKey stakePoolProgram,
                                             final StakePoolState stakePoolState,
                                             final PublicKey poolTokenATA,
                                             final long poolTokenAmount,
                                             final long lamportsOut) {
    return StakePoolProgram.withdrawSolWithSlippage(
        accounts,
        AccountMeta.createInvoked(stakePoolProgram),
        stakePoolState.address(),
        owner,
        poolTokenATA,
        stakePoolState.reserveStake(),
        owner,
        stakePoolState.managerFeeAccount(),
        stakePoolState.poolMint(),
        stakePoolState.tokenProgramId(),
        poolTokenAmount,
        lamportsOut
    );
  }


  @Override
  public Instruction withdrawSol(final PublicKey stakePoolProgram,
                                 final StakePoolState stakePoolState,
                                 final PublicKey poolTokenATA,
                                 final long poolTokenAmount) {
    return StakePoolProgram.withdrawSol(
        accounts,
        AccountMeta.createInvoked(stakePoolProgram),
        stakePoolState.address(),
        owner,
        poolTokenATA,
        stakePoolState.reserveStake(),
        owner,
        stakePoolState.managerFeeAccount(),
        stakePoolState.poolMint(),
        stakePoolState.tokenProgramId(),
        poolTokenAmount
    );
  }

  @Override
  public Instruction withdrawStakeWithSlippage(final PublicKey poolProgram,
                                               final StakePoolState stakePoolState,
                                               final PublicKey validatorOrReserveStakeAccount,
                                               final PublicKey uninitializedStakeAccount,
                                               final PublicKey stakeAccountWithdrawalAuthority,
                                               final PublicKey poolTokenATA,
                                               final long poolTokenAmount,
                                               final long lamportsOut) {
    return StakePoolProgram.withdrawStakeWithSlippage(
        accounts,
        AccountMeta.createInvoked(poolProgram),
        stakePoolState.address(),
        stakePoolState.validatorList(),
        validatorOrReserveStakeAccount,
        uninitializedStakeAccount,
        stakeAccountWithdrawalAuthority,
        owner,
        poolTokenATA,
        stakePoolState.managerFeeAccount(),
        stakePoolState.poolMint(),
        stakePoolState.tokenProgramId(),
        poolTokenAmount,
        lamportsOut
    );
  }

  @Override
  public Instruction withdrawStake(final PublicKey poolProgram,
                                   final StakePoolState stakePoolState,
                                   final PublicKey validatorOrReserveStakeAccount,
                                   final PublicKey uninitializedStakeAccount,
                                   final PublicKey stakeAccountWithdrawalAuthority,
                                   final PublicKey poolTokenATA,
                                   final long poolTokenAmount) {
    return StakePoolProgram.withdrawStake(
        accounts,
        AccountMeta.createInvoked(poolProgram),
        stakePoolState.address(),
        stakePoolState.validatorList(),
        validatorOrReserveStakeAccount,
        uninitializedStakeAccount,
        stakeAccountWithdrawalAuthority,
        ownerPublicKey(),
        poolTokenATA,
        stakePoolState.managerFeeAccount(),
        stakePoolState.poolMint(),
        stakePoolState.tokenProgramId(),
        poolTokenAmount
    );
  }
}
