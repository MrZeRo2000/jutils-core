package com.romanpulov.jutilscore.storage;

import java.util.List;

public interface BackupProcessor {
    String createRollingBackup();
    String restoreBackup();
    List<String> getBackupFileNames();
}
