package com.romanpulov.jutilscore.storage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    public String getDataFileName() {
        return dataFileName;
    }

    @Override
    public String getBackupFolderName() {
        return backupFolderName;
    }

    @Override
    public String getBackupFileName() {
        return backupFileName;
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

    @Override
    public InputStream createBackupInputStream(String backupFileName) throws IOException {
        return new FileInputStream(backupFolderName + backupFileName);
    }
}
