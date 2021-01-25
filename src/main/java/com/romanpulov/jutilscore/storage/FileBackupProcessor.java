package com.romanpulov.jutilscore.storage;

import java.util.List;

public class FileBackupProcessor implements BackupProcessor {
    private final String dataFileName;
    private final String backupFolderName;
    private final String backupFileName;

    public FileBackupProcessor(String dataFileName, String backupFolderName, String backupFileName) {
        this.dataFileName = dataFileName;
        this.backupFolderName = backupFolderName;
        this.backupFileName = backupFileName;
    }

    @Override
    public String createRollingBackup() {
        return BackupUtils.createRollingLocalBackup(dataFileName, backupFolderName, backupFileName);
    }

    @Override
    public String restoreBackup() {
        return BackupUtils.restoreBackup(dataFileName, backupFolderName, backupFileName);
    }

    @Override
    public List<String> getBackupFileNames() {
        return BackupUtils.getBackupFileNames(backupFolderName);
    }
}
