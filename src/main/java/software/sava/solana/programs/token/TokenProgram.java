package software.sava.solana.programs.token;

import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.encoding.ByteUtil;
import software.sava.core.programs.Discriminator;
import software.sava.core.tx.Instruction;

import java.util.Arrays;
import java.util.List;

import static software.sava.core.accounts.PublicKey.PUBLIC_KEY_LENGTH;
import static software.sava.core.accounts.meta.AccountMeta.*;
import static software.sava.core.tx.Instruction.createInstruction;

// https://github.com/solana-program/token/blob/main/program/src/instruction.rs#L23
// https://github.com/solana-program/token-2022/blob/main/program/src/instruction.rs
public final class TokenProgram {

  public enum AuthorityType {
    /// Authority to mint new tokens
    MintTokens,
    /// Authority to freeze any account associated with the Mint
    FreezeAccount,
    /// Owner of a given token account
    AccountOwner,
    /// Authority to close a token account
    CloseAccount,
  }

  public enum TokenInstruction implements Discriminator {

    // Initializes a new mint and optionally deposits all the newly minted
    // tokens in an account.
    //
    // The 'InitializeMint' instruction requires no signers and MUST be
    // included within the same Transaction as the system program's
    // 'CreateAccount' instruction that creates the account being initialized.
    // Otherwise another party can acquire ownership of the uninitialized
    // account.
    //
    // Accounts expected by this instruction:
    //
    //   0. '[writable]' The mint to initialize.
    //   1. '[]' Rent sysvar
    InitializeMint,
    // Number of base 10 digits to the right of the decimal place.
//    decimals: u8,
//        // The authority/multisignature to mint tokens.
//        mint_authority: Pubkey,
//        // The freeze authority/multisignature of the mint.
//        freeze_authority: COption<Pubkey>

    // Initializes a new account to hold tokens.  If this account is associated
    // with the native mint then the token balance of the initialized account
    // will be equal to the amount of SOL in the account. If this account is
    // associated with another mint, that mint must be initialized before this
    // command can succeed.
    //
    // The 'InitializeAccount' instruction requires no signers and MUST be
    // included within the same Transaction as the system program's
    // 'CreateAccount' instruction that creates the account being initialized.
    // Otherwise another party can acquire ownership of the uninitialized
    // account.
    //
    // Accounts expected by this instruction:
    //
    //   0. '[writable]'  The account to initialize.
    //   1. '[]' The mint this account will be associated with.
    //   2. '[]' The new account's owner/multisignature.
    //   3. '[]' Rent sysvar
    InitializeAccount,

    // Initializes a multisignature account with N provided signers.
    //
    // Multisignature accounts can used in place of any single owner/delegate
    // accounts in any token instruction that require an owner/delegate to be
    // present.  The variant field represents the number of signers (M)
    // required to validate this multisignature account.
    //
    // The 'InitializeMultisig' instruction requires no signers and MUST be
    // included within the same Transaction as the system program's
    // 'CreateAccount' instruction that creates the account being initialized.
    // Otherwise another party can acquire ownership of the uninitialized
    // account.
    //
    // Accounts expected by this instruction:
    //
    //   0. '[writable]' The multisignature account to initialize.
    //   1. '[]' Rent sysvar
    //   2. ..2+N. '[]' The signer accounts, must equal to N where 1 <= N <=
    //      11.
    InitializeMultisig,
    // The number of signers (M) required to validate this multisignature
    // account.
//    m: u8,

    // Transfers tokens from one account to another either directly or via a
    // delegate.  If this account is associated with the native mint then equal
    // amounts of SOL and Tokens will be transferred to the destination
    // account.
    //
    // Accounts expected by this instruction:
    //
    //   * Single owner/delegate
    //   0. '[writable]' The source account.
    //   1. '[writable]' The destination account.
    //   2. '[signer]' The source account's owner/delegate.
    //
    //   * Multisignature owner/delegate
    //   0. '[writable]' The source account.
    //   1. '[writable]' The destination account.
    //   2. '[]' The source account's multisignature owner/delegate.
    //   3. ..3+M '[signer]' M signer accounts.
    Transfer,
    // The amount of tokens to transfer.
//    amount: u64,

    // Approves a delegate.  A delegate is given the authority over tokens on
    // behalf of the source account's owner.
    //
    // Accounts expected by this instruction:
    //
    //   * Single owner
    //   0. '[writable]' The source account.
    //   1. '[]' The delegate.
    //   2. '[signer]' The source account owner.
    //
    //   * Multisignature owner
    //   0. '[writable]' The source account.
    //   1. '[]' The delegate.
    //   2. '[]' The source account's multisignature owner.
    //   3. ..3+M '[signer]' M signer accounts
    Approve,
    // The amount of tokens the delegate is approved for.
//    amount: u64,

    // Revokes the delegate's authority.
    //
    // Accounts expected by this instruction:
    //
    //   * Single owner
    //   0. '[writable]' The source account.
    //   1. '[signer]' The source account owner.
    //
    //   * Multisignature owner
    //   0. '[writable]' The source account.
    //   1. '[]' The source account's multisignature owner.
    //   2. ..2+M '[signer]' M signer accounts
    Revoke,

    // Sets a new authority of a mint or account.
    //
    // Accounts expected by this instruction:
    //
    //   * Single authority
    //   0. '[writable]' The mint or account to change the authority of.
    //   1. '[signer]' The current authority of the mint or account.
    //
    //   * Multisignature authority
    //   0. '[writable]' The mint or account to change the authority of.
    //   1. '[]' The mint's or account's current multisignature authority.
    //   2. ..2+M '[signer]' M signer accounts
    SetAuthority,
    // The type of authority to update.
//    authority_type: AuthorityType,
//        // The new authority
//        new_authority: COption<Pubkey>,

    // Mints new tokens to an account.  The native mint does not support
    // minting.
    //
    // Accounts expected by this instruction:
    //
    //   * Single authority
    //   0. '[writable]' The mint.
    //   1. '[writable]' The account to mint tokens to.
    //   2. '[signer]' The mint's minting authority.
    //
    //   * Multisignature authority
    //   0. '[writable]' The mint.
    //   1. '[writable]' The account to mint tokens to.
    //   2. '[]' The mint's multisignature mint-tokens authority.
    //   3. ..3+M '[signer]' M signer accounts.
    MintTo,
    // The amount of new tokens to mint.
//    amount: u64,

    // Burns tokens by removing them from an account.  'Burn' does not support
    // accounts associated with the native mint, use 'CloseAccount' instead.
    //
    // Accounts expected by this instruction:
    //
    //   * Single owner/delegate
    //   0. '[writable]' The account to burn from.
    //   1. '[writable]' The token mint.
    //   2. '[signer]' The account's owner/delegate.
    //
    //   * Multisignature owner/delegate
    //   0. '[writable]' The account to burn from.
    //   1. '[writable]' The token mint.
    //   2. '[]' The account's multisignature owner/delegate.
    //   3. ..3+M '[signer]' M signer accounts.
    Burn,
    // The amount of tokens to burn.
//    amount: u64,

    // Close an account by transferring all its SOL to the destination account.
    // Non-native accounts may only be closed if its token amount is zero.
    //
    // Accounts expected by this instruction:
    //
    //   * Single owner
    //   0. '[writable]' The account to close.
    //   1. '[writable]' The destination account.
    //   2. '[signer]' The account's owner.
    //
    //   * Multisignature owner
    //   0. '[writable]' The account to close.
    //   1. '[writable]' The destination account.
    //   2. '[]' The account's multisignature owner.
    //   3. ..3+M '[signer]' M signer accounts.
    CloseAccount,

    // Freeze an Initialized account using the Mint's freeze_authority (if
    // set).
    //
    // Accounts expected by this instruction:
    //
    //   * Single owner
    //   0. '[writable]' The account to freeze.
    //   1. '[]' The token mint.
    //   2. '[signer]' The mint freeze authority.
    //
    //   * Multisignature owner
    //   0. '[writable]' The account to freeze.
    //   1. '[]' The token mint.
    //   2. '[]' The mint's multisignature freeze authority.
    //   3. ..3+M '[signer]' M signer accounts.
    FreezeAccount,

    // Thaw a Frozen account using the Mint's freeze_authority (if set).
    //
    // Accounts expected by this instruction:
    //
    //   * Single owner
    //   0. '[writable]' The account to freeze.
    //   1. '[]' The token mint.
    //   2. '[signer]' The mint freeze authority.
    //
    //   * Multisignature owner
    //   0. '[writable]' The account to freeze.
    //   1. '[]' The token mint.
    //   2. '[]' The mint's multisignature freeze authority.
    //   3. ..3+M '[signer]' M signer accounts.
    ThawAccount,

    // Transfers tokens from one account to another either directly or via a
    // delegate.  If this account is associated with the native mint then equal
    // amounts of SOL and Tokens will be transferred to the destination
    // account.
    //
    // This instruction differs from Transfer in that the token mint and
    // decimals value is checked by the caller.  This may be useful when
    // creating transactions offline or within a hardware wallet.
    //
    // Accounts expected by this instruction:
    //
    //   * Single owner/delegate
    //   0. '[writable]' The source account.
    //   1. '[]' The token mint.
    //   2. '[writable]' The destination account.
    //   3. '[signer]' The source account's owner/delegate.
    //
    //   * Multisignature owner/delegate
    //   0. '[writable]' The source account.
    //   1. '[]' The token mint.
    //   2. '[writable]' The destination account.
    //   3. '[]' The source account's multisignature owner/delegate.
    //   4. ..4+M '[signer]' M signer accounts.
    TransferChecked,
    // The amount of tokens to transfer.
//    amount: u64,
//        // Expected number of base 10 digits to the right of the decimal place.
//        decimals: u8,

    // Approves a delegate.  A delegate is given the authority over tokens on
    // behalf of the source account's owner.
    //
    // This instruction differs from Approve in that the token mint and
    // decimals value is checked by the caller.  This may be useful when
    // creating transactions offline or within a hardware wallet.
    //
    // Accounts expected by this instruction:
    //
    //   * Single owner
    //   0. '[writable]' The source account.
    //   1. '[]' The token mint.
    //   2. '[]' The delegate.
    //   3. '[signer]' The source account owner.
    //
    //   * Multisignature owner
    //   0. '[writable]' The source account.
    //   1. '[]' The token mint.
    //   2. '[]' The delegate.
    //   3. '[]' The source account's multisignature owner.
    //   4. ..4+M '[signer]' M signer accounts
    ApproveChecked,
    // The amount of tokens the delegate is approved for.
//    amount: u64,
//        // Expected number of base 10 digits to the right of the decimal place.
//        decimals: u8,

    // Mints new tokens to an account.  The native mint does not support
    // minting.
    //
    // This instruction differs from MintTo in that the decimals value is
    // checked by the caller.  This may be useful when creating transactions
    // offline or within a hardware wallet.
    //
    // Accounts expected by this instruction:
    //
    //   * Single authority
    //   0. '[writable]' The mint.
    //   1. '[writable]' The account to mint tokens to.
    //   2. '[signer]' The mint's minting authority.
    //
    //   * Multisignature authority
    //   0. '[writable]' The mint.
    //   1. '[writable]' The account to mint tokens to.
    //   2. '[]' The mint's multisignature mint-tokens authority.
    //   3. ..3+M '[signer]' M signer accounts.
    MintToChecked,
    // The amount of new tokens to mint.
//    amount: u64,
//        // Expected number of base 10 digits to the right of the decimal place.
//        decimals: u8,

    // Burns tokens by removing them from an account.  'BurnChecked' does not
    // support accounts associated with the native mint, use 'CloseAccount'
    // instead.
    //
    // This instruction differs from Burn in that the decimals value is checked
    // by the caller. This may be useful when creating transactions offline or
    // within a hardware wallet.
    //
    // Accounts expected by this instruction:
    //
    //   * Single owner/delegate
    //   0. '[writable]' The account to burn from.
    //   1. '[writable]' The token mint.
    //   2. '[signer]' The account's owner/delegate.
    //
    //   * Multisignature owner/delegate
    //   0. '[writable]' The account to burn from.
    //   1. '[writable]' The token mint.
    //   2. '[]' The account's multisignature owner/delegate.
    //   3. ..3+M '[signer]' M signer accounts.
    BurnChecked,
    // The amount of tokens to burn.
//    amount: u64,
//        // Expected number of base 10 digits to the right of the decimal place.
//        decimals: u8,

    // Like InitializeAccount, but the owner pubkey is passed via instruction
    // data rather than the accounts list. This variant may be preferable
    // when using Cross Program Invocation from an instruction that does
    // not need the owner's 'AccountInfo' otherwise.
    //
    // Accounts expected by this instruction:
    //
    //   0. '[writable]'  The account to initialize.
    //   1. '[]' The mint this account will be associated with.
    //   3. '[]' Rent sysvar
    InitializeAccount2,
    // The new account's owner/multisignature.
//    owner: Pubkey,

    // Given a wrapped / native token account (a token account containing SOL)
    // updates its amount field based on the account's underlying 'lamports'.
    // This is useful if a non-wrapped SOL account uses
    // 'system_instruction::transfer' to move lamports to a wrapped token
    // account, and needs to have its token 'amount' field updated.
    //
    // Accounts expected by this instruction:
    //
    //   0. '[writable]'  The native token account to sync with its underlying
    //      lamports.
    SyncNative,

    // Like InitializeAccount2, but does not require the Rent sysvar to be
    // provided
    //
    // Accounts expected by this instruction:
    //
    //   0. '[writable]'  The account to initialize.
    //   1. '[]' The mint this account will be associated with.
    InitializeAccount3,
    // The new account's owner/multisignature.
//    owner: Pubkey,

    // Like InitializeMultisig, but does not require the Rent sysvar to be
    // provided
    //
    // Accounts expected by this instruction:
    //
    //   0. '[writable]' The multisignature account to initialize.
    //   1. ..1+N. '[]' The signer accounts, must equal to N where 1 <= N <=
    //      11.
    InitializeMultisig2,
    // The number of signers (M) required to validate this multisignature
    // account.
//    m: u8,

    // Like ['InitializeMint'], but does not require the Rent sysvar to be
    // provided
    //
    // Accounts expected by this instruction:
    //
    //   0. '[writable]' The mint to initialize.
    InitializeMint2,
    // Number of base 10 digits to the right of the decimal place.
//    decimals: u8,
//        // The authority/multisignature to mint tokens.
//        mint_authority: Pubkey,
//        // The freeze authority/multisignature of the mint.
//        freeze_authority: COption<Pubkey>,

    // Gets the required size of an account for the given mint as a
    // little-endian 'u64'.
    //
    // Return data can be fetched using 'sol_get_return_data' and deserializing
    // the return data as a little-endian 'u64'.
    //
    // Accounts expected by this instruction:
    //
    //   0. '[]' The mint to calculate for
    GetAccountDataSize, // typically, there's also data, but this program ignores it

    // Initialize the Immutable Owner extension for the given token account
    //
    // Fails if the account has already been initialized, so must be called
    // before 'InitializeAccount'.
    //
    // No-ops in this version of the program, but is included for compatibility
    // with the Associated Token Account program.
    //
    // Accounts expected by this instruction:
    //
    //   0. '[writable]'  The account to initialize.
    //
    // Data expected by this instruction:
    //   None
    InitializeImmutableOwner,

    // Convert an Amount of tokens to a UiAmount 'string', using the given
    // mint. In this version of the program, the mint can only specify the
    // number of decimals.
    //
    // Fails on an invalid mint.
    //
    // Return data can be fetched using 'sol_get_return_data' and deserialized
    // with 'String::from_utf8'.
    //
    // Accounts expected by this instruction:
    //
    //   0. '[]' The mint to calculate for
    AmountToUiAmount,
    // The amount of tokens to reformat.
//    amount: u64,

    // Convert a UiAmount of tokens to a little-endian 'u64' raw Amount, using
    // the given mint. In this version of the program, the mint can only
    // specify the number of decimals.
    //
    // Return data can be fetched using 'sol_get_return_data' and deserializing
    // the return data as a little-endian 'u64'.
    //
    // Accounts expected by this instruction:
    //
    //   0. '[]' The mint to calculate for
    UiAmountToAmount;
    // The ui_amount of tokens to reformat.
//    ui_amount: &'a str,
    // Any new variants also need to be added to program-2022 'TokenInstruction', so that the
    // latter remains a superset of this instruction set. New variants also need to be added to
    // token/js/src/instructions/types.ts to maintain @solana/spl-token compatibility

    private final byte discriminator;
    private final byte[] discriminatorBytes;

    TokenInstruction() {
      this.discriminator = (byte) this.ordinal();
      this.discriminatorBytes = new byte[]{this.discriminator};
    }

    @Override
    public byte[] data() {
      return discriminatorBytes;
    }

    @Override
    public int write(final byte[] bytes, final int i) {
      bytes[i] = (byte) this.ordinal();
      return 1;
    }

    @Override
    public int length() {
      return 1;
    }
  }

  private static byte[] initializeMintData(final TokenInstruction tokenInstruction,
                                           final int decimals,
                                           final PublicKey mintAuthority,
                                           final PublicKey freezeAuthority) {
    final byte[] data;
    if (freezeAuthority == null) {
      data = new byte[1 + 1 + PUBLIC_KEY_LENGTH + 1];
      data[34] = (byte) 0;
    } else {
      data = new byte[1 + 1 + PUBLIC_KEY_LENGTH + 1 + PUBLIC_KEY_LENGTH];
      data[34] = (byte) 1;
      freezeAuthority.write(data, 35);
    }
    data[0] = tokenInstruction.discriminator;
    data[1] = (byte) (decimals & 0xFF);
    mintAuthority.write(data, 2);
    return data;
  }

  public static Instruction initializeMint(final AccountMeta invokedTokenProgram,
                                           final SolanaAccounts solanaAccounts,
                                           final PublicKey mint,
                                           final int decimals,
                                           final PublicKey mintAuthority,
                                           final PublicKey freezeAuthority) {
    final var keys = List.of(
        createWrite(mint),
        solanaAccounts.readRentSysVar()
    );
    final byte[] data = initializeMintData(TokenInstruction.InitializeMint, decimals, mintAuthority, freezeAuthority);
    return createInstruction(invokedTokenProgram, keys, data);
  }

  public static Instruction initializeMint(final SolanaAccounts solanaAccounts,
                                           final PublicKey mint,
                                           final int decimals,
                                           final PublicKey mintAuthority,
                                           final PublicKey freezeAuthority) {
    return initializeMint(solanaAccounts.invokedTokenProgram(), solanaAccounts, mint, decimals, mintAuthority, freezeAuthority);
  }

  public static Instruction initializeMint2(final AccountMeta invokedTokenProgram,
                                            final PublicKey mint,
                                            final int decimals,
                                            final PublicKey mintAuthority,
                                            final PublicKey freezeAuthority) {
    final var keys = List.of(createWrite(mint));
    final byte[] data = initializeMintData(TokenInstruction.InitializeMint2, decimals, mintAuthority, freezeAuthority);
    return createInstruction(invokedTokenProgram, keys, data);
  }

  public static Instruction initializeMint2(final SolanaAccounts solanaAccounts,
                                            final PublicKey mint,
                                            final int decimals,
                                            final PublicKey mintAuthority,
                                            final PublicKey freezeAuthority) {
    return initializeMint2(solanaAccounts.invokedTokenProgram(), mint, decimals, mintAuthority, freezeAuthority);
  }

  public static Instruction initializeAccount(final AccountMeta invokedTokenProgram,
                                              final SolanaAccounts solanaAccounts,
                                              final PublicKey account,
                                              final PublicKey mint,
                                              final PublicKey owner) {
    final var keys = List.of(
        createWrite(account),
        createRead(mint),
        createRead(owner),
        solanaAccounts.readRentSysVar()
    );
    return createInstruction(invokedTokenProgram, keys, TokenInstruction.InitializeAccount.discriminatorBytes);
  }

  public static Instruction initializeAccount(final SolanaAccounts solanaAccounts,
                                              final PublicKey account,
                                              final PublicKey mint,
                                              final PublicKey owner) {
    return initializeAccount(solanaAccounts.invokedTokenProgram(), solanaAccounts, account, mint, owner);
  }

  public static Instruction initializeAccount2(final AccountMeta invokedTokenProgram,
                                               final SolanaAccounts solanaAccounts,
                                               final PublicKey account,
                                               final PublicKey mint,
                                               final PublicKey owner) {
    final var keys = List.of(
        createWrite(account),
        createRead(mint),
        solanaAccounts.readRentSysVar()
    );

    final byte[] data = new byte[1 + PUBLIC_KEY_LENGTH];
    data[0] = TokenInstruction.InitializeAccount2.discriminator;
    owner.write(data, 1);

    return createInstruction(invokedTokenProgram, keys, data);
  }

  public static Instruction initializeAccount2(final SolanaAccounts solanaAccounts,
                                               final PublicKey account,
                                               final PublicKey mint,
                                               final PublicKey owner) {
    return initializeAccount2(solanaAccounts.invokedTokenProgram(), solanaAccounts, account, mint, owner);
  }

  public static Instruction initializeAccount3(final AccountMeta invokedTokenProgram,
                                               final PublicKey account,
                                               final PublicKey mint,
                                               final PublicKey owner) {
    final var keys = List.of(
        createWrite(account),
        createRead(mint)
    );

    final byte[] data = new byte[1 + PUBLIC_KEY_LENGTH];
    data[0] = TokenInstruction.InitializeAccount3.discriminator;
    owner.write(data, 1);

    return createInstruction(invokedTokenProgram, keys, data);
  }

  public static Instruction initializeAccount3(final SolanaAccounts solanaAccounts,
                                               final PublicKey account,
                                               final PublicKey mint,
                                               final PublicKey owner) {
    return initializeAccount3(solanaAccounts.invokedTokenProgram(), account, mint, owner);
  }

  static AccountMeta[] initSigners(int offset, final List<PublicKey> signerAccounts) {
    final var keys = new AccountMeta[offset + signerAccounts.size()];
    for (final var signerAccount : signerAccounts) {
      keys[offset++] = createRead(signerAccount);
    }
    return keys;
  }

  public static Instruction initializeMultisig(final AccountMeta invokedTokenProgram,
                                               final SolanaAccounts solanaAccounts,
                                               final PublicKey multisigAccount,
                                               final List<PublicKey> signerAccounts,
                                               final int requiredSignatures) {
    final var keys = initSigners(2, signerAccounts);
    keys[0] = createWrite(multisigAccount);
    keys[1] = solanaAccounts.readRentSysVar();

    final byte[] data = new byte[2];
    data[0] = TokenInstruction.InitializeMultisig.discriminator;
    data[1] = (byte) (requiredSignatures & 0xFF);

    return createInstruction(invokedTokenProgram, Arrays.asList(keys), data);
  }

  public static Instruction initializeMultisig(final SolanaAccounts solanaAccounts,
                                               final PublicKey multisigAccount,
                                               final List<PublicKey> signerAccounts,
                                               final int requiredSignatures) {
    return initializeMultisig(
        solanaAccounts.invokedTokenProgram(),
        solanaAccounts,
        multisigAccount,
        signerAccounts,
        requiredSignatures
    );
  }

  public static Instruction initializeMultisig2(final AccountMeta invokedTokenProgram,
                                                final PublicKey multisigAccount,
                                                final List<PublicKey> signerAccounts,
                                                final int requiredSignatures) {
    final var keys = initSigners(1, signerAccounts);
    keys[0] = createWrite(multisigAccount);

    final byte[] data = new byte[2];
    data[0] = TokenInstruction.InitializeMultisig2.discriminator;
    data[1] = (byte) (requiredSignatures & 0xFF);

    return createInstruction(invokedTokenProgram, Arrays.asList(keys), data);
  }

  private static byte[] amountData(final TokenInstruction tokenInstruction, final long amount) {
    final byte[] data = new byte[1 + Long.BYTES];
    data[0] = tokenInstruction.discriminator;
    ByteUtil.putInt64LE(data, 1, amount);
    return data;
  }

  private static byte[] transferData(final long amount) {
    return amountData(TokenInstruction.Transfer, amount);
  }

  public static Instruction transfer(final AccountMeta invokedProgram,
                                     final PublicKey source,
                                     final PublicKey destination,
                                     final long amount,
                                     final PublicKey owner) {
    final var keys = List.of(
        createWrite(source),
        createWrite(destination),
        createReadOnlySigner(owner)
    );

    final byte[] data = transferData(amount);

    return createInstruction(invokedProgram, keys, data);
  }

  public static Instruction transferMultisig(final AccountMeta invokedProgram,
                                             final PublicKey source,
                                             final PublicKey destination,
                                             final long amount,
                                             final PublicKey owner,
                                             final List<PublicKey> signerAccounts) {
    final var keys = initSigners(3, signerAccounts);
    keys[0] = createWrite(source);
    keys[1] = createWrite(destination);
    keys[2] = createRead(owner);

    final byte[] data = transferData(amount);

    return createInstruction(invokedProgram, Arrays.asList(keys), data);
  }

  private static byte[] checkedAmountData(final TokenInstruction tokenInstruction,
                                          final long amount,
                                          final int decimals) {
    final byte[] data = new byte[10];
    data[0] = tokenInstruction.discriminator;
    ByteUtil.putInt64LE(data, 1, amount);
    data[9] = (byte) (decimals & 0xFF);
    return data;
  }

  private static byte[] transferCheckedData(final long amount, final int decimals) {
    return checkedAmountData(TokenInstruction.TransferChecked, amount, decimals);
  }

  public static Instruction transferChecked(final AccountMeta invokedProgram,
                                            final PublicKey source,
                                            final PublicKey destination,
                                            final long amount,
                                            final int decimals,
                                            final PublicKey owner,
                                            final PublicKey tokenMint) {
    final var keys = List.of(
        createWrite(source),
        createRead(tokenMint),
        createWrite(destination),
        createReadOnlySigner(owner)
    );
    final byte[] data = transferCheckedData(amount, decimals);
    return createInstruction(invokedProgram, keys, data);
  }

  public static Instruction transferCheckedMultisig(final AccountMeta invokedProgram,
                                                    final PublicKey source,
                                                    final PublicKey destination,
                                                    final long amount,
                                                    final int decimals,
                                                    final PublicKey owner,
                                                    final PublicKey tokenMint,
                                                    final List<PublicKey> signerAccounts) {
    final var keys = initSigners(4, signerAccounts);
    keys[0] = createWrite(source);
    keys[1] = createRead(tokenMint);
    keys[2] = createWrite(destination);
    keys[3] = createRead(owner);

    final byte[] data = transferCheckedData(amount, decimals);

    return createInstruction(invokedProgram, Arrays.asList(keys), data);
  }

  private static byte[] approveData(final long amount) {
    return amountData(TokenInstruction.Approve, amount);
  }

  public static Instruction approve(final AccountMeta invokedTokenProgram,
                                    final PublicKey sourceAccount,
                                    final PublicKey delegate,
                                    final PublicKey owner,
                                    final long amount) {
    final var keys = List.of(
        createWrite(sourceAccount),
        createRead(delegate),
        createReadOnlySigner(owner)
    );

    final byte[] data = approveData(amount);

    return createInstruction(invokedTokenProgram, keys, data);
  }

  public static Instruction approve(final SolanaAccounts solanaAccounts,
                                    final PublicKey sourceAccount,
                                    final PublicKey delegate,
                                    final PublicKey owner,
                                    final long amount) {
    return approve(solanaAccounts.invokedTokenProgram(), sourceAccount, delegate, owner, amount);
  }

  public static Instruction approveMultisig(final AccountMeta invokedTokenProgram,
                                            final PublicKey sourceAccount,
                                            final PublicKey delegate,
                                            final PublicKey owner,
                                            final List<PublicKey> signerAccounts,
                                            final long amount) {
    final var keys = initSigners(3, signerAccounts);
    keys[0] = createWrite(sourceAccount);
    keys[1] = createRead(delegate);
    keys[2] = createRead(owner);

    final byte[] data = approveData(amount);

    return createInstruction(invokedTokenProgram, Arrays.asList(keys), data);
  }

  public static Instruction approveMultisig(final SolanaAccounts solanaAccounts,
                                            final PublicKey sourceAccount,
                                            final PublicKey delegate,
                                            final PublicKey owner,
                                            final List<PublicKey> signerAccounts,
                                            final long amount) {
    return approveMultisig(
        solanaAccounts.invokedTokenProgram(),
        sourceAccount,
        delegate,
        owner,
        signerAccounts,
        amount
    );
  }

  private static byte[] approveCheckedData(final long amount, final int decimals) {
    return checkedAmountData(TokenInstruction.ApproveChecked, amount, decimals);
  }

  public static Instruction approveChecked(final AccountMeta invokedTokenProgram,
                                           final PublicKey sourceAccount,
                                           final PublicKey tokenMint,
                                           final int decimals,
                                           final PublicKey delegate,
                                           final PublicKey owner,
                                           final long amount) {
    final var keys = List.of(
        createWrite(sourceAccount),
        createRead(tokenMint),
        createRead(delegate),
        createReadOnlySigner(owner)
    );

    final byte[] data = approveCheckedData(amount, decimals);

    return createInstruction(invokedTokenProgram, keys, data);
  }

  public static Instruction approveChecked(final SolanaAccounts solanaAccounts,
                                           final PublicKey sourceAccount,
                                           final PublicKey tokenMint,
                                           final int decimals,
                                           final PublicKey delegate,
                                           final PublicKey owner,
                                           final long amount) {
    return approveChecked(
        solanaAccounts.invokedTokenProgram(),
        sourceAccount,
        tokenMint,
        decimals,
        delegate,
        owner,
        amount
    );
  }

  public static Instruction approveCheckedMultisig(final AccountMeta invokedTokenProgram,
                                                   final PublicKey sourceAccount,
                                                   final PublicKey tokenMint,
                                                   final int decimals,
                                                   final PublicKey delegate,
                                                   final PublicKey owner,
                                                   final List<PublicKey> signerAccounts,
                                                   final long amount) {
    final var keys = initSigners(4, signerAccounts);
    keys[0] = createWrite(sourceAccount);
    keys[1] = createRead(tokenMint);
    keys[2] = createRead(delegate);
    keys[3] = createRead(owner);

    final byte[] data = approveCheckedData(amount, decimals);

    return createInstruction(invokedTokenProgram, Arrays.asList(keys), data);
  }

  public static Instruction approveCheckedMultisig(final SolanaAccounts solanaAccounts,
                                                   final PublicKey sourceAccount,
                                                   final PublicKey tokenMint,
                                                   final int decimals,
                                                   final PublicKey delegate,
                                                   final PublicKey owner,
                                                   final List<PublicKey> signerAccounts,
                                                   final long amount) {
    return approveCheckedMultisig(
        solanaAccounts.invokedTokenProgram(),
        sourceAccount,
        tokenMint,
        decimals,
        delegate,
        owner,
        signerAccounts,
        amount
    );
  }

  public static Instruction revoke(final AccountMeta invokedTokenProgram,
                                   final PublicKey sourceAccount,
                                   final PublicKey owner) {
    final var keys = List.of(
        createWrite(sourceAccount),
        createReadOnlySigner(owner)
    );
    return createInstruction(invokedTokenProgram, keys, TokenInstruction.Revoke.discriminatorBytes);
  }

  public static Instruction revoke(final SolanaAccounts solanaAccounts,
                                   final PublicKey sourceAccount,
                                   final PublicKey owner) {
    return revoke(solanaAccounts.invokedTokenProgram(), sourceAccount, owner);
  }

  public static Instruction revokeMultisig(final AccountMeta invokedTokenProgram,
                                           final PublicKey sourceAccount,
                                           final PublicKey owner,
                                           final List<PublicKey> signerAccounts) {
    final var keys = initSigners(2, signerAccounts);
    keys[0] = createWrite(sourceAccount);
    keys[1] = createRead(owner);

    return createInstruction(invokedTokenProgram, Arrays.asList(keys), TokenInstruction.Revoke.discriminatorBytes);
  }

  public static Instruction revokeMultisig(final SolanaAccounts solanaAccounts,
                                           final PublicKey sourceAccount,
                                           final PublicKey owner,
                                           final List<PublicKey> signerAccounts) {
    return revokeMultisig(solanaAccounts.invokedTokenProgram(), sourceAccount, owner, signerAccounts);
  }

  private static byte[] setAuthorityData(final AuthorityType authorityType, final PublicKey newAuthority) {
    final byte[] data;
    if (newAuthority == null) {
      data = new byte[3];
    } else {
      data = new byte[3 + PUBLIC_KEY_LENGTH];
      data[2] = 1;
      newAuthority.write(data, 3);
    }

    data[0] = TokenInstruction.SetAuthority.discriminator;
    data[1] = (byte) authorityType.ordinal();

    return data;
  }

  public static Instruction setAuthority(final AccountMeta invokedTokenProgram,
                                         final PublicKey account,
                                         final PublicKey authority,
                                         final AuthorityType authorityType,
                                         final PublicKey newAuthority) {
    final var keys = List.of(
        createWrite(account),
        createReadOnlySigner(authority)
    );

    final byte[] data = setAuthorityData(authorityType, newAuthority);

    return createInstruction(invokedTokenProgram, keys, data);
  }

  public static Instruction setAuthority(final SolanaAccounts solanaAccounts,
                                         final PublicKey account,
                                         final PublicKey authority,
                                         final AuthorityType authorityType,
                                         final PublicKey newAuthority) {
    return setAuthority(solanaAccounts.invokedTokenProgram(), account, authority, authorityType, newAuthority);
  }

  public static Instruction setAuthorityMultisig(final AccountMeta invokedTokenProgram,
                                                 final PublicKey account,
                                                 final PublicKey owner,
                                                 final List<PublicKey> signerAccounts,
                                                 final AuthorityType authorityType,
                                                 final PublicKey newAuthority) {
    final var keys = initSigners(2, signerAccounts);
    keys[0] = createWrite(account);
    keys[1] = createRead(owner);

    final byte[] data = setAuthorityData(authorityType, newAuthority);

    return createInstruction(invokedTokenProgram, Arrays.asList(keys), data);
  }

  public static Instruction setAuthorityMultisig(final SolanaAccounts solanaAccounts,
                                                 final PublicKey account,
                                                 final PublicKey owner,
                                                 final List<PublicKey> signerAccounts,
                                                 final AuthorityType authorityType,
                                                 final PublicKey newAuthority) {
    return setAuthorityMultisig(solanaAccounts.invokedTokenProgram(), account, owner, signerAccounts, authorityType, newAuthority);
  }

  private static byte[] mintToData(final long amount) {
    return amountData(TokenInstruction.MintTo, amount);
  }

  private static List<AccountMeta> mintToKeys(final PublicKey mint,
                                              final PublicKey account,
                                              final PublicKey authority) {
    return List.of(
        createWrite(mint),
        createWrite(account),
        createReadOnlySigner(authority)
    );
  }

  public static Instruction mintTo(final AccountMeta invokedTokenProgram,
                                   final PublicKey mint,
                                   final PublicKey account,
                                   final PublicKey authority,
                                   final long amount) {
    final var keys = mintToKeys(mint, account, authority);
    final byte[] data = mintToData(amount);
    return createInstruction(invokedTokenProgram, keys, data);
  }

  public static Instruction mintTo(final SolanaAccounts solanaAccounts,
                                   final PublicKey mint,
                                   final PublicKey account,
                                   final PublicKey authority,
                                   final long amount) {
    return mintTo(solanaAccounts.invokedTokenProgram(), mint, account, authority, amount);
  }

  private static List<AccountMeta> mintToMultisigKeys(final PublicKey mint,
                                                      final PublicKey account,
                                                      final PublicKey authority,
                                                      final List<PublicKey> signerAccounts) {
    final var keys = initSigners(3, signerAccounts);
    keys[0] = createWrite(mint);
    keys[1] = createWrite(account);
    keys[2] = createRead(authority);
    return Arrays.asList(keys);
  }

  public static Instruction mintToMultisig(final AccountMeta invokedTokenProgram,
                                           final PublicKey mint,
                                           final PublicKey account,
                                           final PublicKey authority,
                                           final List<PublicKey> signerAccounts,
                                           final long amount) {
    final var keys = mintToMultisigKeys(mint, account, authority, signerAccounts);
    final byte[] data = mintToData(amount);
    return createInstruction(invokedTokenProgram, keys, data);
  }

  public static Instruction mintToMultisig(final SolanaAccounts solanaAccounts,
                                           final PublicKey mint,
                                           final PublicKey account,
                                           final PublicKey authority,
                                           final List<PublicKey> signerAccounts,
                                           final long amount) {
    return mintToMultisig(
        solanaAccounts.invokedTokenProgram(),
        mint,
        account,
        authority,
        signerAccounts,
        amount
    );
  }

  private static byte[] mintToCheckedData(final long amount, final int decimals) {
    return checkedAmountData(TokenInstruction.MintToChecked, amount, decimals);
  }

  public static Instruction mintToChecked(final AccountMeta invokedTokenProgram,
                                          final PublicKey mint,
                                          final int decimals,
                                          final PublicKey account,
                                          final PublicKey authority,
                                          final long amount) {
    final var keys = mintToKeys(mint, account, authority);
    final byte[] data = mintToCheckedData(amount, decimals);
    return createInstruction(invokedTokenProgram, keys, data);
  }

  public static Instruction mintToChecked(final SolanaAccounts solanaAccounts,
                                          final PublicKey mint,
                                          final int decimals,
                                          final PublicKey account,
                                          final PublicKey authority,
                                          final long amount) {
    return mintToChecked(
        solanaAccounts.invokedTokenProgram(),
        mint,
        decimals,
        account,
        authority,
        amount
    );
  }

  public static Instruction mintToCheckedMultisig(final AccountMeta invokedTokenProgram,
                                                  final PublicKey mint,
                                                  final int decimals,
                                                  final PublicKey account,
                                                  final PublicKey authority,
                                                  final List<PublicKey> signerAccounts,
                                                  final long amount) {
    final var keys = mintToMultisigKeys(mint, account, authority, signerAccounts);
    final byte[] data = mintToCheckedData(amount, decimals);
    return createInstruction(invokedTokenProgram, keys, data);
  }

  public static Instruction mintToCheckedMultisig(final SolanaAccounts solanaAccounts,
                                                  final PublicKey mint,
                                                  final int decimals,
                                                  final PublicKey account,
                                                  final PublicKey authority,
                                                  final List<PublicKey> signerAccounts,
                                                  final long amount) {
    return mintToCheckedMultisig(
        solanaAccounts.invokedTokenProgram(),
        mint,
        decimals,
        account,
        authority,
        signerAccounts,
        amount
    );
  }

  private static byte[] burnData(final long amount) {
    return amountData(TokenInstruction.Burn, amount);
  }

  private static List<AccountMeta> burnKeys(final PublicKey mint,
                                            final PublicKey account,
                                            final PublicKey authority) {
    return List.of(
        createWrite(account),
        createWrite(mint),
        createReadOnlySigner(authority)
    );
  }

  public static Instruction burn(final AccountMeta invokedTokenProgram,
                                 final PublicKey mint,
                                 final PublicKey account,
                                 final PublicKey authority,
                                 final long amount) {
    final var keys = burnKeys(mint, account, authority);
    final byte[] data = burnData(amount);
    return createInstruction(invokedTokenProgram, keys, data);
  }

  public static Instruction burn(final SolanaAccounts solanaAccounts,
                                 final PublicKey mint,
                                 final PublicKey account,
                                 final PublicKey authority,
                                 final long amount) {
    return burn(solanaAccounts.invokedTokenProgram(), mint, account, authority, amount);
  }

  private static List<AccountMeta> burnMultisigKeys(final PublicKey mint,
                                                    final PublicKey account,
                                                    final PublicKey authority,
                                                    final List<PublicKey> signerAccounts) {
    final var keys = initSigners(3, signerAccounts);
    keys[0] = createWrite(account);
    keys[1] = createWrite(mint);
    keys[2] = createRead(authority);
    return Arrays.asList(keys);
  }

  public static Instruction burnMultisig(final AccountMeta invokedTokenProgram,
                                         final PublicKey mint,
                                         final PublicKey account,
                                         final PublicKey authority,
                                         final List<PublicKey> signerAccounts,
                                         final long amount) {
    final var keys = burnMultisigKeys(mint, account, authority, signerAccounts);
    final byte[] data = burnData(amount);
    return createInstruction(invokedTokenProgram, keys, data);
  }

  public static Instruction burnMultisig(final SolanaAccounts solanaAccounts,
                                         final PublicKey mint,
                                         final PublicKey account,
                                         final PublicKey authority,
                                         final List<PublicKey> signerAccounts,
                                         final long amount) {
    return burnMultisig(
        solanaAccounts.invokedTokenProgram(),
        mint,
        account,
        authority,
        signerAccounts,
        amount
    );
  }

  private static byte[] burnCheckedData(final long amount, final int decimals) {
    return checkedAmountData(TokenInstruction.BurnChecked, amount, decimals);
  }

  public static Instruction burnChecked(final AccountMeta invokedTokenProgram,
                                        final PublicKey mint,
                                        final int decimals,
                                        final PublicKey account,
                                        final PublicKey authority,
                                        final long amount) {
    final var keys = burnKeys(mint, account, authority);
    final byte[] data = burnCheckedData(amount, decimals);
    return createInstruction(invokedTokenProgram, keys, data);
  }

  public static Instruction burnChecked(final SolanaAccounts solanaAccounts,
                                        final PublicKey mint,
                                        final int decimals,
                                        final PublicKey account,
                                        final PublicKey authority,
                                        final long amount) {
    return burnChecked(
        solanaAccounts.invokedTokenProgram(),
        mint,
        decimals,
        account,
        authority,
        amount
    );
  }

  public static Instruction burnCheckedMultisig(final AccountMeta invokedTokenProgram,
                                                final PublicKey mint,
                                                final int decimals,
                                                final PublicKey account,
                                                final PublicKey authority,
                                                final List<PublicKey> signerAccounts,
                                                final long amount) {
    final var keys = burnMultisigKeys(mint, account, authority, signerAccounts);
    final byte[] data = burnCheckedData(amount, decimals);
    return createInstruction(invokedTokenProgram, keys, data);
  }

  public static Instruction burnCheckedMultisig(final SolanaAccounts solanaAccounts,
                                                final PublicKey mint,
                                                final int decimals,
                                                final PublicKey account,
                                                final PublicKey authority,
                                                final List<PublicKey> signerAccounts,
                                                final long amount) {
    return burnCheckedMultisig(
        solanaAccounts.invokedTokenProgram(),
        mint,
        decimals,
        account,
        authority,
        signerAccounts,
        amount
    );
  }

  public static Instruction closeAccount(final AccountMeta invokedProgram,
                                         final PublicKey tokenAccount,
                                         final PublicKey lamportDestination,
                                         final PublicKey owner) {
    final var keys = List.of(
        createWrite(tokenAccount),
        createWrite(lamportDestination),
        createReadOnlySigner(owner)
    );
    return createInstruction(invokedProgram, keys, TokenInstruction.CloseAccount.discriminatorBytes);
  }

  public static Instruction closeAccount(final SolanaAccounts solanaAccounts,
                                         final PublicKey tokenAccount,
                                         final PublicKey lamportDestination,
                                         final PublicKey owner) {
    return closeAccount(solanaAccounts.invokedSystemProgram(), tokenAccount, lamportDestination, owner);
  }

  public static Instruction closeAccountMultisig(final AccountMeta invokedProgram,
                                                 final PublicKey tokenAccount,
                                                 final PublicKey lamportDestination,
                                                 final PublicKey owner,
                                                 final List<PublicKey> signerAccounts) {
    final var keys = initSigners(3, signerAccounts);
    keys[0] = createWrite(tokenAccount);
    keys[1] = createWrite(lamportDestination);
    keys[2] = createRead(owner);
    return createInstruction(invokedProgram, Arrays.asList(keys), TokenInstruction.CloseAccount.discriminatorBytes);
  }

  public static Instruction closeAccountMultisig(final SolanaAccounts solanaAccounts,
                                                 final PublicKey tokenAccount,
                                                 final PublicKey lamportDestination,
                                                 final PublicKey owner,
                                                 final List<PublicKey> signerAccounts) {
    return closeAccountMultisig(
        solanaAccounts.invokedSystemProgram(),
        tokenAccount,
        lamportDestination,
        owner,
        signerAccounts
    );
  }

  public static Instruction freezeAccount(final AccountMeta invokedProgram,
                                          final PublicKey account,
                                          final PublicKey mint,
                                          final PublicKey freezeAuthority) {
    final var keys = List.of(
        createWrite(account),
        createRead(mint),
        createReadOnlySigner(freezeAuthority)
    );
    return createInstruction(invokedProgram, keys, TokenInstruction.FreezeAccount.discriminatorBytes);
  }

  public static Instruction freezeAccount(final SolanaAccounts solanaAccounts,
                                          final PublicKey account,
                                          final PublicKey mint,
                                          final PublicKey freezeAuthority) {
    return freezeAccount(solanaAccounts.invokedSystemProgram(), account, mint, freezeAuthority);
  }

  public static Instruction freezeAccountMultisig(final AccountMeta invokedProgram,
                                                  final PublicKey account,
                                                  final PublicKey mint,
                                                  final PublicKey freezeAuthority,
                                                  final List<PublicKey> signerAccounts) {
    final var keys = initSigners(3, signerAccounts);
    keys[0] = createWrite(account);
    keys[1] = createRead(mint);
    keys[2] = createRead(freezeAuthority);
    return createInstruction(invokedProgram, Arrays.asList(keys), TokenInstruction.FreezeAccount.discriminatorBytes);
  }

  public static Instruction freezeAccountMultisig(final SolanaAccounts solanaAccounts,
                                                  final PublicKey account,
                                                  final PublicKey mint,
                                                  final PublicKey freezeAuthority,
                                                  final List<PublicKey> signerAccounts) {
    return freezeAccountMultisig(
        solanaAccounts.invokedTokenProgram(),
        account,
        mint,
        freezeAuthority,
        signerAccounts
    );
  }

  public static Instruction thawAccount(final AccountMeta invokedProgram,
                                        final PublicKey account,
                                        final PublicKey mint,
                                        final PublicKey authority) {
    final var keys = List.of(
        createWrite(account),
        createRead(mint),
        createReadOnlySigner(authority)
    );
    return createInstruction(invokedProgram, keys, TokenInstruction.ThawAccount.discriminatorBytes);
  }

  public static Instruction thawAccount(final SolanaAccounts solanaAccounts,
                                        final PublicKey account,
                                        final PublicKey mint,
                                        final PublicKey authority) {
    return thawAccount(solanaAccounts.invokedSystemProgram(), account, mint, authority);
  }

  public static Instruction thawAccountMultisig(final AccountMeta invokedProgram,
                                                final PublicKey account,
                                                final PublicKey mint,
                                                final PublicKey authority,
                                                final List<PublicKey> signerAccounts) {
    final var keys = initSigners(3, signerAccounts);
    keys[0] = createWrite(account);
    keys[1] = createRead(mint);
    keys[2] = createRead(authority);
    return createInstruction(invokedProgram, Arrays.asList(keys), TokenInstruction.ThawAccount.discriminatorBytes);
  }

  public static Instruction thawAccountMultisig(final SolanaAccounts solanaAccounts,
                                                final PublicKey account,
                                                final PublicKey mint,
                                                final PublicKey authority,
                                                final List<PublicKey> signerAccounts) {
    return thawAccountMultisig(
        solanaAccounts.invokedTokenProgram(),
        account,
        mint,
        authority,
        signerAccounts
    );
  }

  public static Instruction syncNative(final AccountMeta invokedProgram, final PublicKey solTokenAccount) {
    final var keys = List.of(createWrite(solTokenAccount));
    return createInstruction(invokedProgram, keys, TokenInstruction.SyncNative.discriminatorBytes);
  }

  public static Instruction initializeImmutableOwner(final AccountMeta invokedProgram, final PublicKey account) {
    final var keys = List.of(createWrite(account));
    return createInstruction(invokedProgram, keys, TokenInstruction.InitializeImmutableOwner.discriminatorBytes);
  }

  private TokenProgram() {
  }
}
