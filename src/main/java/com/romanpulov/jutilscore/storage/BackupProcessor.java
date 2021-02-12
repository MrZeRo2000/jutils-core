package com.romanpulov.jutilscore.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface BackupProcessor {
    String getDataFileName();
    String getBackupFolderName();
    String getBackupFileName();

    List<String> getBackupFileNames();

    String createRollingBackup();
    String restoreBackup();
    InputStream createBackupInputStream(String backupFileName) throws IOException;
}
