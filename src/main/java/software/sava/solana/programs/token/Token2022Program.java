package software.sava.solana.programs.token;

import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.SolanaAccounts;
import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.accounts.token.extensions.ExtensionType;
import software.sava.core.encoding.ByteUtil;
import software.sava.core.programs.Discriminator;
import software.sava.core.tx.Instruction;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static software.sava.core.accounts.PublicKey.PUBLIC_KEY_LENGTH;
import static software.sava.core.accounts.meta.AccountMeta.*;
import static software.sava.core.tx.Instruction.createInstruction;
import static software.sava.solana.programs.token.TokenProgram.initSigners;

// https://github.com/solana-program/token-2022
public final class Token2022Program {

  public enum TokenInstruction implements Discriminator {

    /// Initializes a new mint and optionally deposits all the newly minted
    /// tokens in an account.
    ///
    /// The `InitializeMint` instruction requires no signers and MUST be
    /// included within the same Transaction as the system program's
    /// `CreateAccount` instruction that creates the account being initialized.
    /// Otherwise another party can acquire ownership of the uninitialized
    /// account.
    ///
    /// All extensions must be initialized before calling this instruction.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   0. `[writable]` The mint to initialize.
    ///   1. `[]` Rent sysvar
    InitializeMint {
      /// Number of base 10 digits to the right of the decimal place.
//    decimals: u8,
//    /// The authority/multisignature to mint tokens.
//        #[cfg_attr(feature = "serde-traits", serde(with = "As::<DisplayFromStr>"))]
//    mint_authority: Pubkey,
//    /// The freeze authority/multisignature of the mint.
//        #[cfg_attr(feature = "serde-traits", serde(with = "coption_fromstr"))]
//    freeze_authority: COption<Pubkey>,
    },

    /// Initializes a new account to hold tokens.  If this account is associated
    /// with the native mint then the token balance of the initialized account
    /// will be equal to the amount of SOL in the account. If this account is
    /// associated with another mint, that mint must be initialized before this
    /// command can succeed.
    ///
    /// The `InitializeAccount` instruction requires no signers and MUST be
    /// included within the same Transaction as the system program's
    /// `CreateAccount` instruction that creates the account being initialized.
    /// Otherwise another party can acquire ownership of the uninitialized
    /// account.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   0. `[writable]`  The account to initialize.
    ///   1. `[]` The mint this account will be associated with.
    ///   2. `[]` The new account's owner/multisignature.
    ///   3. `[]` Rent sysvar
    InitializeAccount,

    /// Initializes a multisignature account with N provided signers.
    ///
    /// Multisignature accounts can used in place of any single owner/delegate
    /// accounts in any token instruction that require an owner/delegate to be
    /// present.  The variant field represents the number of signers (M)
    /// required to validate this multisignature account.
    ///
    /// The `InitializeMultisig` instruction requires no signers and MUST be
    /// included within the same Transaction as the system program's
    /// `CreateAccount` instruction that creates the account being initialized.
    /// Otherwise another party can acquire ownership of the uninitialized
    /// account.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   0. `[writable]` The multisignature account to initialize.
    ///   1. `[]` Rent sysvar
    ///   2. ..`2+N`. `[]` The signer accounts, must equal to N where `1 <= N <=
    ///      11`.
    InitializeMultisig {
      /// The number of signers (M) required to validate this multisignature
      /// account.
//    m: u8,
    },

    /// NOTE This instruction is deprecated in favor of `TransferChecked` or
    /// `TransferCheckedWithFee`
    ///
    /// Transfers tokens from one account to another either directly or via a
    /// delegate.  If this account is associated with the native mint then equal
    /// amounts of SOL and Tokens will be transferred to the destination
    /// account.
    ///
    /// If either account contains an `TransferFeeAmount` extension, this will
    /// fail. Mints with the `TransferFeeConfig` extension are required in
    /// order to assess the fee.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   * Single owner/delegate
    ///   0. `[writable]` The source account.
    ///   1. `[writable]` The destination account.
    ///   2. `[signer]` The source account's owner/delegate.
    ///
    ///   * Multisignature owner/delegate
    ///   0. `[writable]` The source account.
    ///   1. `[writable]` The destination account.
    ///   2. `[]` The source account's multisignature owner/delegate.
    ///   3. ..`3+M` `[signer]` M signer accounts.
//      #[
//
//    deprecated(
//        since ="4.0.0",
//        note ="please use `TransferChecked` or `TransferCheckedWithFee` instead"
//    )]
    Transfer {
      /// The amount of tokens to transfer.
//      amount:
//      u64,
    },

    /// Approves a delegate.  A delegate is given the authority over tokens on
    /// behalf of the source account's owner.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   * Single owner
    ///   0. `[writable]` The source account.
    ///   1. `[]` The delegate.
    ///   2. `[signer]` The source account owner.
    ///
    ///   * Multisignature owner
    ///   0. `[writable]` The source account.
    ///   1. `[]` The delegate.
    ///   2. `[]` The source account's multisignature owner.
    ///   3. ..`3+M` `[signer]` M signer accounts
    Approve {
      /// The amount of tokens the delegate is approved for.
//      amount:
//      u64,
    },

    /// Revokes the delegate's authority.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   * Single owner
    ///   0. `[writable]` The source account.
    ///   1. `[signer]` The source account owner or current delegate.
    ///
    ///   * Multisignature owner
    ///   0. `[writable]` The source account.
    ///   1. `[]` The source account's multisignature owner or current delegate.
    ///   2. ..`2+M` `[signer]` M signer accounts
    Revoke,

    /// Sets a new authority of a mint or account.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   * Single authority
    ///   0. `[writable]` The mint or account to change the authority of.
    ///   1. `[signer]` The current authority of the mint or account.
    ///
    ///   * Multisignature authority
    ///   0. `[writable]` The mint or account to change the authority of.
    ///   1. `[]` The mint's or account's current multisignature authority.
    ///   2. ..`2+M` `[signer]` M signer accounts
    SetAuthority {
      /// The type of authority to update.
//      authority_type:
//      AuthorityType,
//      /// The new authority
//        #[cfg_attr(feature = "serde-traits", serde(with = "coption_fromstr"))]
//      new_authority:
//      COption<Pubkey>,
    },

    /// Mints new tokens to an account.  The native mint does not support
    /// minting.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   * Single authority
    ///   0. `[writable]` The mint.
    ///   1. `[writable]` The account to mint tokens to.
    ///   2. `[signer]` The mint's minting authority.
    ///
    ///   * Multisignature authority
    ///   0. `[writable]` The mint.
    ///   1. `[writable]` The account to mint tokens to.
    ///   2. `[]` The mint's multisignature mint-tokens authority.
    ///   3. ..`3+M` `[signer]` M signer accounts.
    MintTo {
      /// The amount of new tokens to mint.
//      amount:
//      u64,
    },

    /// Burns tokens by removing them from an account.  `Burn` does not support
    /// accounts associated with the native mint, use `CloseAccount` instead.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   * Single owner/delegate
    ///   0. `[writable]` The account to burn from.
    ///   1. `[writable]` The token mint.
    ///   2. `[signer]` The account's owner/delegate.
    ///
    ///   * Multisignature owner/delegate
    ///   0. `[writable]` The account to burn from.
    ///   1. `[writable]` The token mint.
    ///   2. `[]` The account's multisignature owner/delegate.
    ///   3. ..`3+M` `[signer]` M signer accounts.
    Burn {
      /// The amount of tokens to burn.
//      amount:
//      u64,
    },

    /// Close an account by transferring all its SOL to the destination account.
    /// Non-native accounts may only be closed if its token amount is zero.
    ///
    /// Accounts with the `TransferFeeAmount` extension may only be closed if
    /// the withheld amount is zero.
    ///
    /// Accounts with the `ConfidentialTransfer` extension may only be closed if
    /// the pending and available balance ciphertexts are empty. Use
    /// `ConfidentialTransferInstruction::ApplyPendingBalance` and
    /// `ConfidentialTransferInstruction::EmptyAccount` to empty these
    /// ciphertexts.
    ///
    /// Accounts with the `ConfidentialTransferFee` extension may only be closed
    /// if the withheld amount ciphertext is empty. Use
    /// `ConfidentialTransferFeeInstruction::HarvestWithheldTokensToMint` to
    /// empty this ciphertext.
    ///
    /// Mints may be closed if they have the `MintCloseAuthority` extension and
    /// their token supply is zero
    ///
    /// Accounts
    ///
    /// Accounts expected by this instruction:
    ///
    ///   * Single owner
    ///   0. `[writable]` The account to close.
    ///   1. `[writable]` The destination account.
    ///   2. `[signer]` The account's owner.
    ///
    ///   * Multisignature owner
    ///   0. `[writable]` The account to close.
    ///   1. `[writable]` The destination account.
    ///   2. `[]` The account's multisignature owner.
    ///   3. ..`3+M` `[signer]` M signer accounts.
    CloseAccount,

    /// Freeze an Initialized account using the Mint's `freeze_authority` (if
    /// set).
    ///
    /// Accounts expected by this instruction:
    ///
    ///   * Single owner
    ///   0. `[writable]` The account to freeze.
    ///   1. `[]` The token mint.
    ///   2. `[signer]` The mint freeze authority.
    ///
    ///   * Multisignature owner
    ///   0. `[writable]` The account to freeze.
    ///   1. `[]` The token mint.
    ///   2. `[]` The mint's multisignature freeze authority.
    ///   3. ..`3+M` `[signer]` M signer accounts.
    FreezeAccount,

    /// Thaw a Frozen account using the Mint's `freeze_authority` (if set).
    ///
    /// Accounts expected by this instruction:
    ///
    ///   * Single owner
    ///   0. `[writable]` The account to freeze.
    ///   1. `[]` The token mint.
    ///   2. `[signer]` The mint freeze authority.
    ///
    ///   * Multisignature owner
    ///   0. `[writable]` The account to freeze.
    ///   1. `[]` The token mint.
    ///   2. `[]` The mint's multisignature freeze authority.
    ///   3. ..`3+M` `[signer]` M signer accounts.
    ThawAccount,

    /// Transfers tokens from one account to another either directly or via a
    /// delegate.  If this account is associated with the native mint then equal
    /// amounts of SOL and Tokens will be transferred to the destination
    /// account.
    ///
    /// This instruction differs from `Transfer` in that the token mint and
    /// decimals value is checked by the caller.  This may be useful when
    /// creating transactions offline or within a hardware wallet.
    ///
    /// If either account contains an `TransferFeeAmount` extension, the fee is
    /// withheld in the destination account.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   * Single owner/delegate
    ///   0. `[writable]` The source account.
    ///   1. `[]` The token mint.
    ///   2. `[writable]` The destination account.
    ///   3. `[signer]` The source account's owner/delegate.
    ///
    ///   * Multisignature owner/delegate
    ///   0. `[writable]` The source account.
    ///   1. `[]` The token mint.
    ///   2. `[writable]` The destination account.
    ///   3. `[]` The source account's multisignature owner/delegate.
    ///   4. ..`4+M` `[signer]` M signer accounts.
    TransferChecked {
      /// The amount of tokens to transfer.
//      amount:
//      u64,
//          /// Expected number of base 10 digits to the right of the decimal place.
//          decimals:u8,
    },

    /// Approves a delegate.  A delegate is given the authority over tokens on
    /// behalf of the source account's owner.
    ///
    /// This instruction differs from `Approve` in that the token mint and
    /// decimals value is checked by the caller.  This may be useful when
    /// creating transactions offline or within a hardware wallet.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   * Single owner
    ///   0. `[writable]` The source account.
    ///   1. `[]` The token mint.
    ///   2. `[]` The delegate.
    ///   3. `[signer]` The source account owner.
    ///
    ///   * Multisignature owner
    ///   0. `[writable]` The source account.
    ///   1. `[]` The token mint.
    ///   2. `[]` The delegate.
    ///   3. `[]` The source account's multisignature owner.
    ///   4. ..`4+M` `[signer]` M signer accounts
    ApproveChecked {
      /// The amount of tokens the delegate is approved for.
//      amount:
//      u64,
//          /// Expected number of base 10 digits to the right of the decimal place.
//          decimals:u8,
    },

    /// Mints new tokens to an account.  The native mint does not support
    /// minting.
    ///
    /// This instruction differs from `MintTo` in that the decimals value is
    /// checked by the caller.  This may be useful when creating transactions
    /// offline or within a hardware wallet.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   * Single authority
    ///   0. `[writable]` The mint.
    ///   1. `[writable]` The account to mint tokens to.
    ///   2. `[signer]` The mint's minting authority.
    ///
    ///   * Multisignature authority
    ///   0. `[writable]` The mint.
    ///   1. `[writable]` The account to mint tokens to.
    ///   2. `[]` The mint's multisignature mint-tokens authority.
    ///   3. ..`3+M` `[signer]` M signer accounts.
    MintToChecked {
      /// The amount of new tokens to mint.
//      amount:
//      u64,
//          /// Expected number of base 10 digits to the right of the decimal place.
//          decimals:u8,
    },

    /// Burns tokens by removing them from an account.  `BurnChecked` does not
    /// support accounts associated with the native mint, use `CloseAccount`
    /// instead.
    ///
    /// This instruction differs from `Burn` in that the decimals value is
    /// checked by the caller. This may be useful when creating transactions
    /// offline or within a hardware wallet.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   * Single owner/delegate
    ///   0. `[writable]` The account to burn from.
    ///   1. `[writable]` The token mint.
    ///   2. `[signer]` The account's owner/delegate.
    ///
    ///   * Multisignature owner/delegate
    ///   0. `[writable]` The account to burn from.
    ///   1. `[writable]` The token mint.
    ///   2. `[]` The account's multisignature owner/delegate.
    ///   3. ..`3+M` `[signer]` M signer accounts.
    BurnChecked {
      /// The amount of tokens to burn.
//      amount:
//      u64,
//          /// Expected number of base 10 digits to the right of the decimal place.
//          decimals:u8,
    },

    /// Like `InitializeAccount`, but the owner pubkey is passed via instruction
    /// data rather than the accounts list. This variant may be preferable
    /// when using Cross Program Invocation from an instruction that does
    /// not need the owner's `AccountInfo` otherwise.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   0. `[writable]`  The account to initialize.
    ///   1. `[]` The mint this account will be associated with.
    ///   2. `[]` Rent sysvar
    InitializeAccount2 {
      /// The new account's owner/multisignature.
//        #[cfg_attr(feature = "serde-traits", serde(with = "As::<DisplayFromStr>"))]
//      owner:
//      Pubkey,
    },
    /// Given a wrapped / native token account (a token account containing SOL)
    /// updates its amount field based on the account's underlying `lamports`.
    /// This is useful if a non-wrapped SOL account uses
    /// `system_instruction::transfer` to move lamports to a wrapped token
    /// account, and needs to have its token `amount` field updated.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   0. `[writable]`  The native token account to sync with its underlying
    ///      lamports.
    SyncNative,

    /// Like `InitializeAccount2`, but does not require the Rent sysvar to be
    /// provided
    ///
    /// Accounts expected by this instruction:
    ///
    ///   0. `[writable]`  The account to initialize.
    ///   1. `[]` The mint this account will be associated with.
    InitializeAccount3 {
      /// The new account's owner/multisignature.
//        #[cfg_attr(feature = "serde-traits", serde(with = "As::<DisplayFromStr>"))]
//      owner:
//      Pubkey,
    },

    /// Like `InitializeMultisig`, but does not require the Rent sysvar to be
    /// provided
    ///
    /// Accounts expected by this instruction:
    ///
    ///   0. `[writable]` The multisignature account to initialize.
    ///   1. ..`1+N`. `[]` The signer accounts, must equal to N where `1 <= N <=
    ///      11`.
    InitializeMultisig2 {
      /// The number of signers (M) required to validate this multisignature
      /// account.
//      m:
//      u8,
    },

    /// Like `InitializeMint`, but does not require the Rent sysvar to be
    /// provided
    ///
    /// Accounts expected by this instruction:
    ///
    ///   0. `[writable]` The mint to initialize.
    InitializeMint2 {
      /// Number of base 10 digits to the right of the decimal place.
//      decimals:
//      u8,
//      /// The authority/multisignature to mint tokens.
//        #[cfg_attr(feature = "serde-traits", serde(with = "As::<DisplayFromStr>"))]
//      mint_authority:
//      Pubkey,
//      /// The freeze authority/multisignature of the mint.
//        #[cfg_attr(feature = "serde-traits", serde(with = "coption_fromstr"))]
//      freeze_authority:
//      COption<Pubkey>,
    },

    /// Gets the required size of an account for the given mint as a
    /// little-endian `u64`.
    ///
    /// Return data can be fetched using `sol_get_return_data` and deserializing
    /// the return data as a little-endian `u64`.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   0. `[]` The mint to calculate for
    GetAccountDataSize {
      /// Additional extension types to include in the returned account size
//      extension_types:
//      Vec<ExtensionType>,
    },
    /// Initialize the Immutable Owner extension for the given token account
    ///
    /// Fails if the account has already been initialized, so must be called
    /// before `InitializeAccount`.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   0. `[writable]`  The account to initialize.
    ///
    /// Data expected by this instruction:
    ///   None
    InitializeImmutableOwner,

    /// Convert an Amount of tokens to a `UiAmount` string, using the given
    /// mint.
    ///
    /// Fails on an invalid mint.
    ///
    /// Return data can be fetched using `sol_get_return_data` and deserialized
    /// with `String::from_utf8`.
    ///
    /// WARNING: For mints using the interest-bearing or scaled-ui-amount
    /// extensions, this instruction uses standard floating-point arithmetic to
    /// convert values, which is not guaranteed to give consistent behavior.
    ///
    /// In particular, conversions will not always work in reverse. For example,
    /// if you pass amount `A` to `AmountToUiAmount` and receive `B`, and pass
    /// the result `B` to `UiAmountToAmount`, you will not always get back `A`.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   0. `[]` The mint to calculate for
    AmountToUiAmount {
      /// The amount of tokens to convert.
//      amount:
//      u64,
    },

    /// Convert a `UiAmount` of tokens to a little-endian `u64` raw Amount,
    /// using the given mint.
    ///
    /// Return data can be fetched using `sol_get_return_data` and deserializing
    /// the return data as a little-endian `u64`.
    ///
    /// WARNING: For mints using the interest-bearing or scaled-ui-amount
    /// extensions, this instruction uses standard floating-point arithmetic to
    /// convert values, which is not guaranteed to give consistent behavior.
    ///
    /// In particular, conversions will not always work in reverse. For example,
    /// if you pass amount `A` to `UiAmountToAmount` and receive `B`, and pass
    /// the result `B` to `AmountToUiAmount`, you will not always get back `A`.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   0. `[]` The mint to calculate for
    UiAmountToAmount {
      /// The `ui_amount` of tokens to convert.
//      ui_amount: &'a str,
    },

    /// Initialize the close account authority on a new mint.
    ///
    /// Fails if the mint has already been initialized, so must be called before
    /// `InitializeMint`.
    ///
    /// The mint must have exactly enough space allocated for the base mint (82
    /// bytes), plus 83 bytes of padding, 1 byte reserved for the account type,
    /// then space required for this extension, plus any others.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   0. `[writable]` The mint to initialize.
    InitializeMintCloseAuthority {
      /// Authority that must sign the `CloseAccount` instruction on a mint
//        #[cfg_attr(feature = "serde-traits", serde(with = "coption_fromstr"))]
//      close_authority:
//      COption<Pubkey>,
    },

    /// The common instruction prefix for Transfer Fee extension instructions.
    ///
    /// See `extension::transfer_fee::instruction::TransferFeeInstruction` for
    /// further details about the extended instructions that share this
    /// instruction prefix
    TransferFeeExtension,

    /// The common instruction prefix for Confidential Transfer extension
    /// instructions.
    ///
    /// See `extension::confidential_transfer::instruction::ConfidentialTransferInstruction` for
    /// further details about the extended instructions that share this
    /// instruction prefix
    ConfidentialTransferExtension,

    /// The common instruction prefix for Default Account State extension
    /// instructions.
    ///
    /// See `extension::default_account_state::instruction::DefaultAccountStateInstruction` for
    /// further details about the extended instructions that share this
    /// instruction prefix
    DefaultAccountStateExtension,

    /// Check to see if a token account is large enough for a list of
    /// `ExtensionTypes`, and if not, use reallocation to increase the data
    /// size.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   * Single owner
    ///   0. `[writable]` The account to reallocate.
    ///   1. `[signer, writable]` The payer account to fund reallocation
    ///   2. `[]` System program for reallocation funding
    ///   3. `[signer]` The account's owner.
    ///
    ///   * Multisignature owner
    ///   0. `[writable]` The account to reallocate.
    ///   1. `[signer, writable]` The payer account to fund reallocation
    ///   2. `[]` System program for reallocation funding
    ///   3. `[]` The account's multisignature owner/delegate.
    ///   4. ..`4+M` `[signer]` M signer accounts.
    Reallocate {
      /// New extension types to include in the reallocated account
//      extension_types:
//      Vec<ExtensionType>,
    },

    /// The common instruction prefix for Memo Transfer account extension
    /// instructions.
    ///
    /// See `extension::memo_transfer::instruction::RequiredMemoTransfersInstruction` for
    /// further details about the extended instructions that share this
    /// instruction prefix
    MemoTransferExtension,

    /// Creates the native mint.
    ///
    /// This instruction only needs to be invoked once after deployment and is
    /// permissionless, Wrapped SOL (`native_mint::id()`) will not be
    /// available until this instruction is successfully executed.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   0. `[writeable,signer]` Funding account (must be a system account)
    ///   1. `[writable]` The native mint address
    ///   2. `[]` System program for mint account funding
    CreateNativeMint,

    /// Initialize the non transferable extension for the given mint account
    ///
    /// Fails if the account has already been initialized, so must be called
    /// before `InitializeMint`.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   0. `[writable]`  The mint account to initialize.
    ///
    /// Data expected by this instruction:
    ///   None
    InitializeNonTransferableMint,

    /// The common instruction prefix for Interest Bearing extension
    /// instructions.
    ///
    /// See `extension::interest_bearing_mint::instruction::InterestBearingMintInstruction` for
    /// further details about the extended instructions that share this
    /// instruction prefix
    InterestBearingMintExtension,

    /// The common instruction prefix for CPI Guard account extension
    /// instructions.
    ///
    /// See `extension::cpi_guard::instruction::CpiGuardInstruction` for
    /// further details about the extended instructions that share this
    /// instruction prefix
    CpiGuardExtension,

    /// Initialize the permanent delegate on a new mint.
    ///
    /// Fails if the mint has already been initialized, so must be called before
    /// `InitializeMint`.
    ///
    /// The mint must have exactly enough space allocated for the base mint (82
    /// bytes), plus 83 bytes of padding, 1 byte reserved for the account type,
    /// then space required for this extension, plus any others.
    ///
    /// Accounts expected by this instruction:
    ///
    ///   0. `[writable]` The mint to initialize.
    ///
    /// Data expected by this instruction:
    ///   Pubkey for the permanent delegate
    InitializePermanentDelegate {
      /// Authority that may sign for `Transfer`s and `Burn`s on any account
//        #[cfg_attr(feature = "serde-traits", serde(with = "As::<DisplayFromStr>"))]
//      delegate:
//      Pubkey,
    },

    /// The common instruction prefix for transfer hook extension instructions.
    ///
    /// See `extension::transfer_hook::instruction::TransferHookInstruction`
    /// for further details about the extended instructions that share this
    /// instruction prefix
    TransferHookExtension,

    /// The common instruction prefix for the confidential transfer fee
    /// extension instructions.
    ///
    /// See `extension::confidential_transfer_fee::instruction::ConfidentialTransferFeeInstruction`
    /// for further details about the extended instructions that share this
    /// instruction prefix
    ConfidentialTransferFeeExtension,

    /// This instruction is to be used to rescue SOL sent to any `TokenProgram`
    /// owned account by sending them to any other account, leaving behind only
    /// lamports for rent exemption.
    ///
    /// 0. `[writable]` Source Account owned by the token program
    /// 1. `[writable]` Destination account
    /// 2. `[signer]` Authority
    /// 3. ..`3+M` `[signer]` M signer accounts.
    WithdrawExcessLamports,

    /// The common instruction prefix for metadata pointer extension
    /// instructions.
    ///
    /// See `extension::metadata_pointer::instruction::MetadataPointerInstruction`
    /// for further details about the extended instructions that share this
    /// instruction prefix
    MetadataPointerExtension,

    /// The common instruction prefix for group pointer extension instructions.
    ///
    /// See `extension::group_pointer::instruction::GroupPointerInstruction`
    /// for further details about the extended instructions that share this
    /// instruction prefix
    GroupPointerExtension,

    /// The common instruction prefix for group member pointer extension
    /// instructions.
    ///
    /// See `extension::group_member_pointer::instruction::GroupMemberPointerInstruction`
    /// for further details about the extended instructions that share this
    /// instruction prefix
    GroupMemberPointerExtension,

    /// Instruction prefix for instructions to the confidential-mint-burn
    /// extension
    ConfidentialMintBurnExtension,

    /// Instruction prefix for instructions to the scaled ui amount
    /// extension
    ScaledUiAmountExtension,

    /// Instruction prefix for instructions to the pausable extension
    PausableExtension;

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

  public static Instruction initializeMint(final SolanaAccounts solanaAccounts,
                                           final PublicKey mint,
                                           final int decimals,
                                           final PublicKey mintAuthority,
                                           final PublicKey freezeAuthority) {
    return TokenProgram.initializeMint(
        solanaAccounts.invokedToken2022Program(),
        solanaAccounts,
        mint,
        decimals,
        mintAuthority,
        freezeAuthority
    );
  }

  public static Instruction initializeMint2(final SolanaAccounts solanaAccounts,
                                            final PublicKey mint,
                                            final int decimals,
                                            final PublicKey mintAuthority,
                                            final PublicKey freezeAuthority) {
    return TokenProgram.initializeMint2(
        solanaAccounts.invokedToken2022Program(),
        mint,
        decimals,
        mintAuthority,
        freezeAuthority
    );
  }

  public static Instruction initializeAccount(final SolanaAccounts solanaAccounts,
                                              final PublicKey account,
                                              final PublicKey mint,
                                              final PublicKey owner) {
    return TokenProgram.initializeAccount(solanaAccounts.invokedToken2022Program(), solanaAccounts, account, mint, owner);
  }

  public static Instruction initializeAccount2(final SolanaAccounts solanaAccounts,
                                               final PublicKey account,
                                               final PublicKey mint,
                                               final PublicKey owner) {
    return TokenProgram.initializeAccount2(solanaAccounts.invokedToken2022Program(), solanaAccounts, account, mint, owner);
  }

  public static Instruction initializeAccount3(final SolanaAccounts solanaAccounts,
                                               final PublicKey account,
                                               final PublicKey mint,
                                               final PublicKey owner) {
    return TokenProgram.initializeAccount3(solanaAccounts.invokedToken2022Program(), account, mint, owner);
  }

  public static Instruction initializeMultisig(final SolanaAccounts solanaAccounts,
                                               final PublicKey multisigAccount,
                                               final List<PublicKey> signerAccounts,
                                               final int requiredSignatures) {
    return TokenProgram.initializeMultisig(
        solanaAccounts.invokedToken2022Program(),
        solanaAccounts,
        multisigAccount,
        signerAccounts,
        requiredSignatures
    );
  }

  public static Instruction initializeMultisig2(final SolanaAccounts solanaAccounts,
                                                final PublicKey multisigAccount,
                                                final List<PublicKey> signerAccounts,
                                                final int requiredSignatures) {
    return TokenProgram.initializeMultisig2(
        solanaAccounts.invokedToken2022Program(),
        multisigAccount,
        signerAccounts,
        requiredSignatures
    );
  }

  public static Instruction transfer(final SolanaAccounts solanaAccounts,
                                     final PublicKey source,
                                     final PublicKey destination,
                                     final PublicKey owner,
                                     final long amount) {
    return TokenProgram.transfer(
        solanaAccounts.invokedToken2022Program(),
        source,
        destination,
        amount,
        owner
    );
  }

  public static Instruction transferMultisig(final SolanaAccounts solanaAccounts,
                                             final PublicKey source,
                                             final PublicKey destination,
                                             final PublicKey owner,
                                             final List<PublicKey> signerAccounts,
                                             final long amount) {
    return TokenProgram.transferMultisig(
        solanaAccounts.invokedToken2022Program(),
        source,
        destination,
        amount,
        owner,
        signerAccounts
    );
  }

  public static Instruction transferChecked(final SolanaAccounts solanaAccounts,
                                            final PublicKey source,
                                            final PublicKey destination,
                                            final long amount,
                                            final int decimals,
                                            final PublicKey owner,
                                            final PublicKey tokenMint) {
    return TokenProgram.transferChecked(
        solanaAccounts.invokedToken2022Program(),
        source,
        destination,
        amount,
        decimals,
        owner,
        tokenMint
    );
  }

  public static Instruction transferCheckedMultisig(final SolanaAccounts solanaAccounts,
                                                    final PublicKey source,
                                                    final PublicKey destination,
                                                    final long amount,
                                                    final int decimals,
                                                    final PublicKey owner,
                                                    final PublicKey tokenMint,
                                                    final List<PublicKey> signerAccounts) {
    return TokenProgram.transferCheckedMultisig(
        solanaAccounts.invokedToken2022Program(),
        source,
        destination,
        amount,
        decimals,
        owner,
        tokenMint,
        signerAccounts
    );
  }

  public static Instruction approve(final SolanaAccounts solanaAccounts,
                                    final PublicKey sourceAccount,
                                    final PublicKey delegate,
                                    final PublicKey owner,
                                    final long amount) {
    return TokenProgram.approve(solanaAccounts.invokedToken2022Program(), sourceAccount, delegate, owner, amount);
  }

  public static Instruction approveMultisig(final SolanaAccounts solanaAccounts,
                                            final PublicKey sourceAccount,
                                            final PublicKey delegate,
                                            final PublicKey owner,
                                            final List<PublicKey> signerAccounts,
                                            final long amount) {
    return TokenProgram.approveMultisig(
        solanaAccounts.invokedToken2022Program(),
        sourceAccount,
        delegate,
        owner,
        signerAccounts,
        amount
    );
  }

  public static Instruction approveChecked(final SolanaAccounts solanaAccounts,
                                           final PublicKey sourceAccount,
                                           final PublicKey tokenMint,
                                           final int decimals,
                                           final PublicKey delegate,
                                           final PublicKey owner,
                                           final long amount) {
    return TokenProgram.approveChecked(
        solanaAccounts.invokedToken2022Program(),
        sourceAccount,
        tokenMint,
        decimals,
        delegate,
        owner,
        amount
    );
  }

  public static Instruction approveCheckedMultisig(final SolanaAccounts solanaAccounts,
                                                   final PublicKey sourceAccount,
                                                   final PublicKey tokenMint,
                                                   final int decimals,
                                                   final PublicKey delegate,
                                                   final PublicKey owner,
                                                   final List<PublicKey> signerAccounts,
                                                   final long amount) {
    return TokenProgram.approveCheckedMultisig(
        solanaAccounts.invokedToken2022Program(),
        sourceAccount,
        tokenMint,
        decimals,
        delegate,
        owner,
        signerAccounts,
        amount
    );
  }

  public static Instruction revoke(final SolanaAccounts solanaAccounts,
                                   final PublicKey sourceAccount,
                                   final PublicKey owner) {
    return TokenProgram.revoke(solanaAccounts.invokedToken2022Program(), sourceAccount, owner);
  }

  public static Instruction revokeMultisig(final SolanaAccounts solanaAccounts,
                                           final PublicKey sourceAccount,
                                           final PublicKey owner,
                                           final List<PublicKey> signerAccounts) {
    return TokenProgram.revokeMultisig(solanaAccounts.invokedToken2022Program(), sourceAccount, owner, signerAccounts);
  }

  public static Instruction setAuthority(final SolanaAccounts solanaAccounts,
                                         final PublicKey account,
                                         final PublicKey authority,
                                         final TokenProgram.AuthorityType authorityType,
                                         final PublicKey newAuthority) {
    return TokenProgram.setAuthority(solanaAccounts.invokedToken2022Program(), account, authority, authorityType, newAuthority);
  }

  public static Instruction setAuthorityMultisig(final SolanaAccounts solanaAccounts,
                                                 final PublicKey account,
                                                 final PublicKey owner,
                                                 final List<PublicKey> signerAccounts,
                                                 final TokenProgram.AuthorityType authorityType,
                                                 final PublicKey newAuthority) {
    return TokenProgram.setAuthorityMultisig(solanaAccounts.invokedToken2022Program(), account, owner, signerAccounts, authorityType, newAuthority);
  }

  public static Instruction mintTo(final SolanaAccounts solanaAccounts,
                                   final PublicKey mint,
                                   final PublicKey account,
                                   final PublicKey authority,
                                   final long amount) {
    return TokenProgram.mintTo(solanaAccounts.invokedToken2022Program(), mint, account, authority, amount);
  }

  public static Instruction mintToMultisig(final SolanaAccounts solanaAccounts,
                                           final PublicKey mint,
                                           final PublicKey account,
                                           final PublicKey authority,
                                           final List<PublicKey> signerAccounts,
                                           final long amount) {
    return TokenProgram.mintToMultisig(
        solanaAccounts.invokedToken2022Program(),
        mint,
        account,
        authority,
        signerAccounts,
        amount
    );
  }

  public static Instruction mintToChecked(final SolanaAccounts solanaAccounts,
                                          final PublicKey mint,
                                          final int decimals,
                                          final PublicKey account,
                                          final PublicKey authority,
                                          final long amount) {
    return TokenProgram.mintToChecked(
        solanaAccounts.invokedToken2022Program(),
        mint,
        decimals,
        account,
        authority,
        amount
    );
  }

  public static Instruction mintToCheckedMultisig(final SolanaAccounts solanaAccounts,
                                                  final PublicKey mint,
                                                  final int decimals,
                                                  final PublicKey account,
                                                  final PublicKey authority,
                                                  final List<PublicKey> signerAccounts,
                                                  final long amount) {
    return TokenProgram.mintToCheckedMultisig(
        solanaAccounts.invokedToken2022Program(),
        mint,
        decimals,
        account,
        authority,
        signerAccounts,
        amount
    );
  }

  public static Instruction burn(final SolanaAccounts solanaAccounts,
                                 final PublicKey mint,
                                 final PublicKey account,
                                 final PublicKey authority,
                                 final long amount) {
    return TokenProgram.burn(solanaAccounts.invokedToken2022Program(), mint, account, authority, amount);
  }

  public static Instruction burnMultisig(final SolanaAccounts solanaAccounts,
                                         final PublicKey mint,
                                         final PublicKey account,
                                         final PublicKey authority,
                                         final List<PublicKey> signerAccounts,
                                         final long amount) {
    return TokenProgram.burnMultisig(
        solanaAccounts.invokedToken2022Program(),
        mint,
        account,
        authority,
        signerAccounts,
        amount
    );
  }

  public static Instruction burnChecked(final SolanaAccounts solanaAccounts,
                                        final PublicKey mint,
                                        final int decimals,
                                        final PublicKey account,
                                        final PublicKey authority,
                                        final long amount) {
    return TokenProgram.burnChecked(
        solanaAccounts.invokedToken2022Program(),
        mint,
        decimals,
        account,
        authority,
        amount
    );
  }

  public static Instruction burnCheckedMultisig(final SolanaAccounts solanaAccounts,
                                                final PublicKey mint,
                                                final int decimals,
                                                final PublicKey account,
                                                final PublicKey authority,
                                                final List<PublicKey> signerAccounts,
                                                final long amount) {
    return TokenProgram.burnCheckedMultisig(
        solanaAccounts.invokedToken2022Program(),
        mint,
        decimals,
        account,
        authority,
        signerAccounts,
        amount
    );
  }

  public static Instruction closeAccount(final SolanaAccounts solanaAccounts,
                                         final PublicKey tokenAccount,
                                         final PublicKey lamportDestination,
                                         final PublicKey owner) {
    return TokenProgram.closeAccount(solanaAccounts.invokedToken2022Program(), tokenAccount, lamportDestination, owner);
  }

  public static Instruction closeAccountMultisig(final SolanaAccounts solanaAccounts,
                                                 final PublicKey tokenAccount,
                                                 final PublicKey lamportDestination,
                                                 final PublicKey owner,
                                                 final List<PublicKey> signerAccounts) {
    return TokenProgram.closeAccountMultisig(
        solanaAccounts.invokedToken2022Program(),
        tokenAccount,
        lamportDestination,
        owner,
        signerAccounts
    );
  }

  public static Instruction freezeAccount(final SolanaAccounts solanaAccounts,
                                          final PublicKey account,
                                          final PublicKey mint,
                                          final PublicKey freezeAuthority) {
    return TokenProgram.freezeAccount(solanaAccounts.invokedToken2022Program(), account, mint, freezeAuthority);
  }

  public static Instruction freezeAccountMultisig(final SolanaAccounts solanaAccounts,
                                                  final PublicKey account,
                                                  final PublicKey mint,
                                                  final PublicKey freezeAuthority,
                                                  final List<PublicKey> signerAccounts) {
    return TokenProgram.freezeAccountMultisig(
        solanaAccounts.invokedToken2022Program(),
        account,
        mint,
        freezeAuthority,
        signerAccounts
    );
  }

  public static Instruction thawAccount(final SolanaAccounts solanaAccounts,
                                        final PublicKey account,
                                        final PublicKey mint,
                                        final PublicKey authority) {
    return TokenProgram.thawAccount(solanaAccounts.invokedToken2022Program(), account, mint, authority);
  }

  public static Instruction thawAccountMultisig(final SolanaAccounts solanaAccounts,
                                                final PublicKey account,
                                                final PublicKey mint,
                                                final PublicKey authority,
                                                final List<PublicKey> signerAccounts) {
    return TokenProgram.thawAccountMultisig(
        solanaAccounts.invokedToken2022Program(),
        account,
        mint,
        authority,
        signerAccounts
    );
  }

  public static Instruction initializeMintCloseAuthority(final AccountMeta invokedTokenProgram,
                                                         final PublicKey mint,
                                                         final PublicKey closeAuthority) {
    final var keys = List.of(createWrite(mint));

    final byte[] data;
    if (closeAuthority == null) {
      data = new byte[2];
    } else {
      data = new byte[2 + PUBLIC_KEY_LENGTH];
      data[1] = (byte) 1;
      closeAuthority.write(data, 2);
    }
    data[0] = TokenInstruction.InitializeMintCloseAuthority.discriminator;

    return createInstruction(invokedTokenProgram, keys, data);
  }

  public static Instruction initializeMintCloseAuthority(final SolanaAccounts solanaAccounts,
                                                         final PublicKey mint,
                                                         final PublicKey closeAuthority) {
    return initializeMintCloseAuthority(solanaAccounts.invokedToken2022Program(), mint, closeAuthority);
  }

  private static byte[] reallocateData(final Collection<ExtensionType> newExtensionTypes) {
    final byte[] data = new byte[1 + (Short.BYTES * newExtensionTypes.size())];
    data[0] = TokenInstruction.Reallocate.discriminator;
    int i = 1;
    for (final var extensionType : newExtensionTypes) {
      ByteUtil.putInt16LE(data, i, extensionType.ordinal());
      i += Short.BYTES;
    }
    return data;
  }

  public static Instruction reallocate(final AccountMeta invokedTokenProgram,
                                       final SolanaAccounts solanaAccounts,
                                       final PublicKey account,
                                       final PublicKey payer,
                                       final PublicKey owner,
                                       final Collection<ExtensionType> newExtensionTypes) {
    final var keys = List.of(
        createWrite(account),
        createWritableSigner(payer),
        solanaAccounts.readSystemProgram(),
        createReadOnlySigner(owner)
    );

    final byte[] data = reallocateData(newExtensionTypes);

    return createInstruction(invokedTokenProgram, keys, data);
  }

  public static Instruction reallocate(final SolanaAccounts solanaAccounts,
                                       final PublicKey account,
                                       final PublicKey payer,
                                       final PublicKey owner,
                                       final Collection<ExtensionType> newExtensionTypes) {
    return reallocate(
        solanaAccounts.invokedToken2022Program(),
        solanaAccounts,
        account,
        payer,
        owner,
        newExtensionTypes
    );
  }

  public static Instruction reallocateMultisig(final AccountMeta invokedTokenProgram,
                                               final SolanaAccounts solanaAccounts,
                                               final PublicKey account,
                                               final PublicKey payer,
                                               final PublicKey owner,
                                               final List<PublicKey> signerAccounts,
                                               final Collection<ExtensionType> newExtensionTypes) {
    final var keys = initSigners(4, signerAccounts);
    keys[0] = createWrite(account);
    keys[1] = createWritableSigner(payer);
    keys[2] = solanaAccounts.readSystemProgram();
    keys[3] = createReadOnlySigner(owner);

    final byte[] data = reallocateData(newExtensionTypes);

    return createInstruction(invokedTokenProgram, Arrays.asList(keys), data);
  }

  public static Instruction reallocateMultisig(final SolanaAccounts solanaAccounts,
                                               final PublicKey account,
                                               final PublicKey payer,
                                               final PublicKey owner,
                                               final List<PublicKey> signerAccounts,
                                               final Collection<ExtensionType> newExtensionTypes) {
    return reallocateMultisig(
        solanaAccounts.invokedToken2022Program(),
        solanaAccounts,
        account,
        payer,
        owner,
        signerAccounts,
        newExtensionTypes
    );
  }

  public static Instruction createNativeMint(final AccountMeta invokedTokenProgram,
                                             final SolanaAccounts solanaAccounts,
                                             final PublicKey fundingAccount,
                                             final PublicKey nativeMintAddress) {
    final var keys = List.of(
        createWritableSigner(fundingAccount),
        createWrite(nativeMintAddress),
        solanaAccounts.readSystemProgram()
    );
    return createInstruction(invokedTokenProgram, keys, TokenInstruction.CreateNativeMint.discriminatorBytes);
  }

  public static Instruction createNativeMint(final SolanaAccounts solanaAccounts,
                                             final PublicKey fundingAccount,
                                             final PublicKey nativeMintAddress) {
    return createNativeMint(solanaAccounts.invokedToken2022Program(), solanaAccounts, fundingAccount, nativeMintAddress);
  }

  public static Instruction initializeNonTransferableMint(final AccountMeta invokedTokenProgram,
                                                          final PublicKey mintAccount) {
    final var keys = List.of(createWrite(mintAccount));
    return createInstruction(invokedTokenProgram, keys, TokenInstruction.InitializeNonTransferableMint.discriminatorBytes);
  }

  public static Instruction initializeNonTransferableMint(final SolanaAccounts solanaAccounts,
                                                          final PublicKey mintAccount) {
    return initializeNonTransferableMint(solanaAccounts.invokedToken2022Program(), mintAccount);
  }

  public static Instruction initializePermanentDelegate(final AccountMeta invokedTokenProgram,
                                                        final PublicKey mintAccount,
                                                        final PublicKey delegate) {
    final var keys = List.of(createWrite(mintAccount));

    final byte[] data = new byte[1 + PUBLIC_KEY_LENGTH];
    data[0] = TokenInstruction.InitializePermanentDelegate.discriminator;
    delegate.write(data, 1);

    return createInstruction(invokedTokenProgram, keys, data);
  }

  public static Instruction initializePermanentDelegate(final SolanaAccounts solanaAccounts,
                                                        final PublicKey mintAccount,
                                                        final PublicKey delegate) {
    return initializePermanentDelegate(solanaAccounts.invokedToken2022Program(), mintAccount, delegate);
  }

  public static Instruction withdrawExcessLamports(final AccountMeta invokedTokenProgram,
                                                   final PublicKey sourceAccount,
                                                   final PublicKey destinationAccount,
                                                   final PublicKey authority,
                                                   final List<PublicKey> signerAccounts) {
    final var keys = initSigners(3, signerAccounts);
    keys[0] = createWrite(sourceAccount);
    keys[1] = createWrite(destinationAccount);
    keys[2] = createReadOnlySigner(authority);
    return createInstruction(invokedTokenProgram, Arrays.asList(keys), TokenInstruction.WithdrawExcessLamports.discriminatorBytes);
  }

  public static Instruction withdrawExcessLamports(final SolanaAccounts solanaAccounts,
                                                   final PublicKey sourceAccount,
                                                   final PublicKey destinationAccount,
                                                   final PublicKey authority,
                                                   final List<PublicKey> signerAccounts) {
    return withdrawExcessLamports(
        solanaAccounts.invokedToken2022Program(),
        sourceAccount,
        destinationAccount,
        authority,
        signerAccounts
    );
  }

  private Token2022Program() {
  }
}
