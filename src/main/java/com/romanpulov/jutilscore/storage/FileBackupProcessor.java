package com.romanpulov.jutilscore.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileBackupProcessor implements BackupProcessor {
    private final String mDataFileName;
    private final String mBackupFolderName;
    private final String mBackupFileName;

    public FileBackupProcessor(String mDataFileName, String mBackupFolderName, String mBackupFileName) {
        this.mDataFileName = mDataFileName;
        this.mBackupFolderName = mBackupFolderName;
        this.mBackupFileName = mBackupFileName;
    }

    @Override
    public String createRollingBackup() {
        return BackupUtils.createRollingLocalBackup(mDataFileName, mBackupFolderName, mBackupFileName);
    }

    @Override
    public String restoreBackup() {
        return BackupUtils.restoreBackup(mDataFileName, mBackupFolderName, mBackupFileName);
    }

    @Override
    public List<String> getBackupFileNames() {
        File[] backupFiles = BackupUtils.getBackupFiles(mBackupFolderName);

        List<String> backupFileNames = new ArrayList<>(backupFiles.length);
        for (File f: backupFiles) {
            backupFileNames.add(f.getAbsolutePath());
        }

        return backupFileNames;
    }
}
