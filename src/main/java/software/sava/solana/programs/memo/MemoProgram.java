package software.sava.solana.programs.memo;

import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.tx.Instruction;

import java.util.List;

public final class MemoProgram {

  public static Instruction createMemoFromAccounts(final AccountMeta invokedMemoProgram,
                                                   final List<AccountMeta> signers,
                                                   final byte[] memo) {
    return Instruction.createInstruction(invokedMemoProgram, signers, memo);
  }

  public static Instruction createMemoFromAccounts(final SolanaAccounts solanaAccounts,
                                                   final List<AccountMeta> signers,
                                                   final byte[] memo) {
    return createMemoFromAccounts(solanaAccounts.invokedMemoProgramV2(), signers, memo);
  }

  public static Instruction createMemo(final AccountMeta invokedMemoProgram,
                                       final List<PublicKey> signers,
                                       final byte[] memo) {
    return createMemoFromAccounts(
        invokedMemoProgram,
        signers.stream().map(AccountMeta::createReadOnlySigner).toList(),
        memo
    );
  }

  public static Instruction createMemo(final SolanaAccounts solanaAccounts,
                                       final List<PublicKey> signers,
                                       final byte[] memo) {
    return createMemo(solanaAccounts.invokedMemoProgramV2(), signers, memo);
  }

  private MemoProgram() {
  }
}
