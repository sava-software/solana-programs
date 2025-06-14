package software.sava.solana.programs.system;

import org.junit.jupiter.api.Test;
import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.Signer;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.encoding.Base58;
import software.sava.core.tx.Transaction;

import static org.junit.jupiter.api.Assertions.*;
import static software.sava.core.accounts.SolanaAccounts.MAIN_NET;

final class TransactionTest {

  private final static Signer signer = Signer.createFromKeyPair(Base58.decode("4Z7cXSyeFR8wNGMVXUE1TwtKn5D5Vu7FzEv69dokLv7KrQk7h6pu4LF8ZRR9yQBhc7uSM6RTTZtU1fmaxiNrxXrs"));

  @Test
  public void signAndSerialize() {
    final var fromPublicKey = PublicKey.fromBase58Encoded("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
    final var toPublicKey = PublicKey.fromBase58Encoded("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
    final int lamports = 3_000;

    final var instruction = SystemProgram.transfer(
        SolanaAccounts.MAIN_NET.invokedSystemProgram(),
        fromPublicKey,
        toPublicKey,
        lamports
    );
    final var program = instruction.programId();
    assertEquals(MAIN_NET.invokedSystemProgram(), program);
    assertFalse(program.feePayer());
    assertFalse(program.signer());
    assertFalse(program.write());
    final var accounts = instruction.accounts();
    assertEquals(2, accounts.size());

    var account = accounts.getFirst();
    assertEquals(fromPublicKey, account.publicKey());
    assertFalse(account.feePayer());
    assertTrue(account.signer());
    assertTrue(account.write());

    account = accounts.getLast();
    assertEquals(toPublicKey, account.publicKey());
    assertFalse(account.feePayer());
    assertFalse(account.signer());
    assertTrue(account.write());

    final var transaction = Transaction.createTx(fromPublicKey, instruction);
    assertTrue(transaction.feePayer().feePayer());
    assertEquals(fromPublicKey, transaction.feePayer().publicKey());

    final var encodedTx = transaction.signAndBase64Encode(Base58.decode("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn"), signer);
    assertEquals(
        "ASdDdWBaKXVRA+6flVFiZokic9gK0+r1JWgwGg/GJAkLSreYrGF4rbTCXNJvyut6K6hupJtm72GztLbWNmRF1Q4BAAEDBhrZ0FOHFUhTft4+JhhJo9+3/QL6vHWyI8jkatuFPQzrerzQ2HXrwm2hsYGjM5s+8qMWlbt6vbxngnO8rc3lqgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAy+KIwZmU8DLmYglP3bPzrlpDaKkGu6VIJJwTOYQmRfUBAgIAAQwCAAAAuAsAAAAAAAA=",
        encodedTx);
  }
}
