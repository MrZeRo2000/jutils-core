package com.romanpulov.jutilscore;

import com.romanpulov.jutilscore.io.ZipFileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class ZipFileTest {

    private static final Path folderPath = Paths.get(System.getProperty("java.io.tmpdir") + "jutils-core-zip-file-test/");

    static void clearFolder() throws Exception  {
        Files.walk(folderPath)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    System.out.println("Deleting " + path.toAbsolutePath().toString());
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        Files.deleteIfExists(folderPath);
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        clearFolder();

        Files.createDirectory(folderPath);
        System.out.println("Folder " + folderPath.toAbsolutePath().toString() + " created");
    }

    @AfterAll
    static void afterAll() throws Exception {
        //Files.deleteIfExists(folderPath);
        //System.out.println("Folder deleted");
    }

    @Test
    void testZipFile() throws Exception {
        final Path filePath  = Paths.get(folderPath.toString(), "test-file.bin");
        final Path zipFilePath  = Paths.get(folderPath.toString(), ZipFileUtils.getZipFileName("test-file.bin"));
        final Path unZipFilePath  = Paths.get(folderPath.toString(), "test-file-unzip.bin");

        // generate and write random bytes file

        byte[] b = new byte[20];
        new Random().nextBytes(b);

        try(ByteArrayInputStream inputStream = new ByteArrayInputStream(b)) {
            Files.copy(inputStream, filePath);
        }

        // zip random bytes file

        Assertions.assertFalse(Files.exists(zipFilePath));
        ZipFileUtils.zipFile(filePath.getParent().toAbsolutePath() + File.separator, filePath.getFileName().toString());
        Assertions.assertTrue(Files.exists(zipFilePath));

        // delete initial file, leave zip file only

        Assertions.assertTrue(Files.exists(filePath));
        Files.delete(filePath);
        Assertions.assertFalse(Files.exists(filePath));

        // unzip file

        ZipFileUtils.unZipFile(zipFilePath.getParent().toAbsolutePath() + File.separator, zipFilePath.getFileName().toString());
        Assertions.assertTrue(Files.exists(filePath));

        // check if it is the same as bytes which were written

        Assertions.assertArrayEquals(b, Files.readAllBytes(filePath));
    }
}
