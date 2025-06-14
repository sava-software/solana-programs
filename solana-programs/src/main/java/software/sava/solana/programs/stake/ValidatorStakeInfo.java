package software.sava.solana.programs.stake;

import software.sava.core.accounts.PublicKey;
import software.sava.core.borsh.Borsh;
import software.sava.core.encoding.ByteUtil;

import static software.sava.core.accounts.PublicKey.PUBLIC_KEY_LENGTH;
import static software.sava.core.accounts.PublicKey.readPubKey;
import static software.sava.core.encoding.ByteUtil.getInt32LE;
import static software.sava.core.encoding.ByteUtil.getInt64LE;

public record ValidatorStakeInfo(long activeStakeLamports,
                                 long transientStakeLamports,
                                 long lastUpdateEpoch,
                                 long transientSeedSuffix,
                                 int unused,
                                 int validatorSeedSuffix,
                                 StakeStatus podStakeStatus,
                                 PublicKey voteAccountAddress) implements Borsh {

  public static int BYTES = Long.BYTES + Long.BYTES + Long.BYTES + Long.BYTES
      + Integer.BYTES + Integer.BYTES
      + 1
      + PUBLIC_KEY_LENGTH;

  public static ValidatorStakeInfo read(final byte[] data, int offset) {
    final long activeStakeLamports = getInt64LE(data, offset);
    offset += Long.BYTES;
    final long transientStakeLamports = getInt64LE(data, offset);
    offset += Long.BYTES;
    final long lastUpdateEpoch = getInt64LE(data, offset);
    offset += Long.BYTES;
    final long transientSeedSuffix = getInt64LE(data, offset);
    offset += Long.BYTES;
    final int unused = getInt32LE(data, offset);
    offset += Integer.BYTES;
    final int validatorSeedSuffix = getInt32LE(data, offset);
    offset += Integer.BYTES;
    final var podStakeStatus = StakeStatus.values()[data[offset] & 0xFF];
    ++offset;
    final var voteAccountAddress = readPubKey(data, offset);
    return new ValidatorStakeInfo(
        activeStakeLamports,
        transientStakeLamports,
        lastUpdateEpoch,
        transientSeedSuffix,
        unused,
        validatorSeedSuffix,
        podStakeStatus,
        voteAccountAddress
    );
  }

  @Override
  public int write(final byte[] data, final int offset) {
    ByteUtil.putInt64LE(data, offset, activeStakeLamports);
    int i = offset + Long.BYTES;
    ByteUtil.putInt64LE(data, i, transientStakeLamports);
    i += Long.BYTES;
    ByteUtil.putInt64LE(data, i, lastUpdateEpoch);
    i += Long.BYTES;
    ByteUtil.putInt64LE(data, i, transientSeedSuffix);
    i += Long.BYTES;
    ByteUtil.putInt32LE(data, i, unused);
    i += Integer.BYTES;
    ByteUtil.putInt32LE(data, i, validatorSeedSuffix);
    i += Integer.BYTES;
    data[i] = (byte) podStakeStatus.ordinal();
    ++i;
    voteAccountAddress.write(data, i);
    return i + PUBLIC_KEY_LENGTH;
  }

  @Override
  public int l() {
    return BYTES;
  }
}
