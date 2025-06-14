package software.sava.solana.programs.stake;

public enum StakeStatus {
  // Stake account is active, there may be a transient stake as well
  Active,
  // Only transient stake account exists, when a transient stake is
  // deactivating during validator removal
  DeactivatingTransient,
  // No more validator stake accounts exist, entry ready for removal during
  // 'UpdateStakePoolBalance'
  ReadyForRemoval,
  // Only the validator stake account is deactivating, no transient stake
  // account exists
  DeactivatingValidator,
  // Both the transient and validator stake account are deactivating, when
  // a validator is removed with a transient stake active
  DeactivatingAll
}
