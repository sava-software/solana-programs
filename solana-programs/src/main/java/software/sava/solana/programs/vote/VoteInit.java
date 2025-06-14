
package software.sava.solana.programs.vote;

import software.sava.core.accounts.PublicKey;
import software.sava.core.serial.Serializable;

import static software.sava.core.accounts.PublicKey.PUBLIC_KEY_LENGTH;
import static software.sava.core.accounts.PublicKey.readPubKey;

public record VoteInit(PublicKey nodePubkey,
                       PublicKey authorizedVoter,
                       PublicKey authorizedWithdrawer,
                       int commission) implements Serializable {

  public static final int BYTES = PUBLIC_KEY_LENGTH + PUBLIC_KEY_LENGTH + PUBLIC_KEY_LENGTH + 1;

  public static VoteInit read(final byte[] data, final int offset) {
    if (data == null || data.length == 0) {
      return null;
    }
    int i = offset;
    final var nodePubkey = readPubKey(data, i);
    i += PUBLIC_KEY_LENGTH;
    final var authorizedVoter = readPubKey(data, i);
    i += PUBLIC_KEY_LENGTH;
    final var authorizedWithdrawer = readPubKey(data, i);
    i += PUBLIC_KEY_LENGTH;
    final int commission = data[i] & 0xFF;
    return new VoteInit(nodePubkey, authorizedVoter, authorizedWithdrawer, commission);
  }

  @Override
  public int write(final byte[] data, final int offset) {
    int i = offset;
    i += nodePubkey.write(data, i);
    i += authorizedVoter.write(data, i);
    i += authorizedWithdrawer.write(data, i);
    data[i] = (byte) commission;
    return i - offset;
  }

  @Override
  public int l() {
    return BYTES;
  }
}
