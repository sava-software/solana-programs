package software.sava.solana.programs.serde;

import software.sava.core.encoding.ByteUtil;

import java.time.Instant;

import static software.sava.core.encoding.ByteUtil.putInt64LE;
import static software.sava.core.programs.Discriminator.NATIVE_DISCRIMINATOR_LENGTH;

public final class SerdeUtil {

  private SerdeUtil() {
  }

  public static int writeString(final byte[] utf8, final byte[] data, final int offset) {
    putInt64LE(data, offset, utf8.length);
    System.arraycopy(utf8, 0, data, offset + Long.BYTES, utf8.length);
    return Long.BYTES + utf8.length;
  }

  public static byte[] readString(final byte[] data, final int offset) {
    final int length = (int) ByteUtil.getInt64LE(data, offset);
    final byte[] str = new byte[length];
    System.arraycopy(data, offset + Long.BYTES, str, 0, length);
    return str;
  }

  public static int writeOptionalEpochSeconds(final Instant timestamp, final byte[] data, final int offset) {
    if (timestamp == null) {
      data[offset] = 0;
      return 1;
    } else {
      data[offset] = 1;
      putInt64LE(data, offset + 1, timestamp.getEpochSecond());
      return 1 + Long.BYTES;
    }
  }

  public static byte[] readDiscriminator(final byte[] data, final int offset) {
    final byte[] discriminator = new byte[NATIVE_DISCRIMINATOR_LENGTH];
    System.arraycopy(data, offset, discriminator, 0, NATIVE_DISCRIMINATOR_LENGTH);
    return discriminator;
  }
}
