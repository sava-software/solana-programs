package software.sava.solana.programs.serde;

import software.sava.core.encoding.ByteUtil;
import software.sava.core.serial.Serializable;

public interface SerdeEnum extends Serializable {

  static <E extends java.lang.Enum<?>> E read(final E[] values, final byte[] data, final int offset) {
    final int ordinal = ByteUtil.getInt32LE(data, offset);
    return values[ordinal];
  }

  int ordinal();

  default int l() {
    return Integer.BYTES;
  }

  default int write(final byte[] data, final int offset) {
    ByteUtil.putInt32LE(data, offset, ordinal());
    return l();
  }
}
