package software.sava.solana.programs.stakepool;

import software.sava.core.borsh.RustEnum;

public sealed interface FeeType extends RustEnum permits
    FeeType.SolReferral,
    FeeType.StakeReferral,
    FeeType.Epoch,
    FeeType.StakeWithdrawal,
    FeeType.SolDeposit,
    FeeType.StakeDeposit,
    FeeType.SolWithdrawal {


  record SolReferral(int val) implements EnumInt8, FeeType {

    @Override
    public int ordinal() {
      return 0;
    }
  }

  record StakeReferral(int val) implements EnumInt8, FeeType {

    @Override
    public int ordinal() {
      return 1;
    }
  }

  record Epoch(StakePoolState.Fee val) implements BorshEnum, FeeType {

    @Override
    public int ordinal() {
      return 2;
    }
  }

  record StakeWithdrawal(StakePoolState.Fee val) implements BorshEnum, FeeType {

    @Override
    public int ordinal() {
      return 3;
    }
  }

  record SolDeposit(StakePoolState.Fee val) implements BorshEnum, FeeType {

    @Override
    public int ordinal() {
      return 4;
    }
  }

  record StakeDeposit(StakePoolState.Fee val) implements BorshEnum, FeeType {

    @Override
    public int ordinal() {
      return 5;
    }
  }

  record SolWithdrawal(StakePoolState.Fee val) implements BorshEnum, FeeType {

    @Override
    public int ordinal() {
      return 6;
    }
  }
}
