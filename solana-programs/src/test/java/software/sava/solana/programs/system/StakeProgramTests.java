package software.sava.solana.programs.system;

import org.junit.jupiter.api.Test;
import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.encoding.Base58;
import software.sava.core.tx.TransactionSkeleton;
import software.sava.solana.programs.stake.StakeProgram;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static software.sava.solana.programs.stake.StakeAuthorize.Staker;
import static software.sava.solana.programs.stake.StakeAuthorize.Withdrawer;

final class StakeProgramTests {

  @Test
  void testParseSplitIx() {
    final var txData = Base64.getDecoder().decode("""
        AfPMO/XQqFumySc8qFm5FzYtO/tZvZwOhh5MszXWmBMPZ9Dtdu8kIUGRjs1SFsv/EK5z1XM9zRVrY+SARus9VQIBAAIFQGApaPSUd2ZTMXfwxK/W6eXjSD7N7MsyiFXUZkYeJzeZHGihq3yZZZuANsM2GDQP6WW4s0evzODV4t9B9k7rbuAZyl2EfWaA7HjlkK2cDUbFtN4Qdkmc3UdLplpvaYIaAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAGodgXkTdUKpg0N73+KnqyVX9TXIp4citopJ3AAAAAAL+0lv5+ZSV0libhfO9SW3Vrk6n1HvV9uLZQVHwPQ8cMAgMCAgBbCQAAAEBgKWj0lHdmUzF38MSv1unl40g+zezLMohV1GZGHic3BwAAAAAAAABzdGFrZTo5yAAAAAAAAAAGodgXkTdUKpg0N73+KnqyVX9TXIp4citopJ3AAAAAAAQDAQIADAMAAAAAvKXf2AMAAA==
        """.stripTrailing());

    var skeleton = TransactionSkeleton.deserializeSkeleton(txData);
    var instructions = skeleton.parseLegacyInstructions();
    assertEquals(2, instructions.length);

    var allocateWithSeedIx = instructions[0];
    var allocateWithSeedData = SystemProgram.AllocateWithSeed.read(allocateWithSeedIx);
    assertArrayEquals(SystemProgram.Instructions.AllocateWithSeed.data(), allocateWithSeedData.discriminator());
    assertEquals(PublicKey.fromBase58Encoded("5LJ93G4SQh9GiewTQJNAu6X9sQ1VVyrpCAgbQsRSgn22"), allocateWithSeedData.baseAccount());
    assertEquals("stake:9", new String(allocateWithSeedData.seed()));
    assertEquals(200, allocateWithSeedData.space());
    assertEquals(PublicKey.fromBase58Encoded("Stake11111111111111111111111111111111111111"), allocateWithSeedData.programOwner());

    var splitIx = instructions[1];
    var splitData = StakeProgram.Split.read(splitIx);
    assertArrayEquals(StakeProgram.Instructions.Split.data(), splitData.discriminator());
    assertEquals(4230000000000L, splitData.lamports());
  }

  @Test
  void authorizeData() {
    byte[] data = Base58.decode("3t9dD1DMBKjQBnMKfD6zcqBgYJE8LDDw42WTR29f8AVNx6xfDrMJJK");
    var authorize = StakeProgram.Authorize.read(data, 0);
    assertArrayEquals(StakeProgram.Instructions.Authorize.data(), authorize.discriminator());
    assertEquals("4zeVNswbjb8x2FnEkGpmuhUQbPLR4MB4ZKj4NNrz5KeC", authorize.newAuthority().toBase58());
    assertEquals(Staker, authorize.stakeAuthorize());

    final var solanaAccounts = SolanaAccounts.MAIN_NET;
    var ix = StakeProgram.authorize(solanaAccounts, List.of(), authorize.newAuthority(), Staker);
    assertArrayEquals(data, ix.data());

    data = Base58.decode("3t9dD1DMBKjQBnMKfD6zcqBgYJE8LDDw42WTR29f8AVNx6xfDsqHaf");
    authorize = StakeProgram.Authorize.read(data, 0);
    assertArrayEquals(StakeProgram.Instructions.Authorize.data(), authorize.discriminator());
    assertEquals("4zeVNswbjb8x2FnEkGpmuhUQbPLR4MB4ZKj4NNrz5KeC", authorize.newAuthority().toBase58());
    assertEquals(Withdrawer, authorize.stakeAuthorize());

    ix = StakeProgram.authorize(solanaAccounts, List.of(), authorize.newAuthority(), Withdrawer);
    assertArrayEquals(data, ix.data());
  }
}
