package software.sava.solana.programs.system;

import org.junit.jupiter.api.Test;
import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.encoding.Base58;
import software.sava.solana.programs.token.Token2022Program;
import software.sava.solana.programs.token.TokenProgram;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class TokenProgramTests {

  @Test
  void initializeMint() {
    final var mint = PublicKey.fromBase58Encoded("3ntfH5pyhTGePb2cv2gqhyBmZHVW3EggCnbq1ND7YmgX");
    final var authority = PublicKey.fromBase58Encoded("2NYZ8sqfCnH5gWwvb3E8eYv9DeMkaHQE9EZVjNBZAVYJ");
    final int decimals = 6;

    final byte[] expectedData = Base64.getDecoder().decode("""
        AAYUYI/tKjCaCLC2naec1R2jSt1cnmJq02rXYP/HQvM47wEUYI/tKjCaCLC2naec1R2jSt1cnmJq02rXYP/HQvM47w==""".stripTrailing());

    final var solAccounts = SolanaAccounts.MAIN_NET;
    var initMintIx = TokenProgram.initializeMint(
        solAccounts,
        mint,
        decimals,
        authority,
        authority
    );

    assertEquals(solAccounts.invokedTokenProgram(), initMintIx.programId());

    var accounts = initMintIx.accounts();
    assertEquals(2, accounts.size());
    assertEquals(AccountMeta.createWrite(mint), accounts.getFirst());
    assertEquals(solAccounts.readRentSysVar(), accounts.getLast());

    assertArrayEquals(expectedData, initMintIx.data());

    expectedData[0] = (byte) TokenProgram.TokenInstruction.InitializeMint2.ordinal();
    initMintIx = TokenProgram.initializeMint2(
        solAccounts,
        mint,
        decimals,
        authority,
        authority
    );

    assertEquals(solAccounts.invokedTokenProgram(), initMintIx.programId());

    accounts = initMintIx.accounts();
    assertEquals(1, accounts.size());
    assertEquals(AccountMeta.createWrite(mint), accounts.getFirst());

    assertArrayEquals(expectedData, initMintIx.data());


    expectedData[0] = (byte) Token2022Program.TokenInstruction.InitializeMint.ordinal();
    initMintIx = Token2022Program.initializeMint(
        solAccounts,
        mint,
        decimals,
        authority,
        authority
    );

    assertEquals(solAccounts.invokedToken2022Program(), initMintIx.programId());

    accounts = initMintIx.accounts();
    assertEquals(2, accounts.size());
    assertEquals(AccountMeta.createWrite(mint), accounts.getFirst());
    assertEquals(solAccounts.readRentSysVar(), accounts.getLast());

    assertArrayEquals(expectedData, initMintIx.data());

    expectedData[0] = (byte) Token2022Program.TokenInstruction.InitializeMint2.ordinal();
    initMintIx = Token2022Program.initializeMint2(
        solAccounts,
        mint,
        decimals,
        authority,
        authority
    );

    assertEquals(solAccounts.invokedToken2022Program(), initMintIx.programId());

    accounts = initMintIx.accounts();
    assertEquals(1, accounts.size());
    assertEquals(AccountMeta.createWrite(mint), accounts.getFirst());

    assertArrayEquals(expectedData, initMintIx.data());
  }

  @Test
  void createMintWithTransferHook() {
    // devnet 3b7rYDCdxymqBXR3FLFgUtUoQyoYGg2ZF2bbKviuSQToSWyZeJmfb2wpjpTsZRf8FCWVMtuNetTAz2EAvmRSZLUi

    final var mintAccount = PublicKey.fromBase58Encoded("88WLQK58mbqNjaUBxYjEvhvdsWGQde4s1EqyagvEng2f");
    final var mintAuthority = PublicKey.fromBase58Encoded("CvUqgjP892h66aYPC9E8gKTXnTebY8qaU5ehGrgEQSwV");
    final var programAccount = PublicKey.fromBase58Encoded("2o6gvxp17hkML8Rz3cvqzbSTFStES287fYeDPeHhF7Vj");

    final byte[] expectedData = Base58.decode("""
          F2LRfuZ8F9SkUvoY2DcGDFXNksBcb4d4UbpQyYcyRZjde31xioLJauJKYwRxjEjAuzMNPepJSHH3njMhSzFgvdM4Gy""".stripTrailing());


    final var solAccounts = SolanaAccounts.MAIN_NET;
    var initializeTransferHookIx = Token2022Program.initializeTransferHook(
            solAccounts,
            mintAccount,
            mintAuthority,
            programAccount
    );

    assertEquals(solAccounts.invokedToken2022Program(), initializeTransferHookIx.programId());

    var accounts = initializeTransferHookIx.accounts();
    assertEquals(1, accounts.size());
    assertEquals(AccountMeta.createWrite(mintAccount), accounts.getFirst());


    assertArrayEquals(expectedData, initializeTransferHookIx.data());

  }

  @Test
  void createMintWithMetadataPointer() {
    // devnet 3b7rYDCdxymqBXR3FLFgUtUoQyoYGg2ZF2bbKviuSQToSWyZeJmfb2wpjpTsZRf8FCWVMtuNetTAz2EAvmRSZLUi

    final var mint = PublicKey.fromBase58Encoded("88WLQK58mbqNjaUBxYjEvhvdsWGQde4s1EqyagvEng2f");
    final var authority = PublicKey.fromBase58Encoded("CvUqgjP892h66aYPC9E8gKTXnTebY8qaU5ehGrgEQSwV");

    final byte[] expectedData = Base58.decode("""
    GC7FSeyRsRSWqdePvGFp5oZSbvCin5dinmBb7X5fn9DzNcfCdmyXiTV9iEzEZRrkmv3ixyvggyPXnUNyTekHbNx3Ph""".stripTrailing());


    final var solAccounts = SolanaAccounts.MAIN_NET;
    var initMetadataPointerIx = Token2022Program.initializeMetadataPointer(
            solAccounts,
            mint,
            authority,
            mint
    );

    assertEquals(solAccounts.invokedToken2022Program(), initMetadataPointerIx.programId());

    var accounts = initMetadataPointerIx.accounts();
    assertEquals(1, accounts.size());
    assertEquals(AccountMeta.createWrite(mint), accounts.getFirst());

    assertArrayEquals(expectedData, initMetadataPointerIx.data());

  }
  @Test
  void createMintWithInitializingMetadata() {
    // devnet 3b7rYDCdxymqBXR3FLFgUtUoQyoYGg2ZF2bbKviuSQToSWyZeJmfb2wpjpTsZRf8FCWVMtuNetTAz2EAvmRSZLUi

    final var name = "SimpleTestCoin";
    final var symbol = "STC";
    final var uri  = "https://example.com/metadata.json";
    final var mintAccount = PublicKey.fromBase58Encoded("88WLQK58mbqNjaUBxYjEvhvdsWGQde4s1EqyagvEng2f");
    final var metadataAccount = PublicKey.fromBase58Encoded("88WLQK58mbqNjaUBxYjEvhvdsWGQde4s1EqyagvEng2f");
    final var mintAuthority = PublicKey.fromBase58Encoded("CvUqgjP892h66aYPC9E8gKTXnTebY8qaU5ehGrgEQSwV");
    final var updateAuthority = PublicKey.fromBase58Encoded("CvUqgjP892h66aYPC9E8gKTXnTebY8qaU5ehGrgEQSwV");

    final byte[] expectedData = Base58.decode("""
          AGUhRKBLRk1Ueut5CpnmUkTSsn2Fpg3v3un8sZ52wUqQh5ZvW8ots8FCc3MtpVSzANADodfMKGeGjhkTv59ziTJPF3XZ9LnH""".stripTrailing());


    final var solAccounts = SolanaAccounts.MAIN_NET;
    var initializeTokenMetadataIx = Token2022Program.initializeTokenMetadataInstruction(
            solAccounts,
            metadataAccount,
            mintAuthority,
            updateAuthority,
            mintAccount,
            name,
            symbol,
            uri
    );

    assertEquals(solAccounts.invokedToken2022Program(), initializeTokenMetadataIx.programId());

    var accounts = initializeTokenMetadataIx.accounts();
    assertEquals(4, accounts.size());
    assertEquals(AccountMeta.createWrite(metadataAccount), accounts.getFirst());
    assertEquals(AccountMeta.createRead(updateAuthority), accounts.get(1));
    assertEquals(AccountMeta.createRead(mintAccount), accounts.get(2));
    assertEquals(AccountMeta.createReadOnlySigner(mintAuthority), accounts.get(3));

    assertArrayEquals(expectedData, initializeTokenMetadataIx.data());

  }

  @Test
  void updateTransferHookAccount() {
    // devnet THZ3HTPAQZaEj6ggHSaLSxSS5CeGYp88VDa6NyXxY7pV9khHk1xJk1yHqP4jWByHjBUz34UuWLPffWQfeCzjNyi

    final var mintAccount = PublicKey.fromBase58Encoded("HCRDkSQ6vM9QxDkJMGNUmVKjWqPYudkEsZRDwoJvyzQE");
    final var mintAuthority = PublicKey.fromBase58Encoded("CvUqgjP892h66aYPC9E8gKTXnTebY8qaU5ehGrgEQSwV");
    final var newTransferHookProgramId = PublicKey.fromBase58Encoded("7cjXTZvHYGuFarmmsYqjXsyYZY5TMyeNmvidxPJfvQ1Q");

    final byte[] expectedData = Base58.decode("""
          pD8q5bQ9YX5HQ6qxGodu61fJtfqPFHjAKoTLes7gCwnme2""".stripTrailing());


    final var solAccounts = SolanaAccounts.MAIN_NET;
    var updateTransferHookIx = Token2022Program.updateTransferHook(
            solAccounts,
            mintAccount,
            mintAuthority,
            newTransferHookProgramId
    );

    assertEquals(solAccounts.invokedToken2022Program(), updateTransferHookIx.programId());

    var accounts = updateTransferHookIx.accounts();
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

    final byte[] expectedData = Base58.decode("""
          t9LQrHqNgyQXjTT4nYpfgsSqXM8sAQVo3zQ8kxyueAPmrf""".stripTrailing());


    final var solAccounts = SolanaAccounts.MAIN_NET;
    var updateMetadataPointerIx = Token2022Program.updateMetadataPointer(
            solAccounts,
            mintAccount,
            mintAuthority,
            newMetadataAddress
    );

    assertEquals(solAccounts.invokedToken2022Program(), updateMetadataPointerIx.programId());

    var accounts = updateMetadataPointerIx.accounts();
    assertEquals(2, accounts.size());
    assertEquals(AccountMeta.createWrite(mintAccount), accounts.getFirst());
    assertEquals(AccountMeta.createReadOnlySigner(mintAuthority), accounts.getLast());

    assertArrayEquals(expectedData, updateMetadataPointerIx.data());

  }

}
