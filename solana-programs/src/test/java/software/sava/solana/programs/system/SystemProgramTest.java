package software.sava.solana.programs.system;

import org.junit.jupiter.api.Test;
import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.encoding.Base58;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static software.sava.core.accounts.SolanaAccounts.MAIN_NET;

final class SystemProgramTest {

  @Test
  public void transferInstruction() {
    final var fromPublicKey = PublicKey.fromBase58Encoded("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
    final var toPublicKey = PublicKey.fromBase58Encoded("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
    final int lamports = 3000;

    final var instruction = SystemProgram.transfer(
        SolanaAccounts.MAIN_NET.invokedSystemProgram(),
        fromPublicKey,
        toPublicKey,
        lamports
    );

    assertEquals(MAIN_NET.invokedSystemProgram(), instruction.programId());
    assertEquals(2, instruction.accounts().size());
    assertEquals(fromPublicKey, instruction.accounts().getFirst().publicKey());
    assertEquals(toPublicKey, instruction.accounts().getLast().publicKey());

    assertArrayEquals(new byte[]{2, 0, 0, 0, -72, 11, 0, 0, 0, 0, 0, 0}, instruction.data());
  }

  @Test
  public void createAccountInstruction() {
    final var instruction = SystemProgram.createAccount(
        MAIN_NET.invokedSystemProgram(),
        MAIN_NET.systemProgram(),
        MAIN_NET.systemProgram(), 2039280, 165,
        MAIN_NET.systemProgram()
    );

    assertEquals("11119os1e9qSs2u7TsThXqkBSRUo9x7kpbdqtNNbTeaxHGPdWbvoHsks9hpp6mb2ed1NeB",
        Base58.encode(instruction.data())
    );
  }


  @Test
  public void parseNonceAccount() {
    final var base64Data = "AQAAAAEAAAAM9WXp4HSq1hKViJ/hvS0dbhl8yvNJy13z3Lc8uGCyBirl7d+e05ILHtmpCyrZqMRG/x5AzISLYbViohfeG07tiBMAAAAAAAA=";
    final byte[] data = Base64.getDecoder().decode(base64Data);
    final var nonceAccount = NonceAccount.read(data, 0);

    assertEquals(1, nonceAccount.version());
    assertEquals(NonceAccount.State.Initialized, nonceAccount.state());
    assertEquals(PublicKey.fromBase58Encoded("savaKKJmmwDsHHhxV6G293hrRM4f1p6jv6qUF441QD3"), nonceAccount.authority());

    final byte[] blockHash = Base58.decode("3tTUV2sKPJ6zkS77Yo5D4vZnaqy3BX4WaTtmJsMwC2rQ");
    assertArrayEquals(blockHash, nonceAccount.nonce());

    assertEquals(5_000, nonceAccount.lamportsPerSignature());
  }
}
