package software.sava.solana.programs.stake;

import software.sava.core.accounts.PublicKey;
import software.sava.core.encoding.ByteUtil;
import software.sava.core.rpc.Filter;

import java.util.function.BiFunction;

import static software.sava.core.accounts.PublicKey.PUBLIC_KEY_LENGTH;
import static software.sava.core.accounts.PublicKey.readPubKey;
import static software.sava.core.encoding.ByteUtil.getFloat64LE;
import static software.sava.core.encoding.ByteUtil.getInt64LE;
import static software.sava.core.rpc.Filter.createDataSizeFilter;
import static software.sava.core.rpc.Filter.createMemCompFilter;

public record StakeAccount(PublicKey address,
                           StakeState state,
                           long rentExemptReserve,
                           PublicKey stakeAuthority,
                           PublicKey withdrawAuthority,
                           LockUp lockup,
                           PublicKey voterPublicKey,
                           long stake,
                           long activationEpoch,
                           long deActivationEpoch,
                           double warmupCoolDownRate,
                           long creditsObserved,
                           byte stakeFlags) {

  public enum State {

    ACTIVATING,
    ACTIVE,
    DE_ACTIVATING,
    INACTIVE
  }

  public State state(final long currentEpoch) {
    if (deActivationEpoch < 0) {
      return activationEpoch > 0 && activationEpoch < currentEpoch ? State.ACTIVE : State.ACTIVATING;
    } else {
      return deActivationEpoch < currentEpoch ? State.INACTIVE : State.DE_ACTIVATING;
    }
  }

  public static final int BYTES = 200;
  public static final Filter DATA_SIZE_FILTER = createDataSizeFilter(BYTES);

  public static final int STATE_OFFSET = 0;
  public static final int RENT_EXEMPT_RESERVE_OFFSET = STATE_OFFSET + Integer.BYTES;
  public static final int STAKE_AUTHORITY_OFFSET = RENT_EXEMPT_RESERVE_OFFSET + Long.BYTES;
  public static final int WITHDRAW_AUTHORITY_OFFSET = STAKE_AUTHORITY_OFFSET + PUBLIC_KEY_LENGTH;

  public static final int LOCKUP_OFFSET = WITHDRAW_AUTHORITY_OFFSET + PUBLIC_KEY_LENGTH;
  public static final int LOCKUP_TIMESTAMP_OFFSET = LOCKUP_OFFSET;
  public static final int LOCKUP_EPOCH_OFFSET = LOCKUP_TIMESTAMP_OFFSET + Long.BYTES;
  public static final int LOCKUP_CUSTODIAN_OFFSET = LOCKUP_EPOCH_OFFSET + Long.BYTES;

  public static final int VOTER_PUBLIC_KEY_OFFSET = LOCKUP_OFFSET + LockUp.BYTES;
  public static final int STAKE_OFFSET = VOTER_PUBLIC_KEY_OFFSET + PUBLIC_KEY_LENGTH;
  public static final int ACTIVATION_EPOCH_OFFSET = STAKE_OFFSET + Long.BYTES;
  public static final int DE_ACTIVATION_EPOCH_OFFSET = ACTIVATION_EPOCH_OFFSET + Long.BYTES;
  public static final int WARMUP_COOLDOWN_RATE_OFFSET = DE_ACTIVATION_EPOCH_OFFSET + Long.BYTES;
  public static final int CREDITS_OBSERVED_OFFSET = WARMUP_COOLDOWN_RATE_OFFSET + Double.BYTES;
  public static final int STAKE_FLAGS_OFFSET = CREDITS_OBSERVED_OFFSET + Long.BYTES;

  public static Filter createStateFilter(final StakeState state) {
    final byte[] stateBytes = new byte[Integer.BYTES];
    ByteUtil.putInt32LE(stateBytes, 0, state.ordinal());
    return createMemCompFilter(STATE_OFFSET, stateBytes);
  }

  public static Filter createStakeAuthorityFilter(final PublicKey stakeAuthority) {
    return createMemCompFilter(STAKE_AUTHORITY_OFFSET, stakeAuthority);
  }

  public static Filter createWithdrawAuthorityFilter(final PublicKey stakeAuthority) {
    return createMemCompFilter(WITHDRAW_AUTHORITY_OFFSET, stakeAuthority);
  }

  public static Filter createCustodianFilter(final PublicKey custodian) {
    return createMemCompFilter(LOCKUP_CUSTODIAN_OFFSET, custodian);
  }

  public static Filter createVoterFilter(final PublicKey voterPublicKey) {
    return createMemCompFilter(VOTER_PUBLIC_KEY_OFFSET, voterPublicKey);
  }

  public static final int MUST_FULLY_ACTIVATE_BEFORE_DEACTIVATION_IS_PERMITTED = 0b0000_0001;

  public boolean isSet(final int mask) {
    return (stakeFlags & mask) == mask;
  }

  public static StakeAccount read(final byte[] data, int offset) {
    return read(null, data, offset);
  }

  public static StakeAccount read(final PublicKey address, final byte[] data) {
    return read(address, data, 0);
  }

  public static final BiFunction<PublicKey, byte[], StakeAccount> FACTORY = StakeAccount::read;

  public static StakeAccount read(final PublicKey address, final byte[] data, int offset) {
    final var stakeState = StakeState.values()[ByteUtil.getInt32LE(data, offset)];
    offset += Integer.BYTES;
    final long rentExemptReserve = getInt64LE(data, offset);
    offset += Long.BYTES;
    final var stakeAuthority = readPubKey(data, offset);
    offset += PUBLIC_KEY_LENGTH;
    final var withdrawAuthority = readPubKey(data, offset);
    offset += PUBLIC_KEY_LENGTH;
    final var lockup = LockUp.read(data, offset);
    offset += LockUp.BYTES;
    final var voterPublicKey = readPubKey(data, offset);
    offset += PUBLIC_KEY_LENGTH;
    final long stake = getInt64LE(data, offset);
    offset += Long.BYTES;
    final long activationEpoch = getInt64LE(data, offset);
    offset += Long.BYTES;
    final long deActivationEpoch = getInt64LE(data, offset);
    offset += Long.BYTES;
    final double warmupCooldownRate = getFloat64LE(data, offset);
    offset += Double.BYTES;
    final long creditsObserved = getInt64LE(data, offset);
    offset += Long.BYTES;
    // https://github.com/solana-labs/solana/blob/master/sdk/program/src/stake/stake_flags.rs#L69
    final byte stakeFlags = data[offset];
    return new StakeAccount(
        address,
        stakeState,
        rentExemptReserve,
        stakeAuthority,
        withdrawAuthority,
        lockup,
        voterPublicKey,
        stake,
        activationEpoch,
        deActivationEpoch,
        warmupCooldownRate,
        creditsObserved,
        stakeFlags
    );
  }
}
