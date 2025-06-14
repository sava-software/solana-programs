package software.sava.solana.programs.vote;

import software.sava.core.accounts.PublicKey;
import software.sava.core.serial.Serializable;
import software.sava.solana.programs.serde.SerdeUtil;

import static software.sava.core.accounts.PublicKey.PUBLIC_KEY_LENGTH;
import static software.sava.core.accounts.PublicKey.readPubKey;

public record VoteAuthorizeWithSeedArgs(VoteAuthorize authorizationType,
                                        PublicKey currentAuthorityDerivedKeyOwner,
                                        byte[] currentAuthorityDerivedKeySeed,
                                        PublicKey newAuthority) implements Serializable {

  public static VoteAuthorizeWithSeedArgs read(final byte[] data, final int offset) {
    if (data == null || data.length == 0) {
      return null;
    }
    int i = offset;
    final var authorizationType = VoteAuthorize.read(data, i);
    i += authorizationType.l();
    final var currentAuthorityDerivedKeyOwner = readPubKey(data, i);
    i += PUBLIC_KEY_LENGTH;
    final var currentAuthorityDerivedKeySeed = SerdeUtil.readString(data, i);
    i += Long.BYTES + currentAuthorityDerivedKeySeed.length;
    final var newAuthority = readPubKey(data, i);
    return new VoteAuthorizeWithSeedArgs(authorizationType, currentAuthorityDerivedKeyOwner, currentAuthorityDerivedKeySeed, newAuthority);
  }

  @Override
  public int write(final byte[] data, final int offset) {
    int i = authorizationType.write(data, offset);
    i += currentAuthorityDerivedKeyOwner.write(data, i);
    i += SerdeUtil.writeString(currentAuthorityDerivedKeySeed, data, i);
    i += newAuthority.write(data, i);
    return i - offset;
  }

  @Override
  public int l() {
    return authorizationType.l() + PUBLIC_KEY_LENGTH + Long.BYTES + currentAuthorityDerivedKeySeed.length + PUBLIC_KEY_LENGTH;
  }
}
