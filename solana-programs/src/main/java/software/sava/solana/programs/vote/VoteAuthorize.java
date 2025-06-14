package software.sava.solana.programs.vote;

import software.sava.solana.programs.serde.SerdeEnum;

public enum VoteAuthorize implements SerdeEnum {

  Voter,
  Withdrawer;

  public static VoteAuthorize read(final byte[] data, final int offset) {
    return SerdeEnum.read(VoteAuthorize.values(), data, offset);
  }
}
