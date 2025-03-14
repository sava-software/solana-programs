package software.sava.solana.programs.stakepool;

import software.sava.core.accounts.PublicKey;
import software.sava.core.borsh.Borsh;
import software.sava.core.encoding.ByteUtil;
import software.sava.solana.programs.stake.LockUp;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.function.BiFunction;

import static java.math.BigDecimal.ZERO;
import static software.sava.core.accounts.PublicKey.PUBLIC_KEY_LENGTH;
import static software.sava.core.accounts.PublicKey.readPubKey;

// https://github.com/solana-labs/solana-program-library/blob/master/stake-pool/program/src/state.rs#L45
public record StakePoolState(PublicKey address,
                             AccountType accountType,
                             PublicKey manager,
                             PublicKey staker,
                             PublicKey stakeDepositAuthority,
                             int stakeWithdrawBumpSeed,
                             PublicKey validatorList,
                             PublicKey reserveStake,
                             PublicKey poolMint,
                             PublicKey managerFeeAccount,
                             PublicKey tokenProgramId,
                             BigDecimal totalLamports,
                             BigDecimal poolTokenSupply,
                             long lastUpdateEpoch,
                             LockUp lockUp,
                             Fee epochFee,
                             FutureEpochFee nextEpochFee,
                             PublicKey preferredDepositValidatorVoteAddress,
                             PublicKey preferredWithdrawValidatorVoteAddress,
                             Fee stakeDepositFee,
                             Fee stakeWithdrawalFee,
                             FutureEpochFee nextStakeWithdrawalFee,
                             int stakeReferralFee,
                             PublicKey solDepositAuthority,
                             Fee solDepositFee,
                             int solReferralFee,
                             PublicKey solWithdrawAuthority,
                             Fee solWithdrawalFee,
                             FutureEpochFee nextSolWithdrawalFee,
                             long lastEpochPoolTokenSupply,
                             long lastEpochTotalLamports) {

  public static final int MANAGER_OFFSET = 1;
  public static final int STAKE_OFFSET = MANAGER_OFFSET + PUBLIC_KEY_LENGTH;
  public static final int STAKE_DEPOSIT_AUTHORITY_OFFSET = STAKE_OFFSET + PUBLIC_KEY_LENGTH;
  public static final int STAKE_WITHDRAWAL_BUMP_SEED_OFFSET = STAKE_DEPOSIT_AUTHORITY_OFFSET + PUBLIC_KEY_LENGTH;
  public static final int VALIDATOR_LIST_OFFSET = STAKE_WITHDRAWAL_BUMP_SEED_OFFSET + 1;
  public static final int RESERVE_STAKE_OFFSET = VALIDATOR_LIST_OFFSET + PUBLIC_KEY_LENGTH;
  public static final int POOL_MINT_OFFSET = RESERVE_STAKE_OFFSET + PUBLIC_KEY_LENGTH;
  public static final int MANAGER_FEE_OFFSET = POOL_MINT_OFFSET + PUBLIC_KEY_LENGTH;
  public static final int TOKEN_PROGRAM_ID_OFFSET = MANAGER_FEE_OFFSET + PUBLIC_KEY_LENGTH;
  public static final int TOTAL_LAMPORTS_OFFSET = TOKEN_PROGRAM_ID_OFFSET + PUBLIC_KEY_LENGTH;
  public static final int POOL_TOKEN_SUPPLY_OFFSET = TOTAL_LAMPORTS_OFFSET + Long.BYTES;
  public static final int LAST_UPDATE_EPOCH_OFFSET = POOL_TOKEN_SUPPLY_OFFSET + Long.BYTES;
  public static final int LOCKUP_OFFSET = LAST_UPDATE_EPOCH_OFFSET + Long.BYTES;
  public static final int EPOCH_FEE_OFFSET = LOCKUP_OFFSET + LockUp.BYTES;
  public static final int NEXT_EPOCH_FEE_OFFSET = EPOCH_FEE_OFFSET + Fee.BYTES;

  public BigDecimal calculateSolPrice(final MathContext mathContext) {
    return totalLamports.signum() == 0 || poolTokenSupply.signum() == 0
        ? ZERO
        : totalLamports.divide(poolTokenSupply, mathContext).stripTrailingZeros();
  }

  public BigDecimal calculateSolPrice(final int scale, final RoundingMode roundingMode) {
    return totalLamports.signum() == 0 || poolTokenSupply.signum() == 0
        ? ZERO
        : totalLamports.divide(poolTokenSupply, scale, roundingMode).stripTrailingZeros();
  }

  public static final BiFunction<PublicKey, byte[], StakePoolState> FACTORY = StakePoolState::parseProgramData;

  public static StakePoolState parseProgramData(final byte[] data) {
    return parseProgramData(null, data);
  }

  public static StakePoolState parseProgramData(final PublicKey address, final byte[] data) {
    final var accountType = AccountType.values()[data[0]];
    int offset = 1;
    final var manager = readPubKey(data, offset);
    offset += PUBLIC_KEY_LENGTH;
    final var staker = readPubKey(data, offset);
    offset += PUBLIC_KEY_LENGTH;
    final var stakeDepositAuthority = readPubKey(data, offset);
    offset += PUBLIC_KEY_LENGTH;
    final int stakeWithdrawBumpSeed = data[offset] & 0xFF;
    ++offset;
    final var validatorList = readPubKey(data, offset);
    offset += PUBLIC_KEY_LENGTH;
    final var reserveStake = readPubKey(data, offset);
    offset += PUBLIC_KEY_LENGTH;
    final var poolMint = readPubKey(data, offset);
    offset += PUBLIC_KEY_LENGTH;
    final var managerFeeAccount = readPubKey(data, offset);
    offset += PUBLIC_KEY_LENGTH;
    final var tokenProgramId = readPubKey(data, offset);
    offset += PUBLIC_KEY_LENGTH;
    final long totalLamports = ByteUtil.getInt64LE(data, offset);
    offset += Long.BYTES;
    final long poolTokenSupply = ByteUtil.getInt64LE(data, offset);
    offset += Long.BYTES;
    final long lastUpdateEpoch = ByteUtil.getInt64LE(data, offset);
    offset += Long.BYTES;
    final var lockUp = LockUp.read(data, offset);
    offset += LockUp.BYTES;
    final var epochFee = Fee.parseFee(data, offset);
    offset += Fee.BYTES;
    final var nextEpochFee = FutureEpochFee.parseFutureFee(data, offset);
    if (nextEpochFee == FutureEpochFee.NONE) {
      ++offset;
    } else {
      offset += FutureEpochFee.BYTES;
    }

    final PublicKey preferredDepositValidatorVoteAddress;
    if (data[offset] == 0) {
      ++offset;
      preferredDepositValidatorVoteAddress = null;
    } else {
      preferredDepositValidatorVoteAddress = readPubKey(data, ++offset);
      offset += PUBLIC_KEY_LENGTH;
    }

    final PublicKey preferredWithdrawValidatorVoteAddress;
    if (data[offset] == 0) {
      ++offset;
      preferredWithdrawValidatorVoteAddress = null;
    } else {
      preferredWithdrawValidatorVoteAddress = readPubKey(data, ++offset);
      offset += PUBLIC_KEY_LENGTH;
    }

    final var stakeDepositFee = Fee.parseFee(data, offset);
    offset += Fee.BYTES;
    final var stakeWithdrawalFee = Fee.parseFee(data, offset);
    offset += Fee.BYTES;

    final var nextStakeWithdrawalFee = FutureEpochFee.parseFutureFee(data, offset);
    if (nextStakeWithdrawalFee == FutureEpochFee.NONE) {
      ++offset;
    } else {
      offset += FutureEpochFee.BYTES;
    }

    final int stakeReferralFee = data[offset];
    ++offset;

    final PublicKey solDepositAuthority;
    if (data[offset] == 0) {
      ++offset;
      solDepositAuthority = null;
    } else {
      solDepositAuthority = readPubKey(data, ++offset);
      offset += PUBLIC_KEY_LENGTH;
    }

    final var solDepositFee = Fee.parseFee(data, offset);
    offset += Fee.BYTES;
    final int solReferralFee = data[offset];
    ++offset;

    final PublicKey solWithdrawAuthority;
    if (data[offset] == 0) {
      ++offset;
      solWithdrawAuthority = null;
    } else {
      solWithdrawAuthority = readPubKey(data, ++offset);
      offset += PUBLIC_KEY_LENGTH;
    }

    final var solWithdrawalFee = Fee.parseFee(data, offset);
    offset += Fee.BYTES;

    final var nextSolWithdrawalFee = FutureEpochFee.parseFutureFee(data, offset);
    if (nextSolWithdrawalFee == FutureEpochFee.NONE) {
      ++offset;
    } else {
      offset += FutureEpochFee.BYTES;
    }

    final long lastEpochPoolTokenSupply = ByteUtil.getInt64LE(data, offset);
    offset += Long.BYTES;
    final long lastEpochTotalLamports = ByteUtil.getInt64LE(data, offset);

    final var bigLamports = new BigDecimal(Long.toUnsignedString(totalLamports));
    final var bigSupply = new BigDecimal(Long.toUnsignedString(poolTokenSupply));

    return new StakePoolState(
        address,
        accountType,
        manager, staker, stakeDepositAuthority,
        stakeWithdrawBumpSeed,
        validatorList,
        reserveStake,
        poolMint,
        managerFeeAccount,
        tokenProgramId,
        bigLamports, bigSupply,
        lastUpdateEpoch,
        lockUp,
        epochFee, nextEpochFee,
        preferredDepositValidatorVoteAddress, preferredWithdrawValidatorVoteAddress,
        stakeDepositFee, stakeWithdrawalFee, nextStakeWithdrawalFee, stakeReferralFee,
        solDepositAuthority, solDepositFee, solReferralFee, solWithdrawAuthority, solWithdrawalFee, nextSolWithdrawalFee,
        lastEpochPoolTokenSupply, lastEpochTotalLamports
    );
  }

  public record Fee(long denominator, long numerator) implements Comparable<Fee>, Borsh {

    static final int BYTES = 16;

    static Fee parseFee(final byte[] data, final int offset) {
      return new Fee(ByteUtil.getInt64LE(data, offset), ByteUtil.getInt64LE(data, offset + 8));
    }

    public BigDecimal toRatio(final MathContext mathContext) {
      return numerator == 0 || denominator == 0 ? ZERO
          : BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), mathContext).stripTrailingZeros();
    }

    public BigDecimal toRatio(final int scale, final RoundingMode roundingMode) {
      return numerator == 0 || denominator == 0 ? ZERO
          : BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), scale, roundingMode).stripTrailingZeros();
    }

    public double toRatio() {
      return numerator == 0 || denominator == 0 ? 0 : numerator / (double) denominator;
    }

    @Override
    public int compareTo(final Fee o) {
      if (numerator == 0 || o.numerator == 0) {
        return Long.compare(numerator, o.numerator);
      } else {
        return Double.compare(toRatio(), o.toRatio());
      }
    }

    @Override
    public int l() {
      return BYTES;
    }

    @Override
    public int write(final byte[] data, final int offset) {
      ByteUtil.putInt64LE(data, offset, denominator);
      ByteUtil.putInt64LE(data, offset + Long.BYTES, numerator);
      return BYTES;
    }
  }

  public enum FutureEpoch {
    NONE,
    ONE,
    TWO
  }

  public record FutureEpochFee(FutureEpoch futureEpoch, Fee fee) {

    private static final FutureEpochFee NONE = new FutureEpochFee(FutureEpoch.NONE, null);
    static final int BYTES = 1 + Fee.BYTES;

    static FutureEpochFee parseFutureFee(final byte[] data, final int offset) {
      final int futureEpochOption = data[offset];
      if (futureEpochOption == 0) {
        return NONE;
      } else if (futureEpochOption == 1) {
        return new FutureEpochFee(FutureEpoch.ONE, Fee.parseFee(data, offset + 1));
      } else if (futureEpochOption == 2) {
        return new FutureEpochFee(FutureEpoch.TWO, Fee.parseFee(data, offset + 1));
      } else {
        throw new IllegalStateException("Unknown FutureEpoch value: " + futureEpochOption);
      }
    }
  }
}
