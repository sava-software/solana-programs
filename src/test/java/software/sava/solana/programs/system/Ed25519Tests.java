package software.sava.solana.programs.system;

import org.junit.jupiter.api.Test;
import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.tx.Transaction;
import software.sava.core.tx.TransactionSkeleton;
import software.sava.solana.programs.ed25519.Ed25519SignatureOffsets;

import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

final class Ed25519Tests {

  @Test
  void parseVerifySig() {
    final var solanaAccounts = SolanaAccounts.MAIN_NET;
    final byte[] data = Base64.getDecoder().decode("""
        AUtAF4nEONye36nRnwuYCENB4v/gTRGheOvwMjIVy1p3Yt4zltjgbsCnBCE3uhsLTweJ5tujQnKEYb+8QzIO5QmAAQAEDd99Z6UTtp321u6/nTONgzk6qxRNj3/PQET8wiSKG75OChN4yTK1i4Ts7wfwuUWF/22HgBxfvHy4wJkougO65l5HpxONNGN6Ay/cZJu5nmMlLMI2jWOk0taG946X9TqDkGAx9IGNoT76IajAgBQHvC+5sxDdeqP4YtI6BtMBK/7TaUbQ1l56peGYIXNmUajgksMDPXaVUUsTvhDULJozudh4Uhyxec67hYm1VqLV7JTSSYaC/fm7KvWtZOSRzEFT2psYjg+Nu8EC4plOCb/FWX8SdqwCcE5VHPV7HcWGGgwesy4L8IUj7e3FCMnR6r6qBfZdrIMXFf5D/ynxUZ8TH/u179HvS/yl6+DHyVpN4ldhAOUDy80kQDbvw3yoZsx2EQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAwZGb+UhFzL/7K26csOb57yM5bvF9xJrLEObOkAAAAADfUbWfJP7vhL5Qo+DjUD/BXB0SSf0imT8ynBEgAAAAAlU276eyWDJinopP+ITNpZv4YDRUa5LgXlWH4mFSlP2mw8Vskr+FFvpu5o1/xQk1AQdVIMptuQCkV1p28WYSMkGCgAJA0CcAAAAAAAACgAFAoAaBgAJAgAFDAIAAAAgTgAAAAAAAAsAEAEADAAEAEwABABuAKIABAAMCxIIAgQADhUXERMNkQIgT2WLGQZiDwQBAAC28Xp2BheE25F/tVr8jYRs0srNO7eQpMq1pCq5hEXZALIvnSuPzICL9aGXoGFbABb8OtGtZrHhSJrsmSJDOCwLSBXCpGw3/FxrF3HFpf6Ve3Z5YAFL5BVQT5J7s3HwEGSiAGM4ZDVhNjVlMjIzNGY1NWQwMDAxMDEwMDgwOTY5ODAwMDAwMDAwMDBhN2NjNzYwYTAwMDAwMDAwMDAwMDAxMDAwMDAwMDAwMDAxMDAwMDAwMDAwMTE0MDE1YTliODUwYTAwMDAwMDAwMDFmODRkODIwYTAwMDAwMDAwMDAwMGViYTQ2ZTE0MDAwMDAwMDA0NjU0N2E2Mzc1NDIzNjU0MDAwMAAMEhIDBggCBAAUFRYXERMPEA0BBzAQGnuDXh2vYgEBANcRxzjdCwAAAFjngwoAAAAAAAAAAQEAAAAAAAAARlR6Y3VCNlQC3E42Kl7pkP+vCgX6bK+0TFMtWHz2EUnYgJSxOMI1cDcBAQajBQNcg1vLygm9ic+3HuQ3aieF/D5bxgfvpmUf3AN/Y9/3gKtTZAAEMTApKg==
        """.stripTrailing());

    final var skeleton = TransactionSkeleton.deserializeSkeleton(data);
    final var instructions = skeleton.parseInstructionsWithoutAccounts();

    var verifySigIx = instructions[3];
    assertEquals(solanaAccounts.ed25519Program(), verifySigIx.programId().publicKey());

    byte[] ixData = verifySigIx.copyData();
    var signatureOffsetsArray = Ed25519SignatureOffsets.readVector(ixData, 0);
    assertEquals(1, signatureOffsetsArray.length);

    var signatureOffsets = signatureOffsetsArray[0];
    assertEquals(12, signatureOffsets.signatureOffset());
    assertEquals(4, signatureOffsets.signatureInstructionIndex());
    assertEquals(76, signatureOffsets.publicKeyOffset());
    assertEquals(4, signatureOffsets.publicKeyInstructionIndex());
    assertEquals(110, signatureOffsets.messageDataOffset());
    assertEquals(162, signatureOffsets.messageDataSize());
    assertEquals(4, signatureOffsets.messageInstructionIndex());

    var referenceInstruction = instructions[4];
    byte[] referenceData = referenceInstruction.copyData();

    var publicKey = PublicKey.readPubKey(referenceData, signatureOffsets.publicKeyOffset());
    assertEquals("5rPbkdhCWfpBthPM8E3GW7cQAjiWZmHpsFN3kpZvozCb", publicKey.toBase58());

    byte[] sig = Arrays.copyOfRange(referenceData, signatureOffsets.signatureOffset(), signatureOffsets.signatureOffset() + Transaction.SIGNATURE_LENGTH);
    assertArrayEquals(
        Base64.getDecoder().decode("tvF6dgYXhNuRf7Va/I2EbNLKzTu3kKTKtaQquYRF2QCyL50rj8yAi/Whl6BhWwAW/DrRrWax4Uia7JkiQzgsCw=="),
        sig
    );

    byte[] msg = Arrays.copyOfRange(referenceData, signatureOffsets.messageDataOffset(), signatureOffsets.messageDataOffset() + signatureOffsets.messageDataSize());
    assertArrayEquals(
        Base64.getDecoder().decode("YzhkNWE2NWUyMjM0ZjU1ZDAwMDEwMTAwODA5Njk4MDAwMDAwMDAwMGE3Y2M3NjBhMDAwMDAwMDAwMDAwMDEwMDAwMDAwMDAwMDEwMDAwMDAwMDAxMTQwMTVhOWI4NTBhMDAwMDAwMDAwMWY4NGQ4MjBhMDAwMDAwMDAwMDAwZWJhNDZlMTQwMDAwMDAwMDQ2NTQ3YTYzNzU0MjM2NTQwMDAw"),
        msg
    );

    assertTrue(PublicKey.verifySignature(
        publicKey.toByteArray(), 0,
        msg, 0, msg.length,
        sig
    ));
  }
}
