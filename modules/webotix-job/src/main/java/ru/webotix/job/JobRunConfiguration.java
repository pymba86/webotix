package ru.webotix.job;

public class JobRunConfiguration {

    private int guardianLoopSeconds;
    private int databaseLockSeconds;

    public int getGuardianLoopSeconds() {
        return guardianLoopSeconds;
    }

    public void setGuardianLoopSeconds(int guardianLoopSeconds) {
        this.guardianLoopSeconds = guardianLoopSeconds;
    }

    public int getDatabaseLockSeconds() {
        return databaseLockSeconds;
    }

    public void setDatabaseLockSeconds(int databaseLockSeconds) {
        this.databaseLockSeconds = databaseLockSeconds;
    }
}
