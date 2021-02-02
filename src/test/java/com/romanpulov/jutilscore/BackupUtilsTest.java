package com.romanpulov.jutilscore;

import com.romanpulov.jutilscore.storage.BackupUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class BackupUtilsTest {
    private static final Path folderPath = Paths.get(System.getProperty("java.io.tmpdir") + "jutils-core-file-utils-backup-test/");

    static void clearFolder(Path folder) throws Exception {
        if (Files.exists(folder)) {
            Files.walk(folder)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        System.out.println("Deleting file:" + path.toAbsolutePath().toString());
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            Files.walk(folder)
                    .filter(path -> Files.isDirectory(path) && !path.equals(folder))
                    .forEach(path -> {
                        System.out.println("Deleting directory:" + path.toAbsolutePath().toString());
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            Files.delete(folder);
        }
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        System.out.println("Starting BackupUtilsTest: clearing folder");
        clearFolder(folderPath);

        Files.createDirectory(folderPath);
        System.out.println("Folder " + folderPath.toAbsolutePath().toString() + " created");
    }

    @AfterAll
    static void afterAll() throws Exception {
        //clearFolder(folderPath);
        //System.out.println("Finalizing BackupUtilsTest: folder cleared");
    }

    @Test
    void mainTest() throws Exception {
        final Path filePath  = Paths.get(folderPath.toString(), "data-file.bin");

        // generate and write random bytes file

        byte[] b1 = new byte[2056];
        new Random().nextBytes(b1);

        Assertions.assertFalse(Files.exists(filePath));

        try(ByteArrayInputStream inputStream = new ByteArrayInputStream(b1)) {
            Files.copy(inputStream, filePath);
        }

        Assertions.assertTrue(Files.exists(filePath));

        String dataFileName = filePath.toAbsolutePath().toString();
        String backupFolderName = filePath.getParent().toAbsolutePath().toString() + "/backup";
        String backupFileName = "data-file-backup.bin";

        // folder does not exist and backups are created
        Assertions.assertNotNull(BackupUtils.createRollingLocalBackup(dataFileName, backupFolderName, backupFileName));
        Assertions.assertEquals(1, BackupUtils.getBackupFiles(backupFolderName).length);

        Files.deleteIfExists(Paths.get(BackupUtils.getBackupFiles(backupFolderName)[0].getAbsolutePath()));

        // folder exists and backups are created
        Assertions.assertNotNull(BackupUtils.createRollingLocalBackup(dataFileName, backupFolderName, backupFileName));
        Assertions.assertEquals(1, BackupUtils.getBackupFiles(backupFolderName).length);


        // generate and write random bytes file 2

        byte[] b2 = new byte[2012];
        new Random().nextBytes(b2);

        Files.deleteIfExists(filePath);

        Assertions.assertFalse(Files.exists(filePath));

        try(ByteArrayInputStream inputStream = new ByteArrayInputStream(b2)) {
            Files.copy(inputStream, filePath);
        }

        Assertions.assertTrue(Files.exists(filePath));

        Assertions.assertNotNull(BackupUtils.createRollingLocalBackup(dataFileName, backupFolderName, backupFileName));

        Assertions.assertEquals(2, BackupUtils.getBackupFiles(backupFolderName).length);

        // restoring from backup

        Files.deleteIfExists(filePath);
        Assertions.assertFalse(Files.exists(filePath));

        Assertions.assertNotNull(BackupUtils.restoreBackup(dataFileName, backupFolderName, backupFileName));

        Assertions.assertTrue(Files.exists(filePath));

        // check restored file content, should be as b2

        Assertions.assertArrayEquals(b2, Files.readAllBytes(filePath));

    }

}
