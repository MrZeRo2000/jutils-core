package com.romanpulov.jutilscore.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface BackupProcessor {
    String createRollingBackup();
    String restoreBackup();
    List<String> getBackupFileNames();
    InputStream createBackupInputStream(String backupFileName) throws IOException;
}
