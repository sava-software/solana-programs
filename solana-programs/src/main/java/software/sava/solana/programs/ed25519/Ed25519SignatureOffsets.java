package software.sava.solana.programs.ed25519;

import software.sava.core.encoding.ByteUtil;

public record Ed25519SignatureOffsets(int signatureOffset,
                                      int signatureInstructionIndex,
                                      int publicKeyOffset,
                                      int publicKeyInstructionIndex,
                                      int messageDataOffset,
                                      int messageDataSize,
                                      int messageInstructionIndex) {

  public static final int BYTES = 14;

  public static Ed25519SignatureOffsets read(final byte[] data, final int offset) {
    if (data == null || data.length == 0) {
      return null;
    }
    int i = offset;
    final int signatureOffset = ByteUtil.getInt16LE(data, i);
    i += 2;
    final int signatureInstructionIndex = ByteUtil.getInt16LE(data, i);
    i += 2;
    final int publicKeyOffset = ByteUtil.getInt16LE(data, i);
    i += 2;
    final int publicKeyInstructionIndex = ByteUtil.getInt16LE(data, i);
    i += 2;
    final int messageDataOffset = ByteUtil.getInt16LE(data, i);
    i += 2;
    final int messageDataSize = ByteUtil.getInt16LE(data, i);
    i += 2;
    final int messageInstructionIndex = ByteUtil.getInt16LE(data, i);
    return new Ed25519SignatureOffsets(
        signatureOffset,
        signatureInstructionIndex,
        publicKeyOffset,
        publicKeyInstructionIndex,
        messageDataOffset,
        messageDataSize,
        messageInstructionIndex
    );
  }


  public static Ed25519SignatureOffsets[] readVector(final byte[] data, final int offset) {
    if (data == null || data.length == 0) {
      return null;
    }
    final int numSignatures = ByteUtil.getInt16LE(data, offset);
    int i = offset + 2;
    final var signatures = new Ed25519SignatureOffsets[numSignatures];
    for (int s = 0; s < numSignatures; s++) {
      signatures[s] = read(data, i);
      i += BYTES;
    }
    return signatures;
  }
}
