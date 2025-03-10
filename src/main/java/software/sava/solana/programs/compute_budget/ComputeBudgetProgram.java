package software.sava.solana.programs.compute_budget;

import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.encoding.ByteUtil;
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

  public static Instruction requestHeapFrame(final AccountMeta invokedProgram, final int heapRegionSize) {
    final byte[] data = new byte[5];
    data[0] = (byte) 1;
    ByteUtil.putInt32LE(data, 1, heapRegionSize);
    return createInstruction(invokedProgram, NO_KEYS, data);
  }

  public static Instruction setComputeUnitLimit(final AccountMeta invokedProgram, final int units) {
    final byte[] data = new byte[5];
    data[0] = (byte) 2;
    ByteUtil.putInt32LE(data, 1, units);
    return createInstruction(invokedProgram, NO_KEYS, data);
  }

  public static Instruction setComputeUnitPrice(final AccountMeta invokedProgram, final long microLamports) {
    final byte[] data = new byte[9];
    data[0] = (byte) 3;
    ByteUtil.putInt64LE(data, 1, microLamports);
    return createInstruction(invokedProgram, NO_KEYS, data);
  }

  public static Instruction setLoadedAccountsDataSizeLimit(final AccountMeta invokedProgram, final int limit) {
    final byte[] data = new byte[5];
    data[0] = (byte) 4;
    ByteUtil.putInt32LE(data, 1, limit);
    return createInstruction(invokedProgram, NO_KEYS, data);
  }

  private ComputeBudgetProgram() {
  }
}
