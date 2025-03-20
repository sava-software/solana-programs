package software.sava.solana.programs.system;

import org.junit.jupiter.api.Test;
import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.encoding.Base58;
import software.sava.core.tx.Transaction;
import software.sava.solana.programs.memo.MemoProgram;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class MemoProgramTest {

  @Test
  void testMemoV2() {
    final var recentBlockHash = "6FE9y44TsLodyVbN243QXATAWhgcj6xVfSLzEEzDbaPS";
    final var feePayer = PublicKey.fromBase58Encoded("savaKKJmmwDsHHhxV6G293hrRM4f1p6jv6qUF441QD3");

    final var memoIx = MemoProgram.createMemo(SolanaAccounts.MAIN_NET, List.of(feePayer), "Sava".getBytes());

    final var transaction = Transaction.createTx(feePayer, memoIx);

    transaction.setRecentBlockHash(recentBlockHash);

    assertEquals(feePayer, transaction.feePayer().publicKey());
    assertArrayEquals(Base58.decode(recentBlockHash), transaction.recentBlockHash());

    final var instructions = transaction.instructions();
    assertEquals(1, instructions.size());
    assertEquals(memoIx, instructions.getFirst());

    final var serialized = transaction.serialized();

    final var expectedTransaction = Base64.getDecoder().decode("""
        AYQi1wFqaZuNJZYZYTeQ5AqEuCsPYhQMfvNrndpMQl1Yl74pzOEGXKizxeNNlWgKiP8AWdYK/V+IwSudfGi1oQ4BAAECDPVl6eB0qtYSlYif4b0tHW4ZfMrzSctd89y3PLhgsgYFSlNamSkhBk0k6HFg2jh8fDW13bySu4HkH6hAQQVEjU3vcJ2gJrwt3TS+VKevTdETKgPzW2DnS1NaKWR0INIpAQEBAARTYXZh
        """.stripTrailing());
    System.arraycopy(expectedTransaction, 1, serialized, 1, Transaction.SIGNATURE_LENGTH);

    assertArrayEquals(expectedTransaction, serialized);
  }
}
