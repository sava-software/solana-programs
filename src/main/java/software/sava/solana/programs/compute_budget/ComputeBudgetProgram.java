package software.sava.solana.programs.compute_budget;

import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.encoding.ByteUtil;
import software.sava.core.programs.Discriminator;
import software.sava.core.tx.Instruction;

import static software.sava.core.accounts.meta.AccountMeta.NO_KEYS;
import static software.sava.core.tx.Instruction.createInstruction;

public final class ComputeBudgetProgram {

  public static int CU_LIMIT_CONSUMPTION = 150;
  public static int CU_PRICE_CONSUMPTION = 150;
  // CU's consumed for typical inclusion of set CU limit and price.
  public static int COMPUTE_UNITS_CONSUMED = CU_LIMIT_CONSUMPTION + CU_PRICE_CONSUMPTION;
  public static int SET_LOADED_ACCOUNT_SIZE_LIMIT_CONSUMPTION = 150;

  public static int MAX_COMPUTE_BUDGET = 1_400_000;

  public enum Instructions implements Discriminator {

    Unused, // deprecated variant, reserved value.
    /// Request a specific transaction-wide program heap region size in bytes.
    /// The value requested must be a multiple of 1024. This new heap region
    /// size applies to each program executed in the transaction, including all
    /// calls to CPIs.
    RequestHeapFrame, //(u32),
    /// Set a specific compute unit limit that the transaction is allowed to consume.
    SetComputeUnitLimit, //(u32),
    /// Set a compute unit price in "micro-lamports" to pay a higher transaction
    /// fee for higher transaction prioritization.
    SetComputeUnitPrice, //(u64),
    /// Set a specific transaction-wide account data size limit, in bytes, is allowed to load.
    SetLoadedAccountsDataSizeLimit; //(u32),

    private final byte discriminator;
    private final byte[] discriminatorBytes;

    Instructions() {
      this.discriminator = (byte) this.ordinal();
      this.discriminatorBytes = new byte[]{this.discriminator};
    }

    @Override
    public byte[] data() {
      return discriminatorBytes;
    }

    @Override
    public int write(final byte[] bytes, final int i) {
      bytes[i] = (byte) this.ordinal();
      return 1;
    }

    @Override
    public int length() {
      return 1;
    }
  }

  public static Instruction requestHeapFrame(final AccountMeta invokedProgram, final int heapRegionSize) {
    final byte[] data = new byte[1 + Integer.BYTES];
    data[0] = Instructions.RequestHeapFrame.discriminator;
    ByteUtil.putInt32LE(data, 1, heapRegionSize);
    return createInstruction(invokedProgram, NO_KEYS, data);
  }

  public static Instruction setComputeUnitLimit(final AccountMeta invokedProgram, final int units) {
    final byte[] data = new byte[1 + Integer.BYTES];
    data[0] = Instructions.SetComputeUnitLimit.discriminator;
    ByteUtil.putInt32LE(data, 1, units);
    return createInstruction(invokedProgram, NO_KEYS, data);
  }

  public static Instruction setComputeUnitPrice(final AccountMeta invokedProgram, final long microLamports) {
    final byte[] data = new byte[1 + Long.BYTES];
    data[0] = Instructions.SetComputeUnitPrice.discriminator;
    ByteUtil.putInt64LE(data, 1, microLamports);
    return createInstruction(invokedProgram, NO_KEYS, data);
  }

  public static Instruction setLoadedAccountsDataSizeLimit(final AccountMeta invokedProgram, final int limit) {
    final byte[] data = new byte[1 + Integer.BYTES];
    data[0] = Instructions.SetLoadedAccountsDataSizeLimit.discriminator;
    ByteUtil.putInt32LE(data, 1, limit);
    return createInstruction(invokedProgram, NO_KEYS, data);
  }

  private ComputeBudgetProgram() {
  }
}
