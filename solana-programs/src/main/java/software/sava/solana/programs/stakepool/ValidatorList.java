package software.sava.solana.programs.stakepool;

import software.sava.core.accounts.PublicKey;
import software.sava.core.borsh.Borsh;
import software.sava.core.encoding.ByteUtil;
import software.sava.solana.programs.stake.ValidatorStakeInfo;

import java.util.function.BiFunction;

import static software.sava.core.encoding.ByteUtil.getInt32LE;

public record ValidatorList(PublicKey address,
                            AccountType accountType,
                            int maxValidators,
                            ValidatorStakeInfo[] validators) implements Borsh {

  public static ValidatorList read(final byte[] data, final int offset) {
    return read(null, data, offset);
  }

  public static ValidatorList read(final PublicKey publicKey, final byte[] data) {
    return read(publicKey, data, 0);
  }

  public static final BiFunction<PublicKey, byte[], ValidatorList> FACTORY = ValidatorList::read;

  public static ValidatorList read(final PublicKey publicKey, final byte[] data, int offset) {
    final var accountType = AccountType.values()[data[offset] & 0xFF];
    ++offset;
    final int maxValidators = getInt32LE(data, offset);
    offset += Integer.BYTES;
    final int numValidators = ByteUtil.getInt32LE(data, offset);
    offset += Integer.BYTES;
    final var validators = new ValidatorStakeInfo[numValidators];
    for (int i = 0; i < numValidators; ++i) {
      validators[i] = ValidatorStakeInfo.read(data, offset);
      offset += ValidatorStakeInfo.BYTES;
    }
    return new ValidatorList(
        publicKey,
        accountType,
        maxValidators,
        validators
    );
  }

  @Override
  public int write(final byte[] data, final int offset) {
    data[offset] = (byte) accountType.ordinal();
    ByteUtil.putInt32LE(data, offset + 1, maxValidators);
    return Borsh.writeArray(validators, data, offset + 1 + Integer.BYTES);
  }

  @Override
  public int l() {
    return 1 + Integer.BYTES + (ValidatorStakeInfo.BYTES * validators.length);
  }
}
