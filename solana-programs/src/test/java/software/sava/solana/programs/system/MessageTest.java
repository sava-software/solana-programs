package software.sava.solana.programs.system;

import org.junit.jupiter.api.Test;
import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.encoding.Base58;
import software.sava.core.tx.Transaction;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MessageTest {

  @Test
  public void serializeMessage() {
    final var fromPublicKey = PublicKey.fromBase58Encoded("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
    final var toPublicKey = PublicKey.fromBase58Encoded("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
    final int lamports = 3_000;

    final var transaction = Transaction.createTx(fromPublicKey, SystemProgram.transfer(
        SolanaAccounts.MAIN_NET.invokedSystemProgram(),
        fromPublicKey,
        toPublicKey,
        lamports)
    );

    final byte[] blockHash = Base58.decode("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn");
    transaction.setRecentBlockHash(blockHash);
    final byte[] expectedMsg = toUnsignedByteArray(new int[]{1, 0, 1, 3, 6, 26, 217, 208, 83, 135, 21, 72, 83, 126, 222, 62, 38, 24, 73, 163,
        223, 183, 253, 2, 250, 188, 117, 178, 35, 200, 228, 106, 219, 133, 61, 12, 235, 122, 188, 208, 216, 117,
        235, 194, 109, 161, 177, 129, 163, 51, 155, 62, 242, 163, 22, 149, 187, 122, 189, 188, 103, 130, 115,
        188, 173, 205, 229, 170, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 203, 226, 136, 193, 153, 148, 240, 50, 230, 98, 9, 79, 221, 179, 243, 174, 90, 67,
        104, 169, 6, 187, 165, 72, 36, 156, 19, 57, 132, 38, 69, 245, 1, 2, 2, 0, 1, 12, 2, 0, 0, 0, 184, 11, 0,
        0, 0, 0, 0, 0});
    final int sigLen = 1 + Transaction.SIGNATURE_LENGTH;
    final byte[] expected = new byte[sigLen + expectedMsg.length];
    System.arraycopy(expectedMsg, 0, expected, sigLen, expectedMsg.length);
    expected[0] = 1;

    assertEquals(Base64.getEncoder().encodeToString(expected), transaction.base64EncodeToString());
  }

  private static byte[] toUnsignedByteArray(final int[] in) {
    final byte[] out = new byte[in.length];
    for (int i = 0; i < in.length; i++) {
      out[i] = (byte) (in[i] & 0xff);
    }
    return out;
  }
}
