package software.sava.solana.programs.stake;

import software.sava.solana.programs.serde.SerdeEnum;

public enum StakeAuthorize implements SerdeEnum {

  Staker,
  Withdrawer;

  public static StakeAuthorize read(final byte[] data, final int offset) {
    return SerdeEnum.read(StakeAuthorize.values(), data, offset);
  }
}
