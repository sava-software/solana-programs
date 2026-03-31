package software.sava.solana.programs.system;

import org.junit.jupiter.api.Test;
import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.accounts.meta.AccountMeta;
import software.sava.solana.programs.token.Token2022Program;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class Token2022ProgramTests {

  @Test
  void createMintWithTransferHook() {
    // devnet 3b7rYDCdxymqBXR3FLFgUtUoQyoYGg2ZF2bbKviuSQToSWyZeJmfb2wpjpTsZRf8FCWVMtuNetTAz2EAvmRSZLUi

    final var mintAccount = PublicKey.fromBase58Encoded("88WLQK58mbqNjaUBxYjEvhvdsWGQde4s1EqyagvEng2f");
    final var mintAuthority = PublicKey.fromBase58Encoded("CvUqgjP892h66aYPC9E8gKTXnTebY8qaU5ehGrgEQSwV");
    final var programAccount = PublicKey.fromBase58Encoded("2o6gvxp17hkML8Rz3cvqzbSTFStES287fYeDPeHhF7Vj");

    final byte[] expectedData = Base64.getDecoder().decode(
        "JACxI9EyhTENtAmHTkc1jeGOXz5ac2rbQKBtpzoJOzX1ZBqq1gqqi0gmr088z6opaqke8W8zo8Lcb1bxDLWCwhBK"
    );

    final var solAccounts = SolanaAccounts.MAIN_NET;
    final var initializeTransferHookIx = Token2022Program.initializeTransferHook(
        solAccounts,
        mintAccount,
        mintAuthority,
        programAccount
    );

    assertEquals(solAccounts.invokedToken2022Program(), initializeTransferHookIx.programId());

    final var accounts = initializeTransferHookIx.accounts();
    assertEquals(1, accounts.size());
    assertEquals(AccountMeta.createWrite(mintAccount), accounts.getFirst());

    assertArrayEquals(expectedData, initializeTransferHookIx.data());
  }

  @Test
  void createMintWithMetadataPointer() {
    // devnet 3b7rYDCdxymqBXR3FLFgUtUoQyoYGg2ZF2bbKviuSQToSWyZeJmfb2wpjpTsZRf8FCWVMtuNetTAz2EAvmRSZLUi

    final var mint = PublicKey.fromBase58Encoded("88WLQK58mbqNjaUBxYjEvhvdsWGQde4s1EqyagvEng2f");
    final var authority = PublicKey.fromBase58Encoded("CvUqgjP892h66aYPC9E8gKTXnTebY8qaU5ehGrgEQSwV");

    final byte[] expectedData = Base64.getDecoder().decode(
        "JwCxI9EyhTENtAmHTkc1jeGOXz5ac2rbQKBtpzoJOzX1ZGnuCvRYCzpsDUH5g837ywyG3bHaM3+sD1Ohmf1+QlgU"
    );

    final var solAccounts = SolanaAccounts.MAIN_NET;
    final var initMetadataPointerIx = Token2022Program.initializeMetadataPointer(
        solAccounts,
        mint,
        authority,
        mint
    );

    assertEquals(solAccounts.invokedToken2022Program(), initMetadataPointerIx.programId());

    final var accounts = initMetadataPointerIx.accounts();
    assertEquals(1, accounts.size());
    assertEquals(AccountMeta.createWrite(mint), accounts.getFirst());

    assertArrayEquals(expectedData, initMetadataPointerIx.data());
  }

  @Test
  void createMintWithInitializingMetadata() {
    // devnet 3b7rYDCdxymqBXR3FLFgUtUoQyoYGg2ZF2bbKviuSQToSWyZeJmfb2wpjpTsZRf8FCWVMtuNetTAz2EAvmRSZLUi

    final var name = "SimpleTestCoin";
    final var symbol = "STC";
    final var uri = "https://example.com/metadata.json";
    final var mintAccount = PublicKey.fromBase58Encoded("88WLQK58mbqNjaUBxYjEvhvdsWGQde4s1EqyagvEng2f");
    final var metadataAccount = PublicKey.fromBase58Encoded("88WLQK58mbqNjaUBxYjEvhvdsWGQde4s1EqyagvEng2f");
    final var authority = PublicKey.fromBase58Encoded("CvUqgjP892h66aYPC9E8gKTXnTebY8qaU5ehGrgEQSwV");
    final var updateAuthority = PublicKey.fromBase58Encoded("CvUqgjP892h66aYPC9E8gKTXnTebY8qaU5ehGrgEQSwV");

    final byte[] expectedData = Base64.getDecoder().decode(
        "0uEeoli4TY0OAAAAU2ltcGxlVGVzdENvaW4DAAAAU1RDIQAAAGh0dHBzOi8vZXhhbXBsZS5jb20vbWV0YWRhdGEuanNvbg=="
    );

    final var solAccounts = SolanaAccounts.MAIN_NET;
    final var initializeTokenMetadataIx = Token2022Program.initializeTokenMetadataInstruction(
        solAccounts,
        metadataAccount,
        authority,
        updateAuthority,
        mintAccount,
        name,
        symbol,
        uri
    );

    assertEquals(solAccounts.invokedToken2022Program(), initializeTokenMetadataIx.programId());

    final var accounts = initializeTokenMetadataIx.accounts();
    assertEquals(4, accounts.size());
    assertEquals(AccountMeta.createWrite(metadataAccount), accounts.getFirst());
    assertEquals(AccountMeta.createRead(updateAuthority), accounts.get(1));
    assertEquals(AccountMeta.createRead(mintAccount), accounts.get(2));
    assertEquals(AccountMeta.createReadOnlySigner(authority), accounts.getLast());

    assertArrayEquals(expectedData, initializeTokenMetadataIx.data());
  }

  @Test
  void updateTransferHookAccount() {
    // devnet THZ3HTPAQZaEj6ggHSaLSxSS5CeGYp88VDa6NyXxY7pV9khHk1xJk1yHqP4jWByHjBUz34UuWLPffWQfeCzjNyi

    final var mintAccount = PublicKey.fromBase58Encoded("HCRDkSQ6vM9QxDkJMGNUmVKjWqPYudkEsZRDwoJvyzQE");
    final var mintAuthority = PublicKey.fromBase58Encoded("CvUqgjP892h66aYPC9E8gKTXnTebY8qaU5ehGrgEQSwV");
    final var newTransferHookProgramId = PublicKey.fromBase58Encoded("7cjXTZvHYGuFarmmsYqjXsyYZY5TMyeNmvidxPJfvQ1Q");

    final byte[] expectedData = Base64.getDecoder().decode("JAFiTYiACXf8S3S3AvEVNeOMHAEhHcY+xbSY6XcSCFeVmw==");

    final var solAccounts = SolanaAccounts.MAIN_NET;
    final var updateTransferHookIx = Token2022Program.updateTransferHook(
        solAccounts,
        mintAccount,
        mintAuthority,
        newTransferHookProgramId
    );

    assertEquals(solAccounts.invokedToken2022Program(), updateTransferHookIx.programId());

    final var accounts = updateTransferHookIx.accounts();
    assertEquals(2, accounts.size());
    assertEquals(AccountMeta.createWrite(mintAccount), accounts.getFirst());
    assertEquals(AccountMeta.createReadOnlySigner(mintAuthority), accounts.getLast());

    assertArrayEquals(expectedData, updateTransferHookIx.data());
  }

  @Test
  void updateMintMetadataAccount() {
    // devnet 3iKA2XCusAq2uCxuGyWhw8oBkdYQMQMq87t5sJTXJpCcD169NDzDjBE3fcuTv6Dg8QpjC4QNmwxZXFhSB8DLZkj2

    final var mintAccount = PublicKey.fromBase58Encoded("HCRDkSQ6vM9QxDkJMGNUmVKjWqPYudkEsZRDwoJvyzQE");
    final var mintAuthority = PublicKey.fromBase58Encoded("CvUqgjP892h66aYPC9E8gKTXnTebY8qaU5ehGrgEQSwV");
    final var newMetadataAddress = PublicKey.fromBase58Encoded("AsFagyk29GvS8dtibZ6vjtbfwjnMzn9xHcEzoAnRusCB");

    final byte[] expectedData = Base64.getDecoder().decode("JwGSmLWb6FaiJaQHWUI5v71XJ6PVnzJ4PGbU7A2sL30skA==");

    final var solAccounts = SolanaAccounts.MAIN_NET;
    final var updateMetadataPointerIx = Token2022Program.updateMetadataPointer(
        solAccounts,
        mintAccount,
        mintAuthority,
        newMetadataAddress
    );

    assertEquals(solAccounts.invokedToken2022Program(), updateMetadataPointerIx.programId());

    final var accounts = updateMetadataPointerIx.accounts();
    assertEquals(2, accounts.size());
    assertEquals(AccountMeta.createWrite(mintAccount), accounts.getFirst());
    assertEquals(AccountMeta.createReadOnlySigner(mintAuthority), accounts.getLast());

    assertArrayEquals(expectedData, updateMetadataPointerIx.data());
  }
}
