package com.romanpulov.jutilscore;

import com.romanpulov.jutilscore.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class FileUtilsTest {

    private static final Path folderPath = Paths.get(System.getProperty("java.io.tmpdir") + "jutils-core-file-utils-test/");
    private static final Path folderListPath = Paths.get(System.getProperty("java.io.tmpdir") + "jutils-core-file-utils-list-test/");

    static void clearFolder(Path folder) throws Exception {
        if (Files.exists(folder)) {
            Files.walk(folder)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        System.out.println("Deleting " + path.toAbsolutePath().toString());
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
        System.out.println("Starting FileUtilsTest: clearing folder");
        clearFolder(folderPath);
        clearFolder(folderListPath);

        Files.createDirectory(folderPath);
        System.out.println("Folder " + folderPath.toAbsolutePath().toString() + " created");

        Files.createDirectory(folderListPath);
        System.out.println("Folder " + folderListPath.toAbsolutePath().toString() + " created");
    }

    @AfterAll
    static void afterAll() throws Exception {
        clearFolder(folderPath);
        clearFolder(folderListPath);
        System.out.println("Finalizing FileUtilsTest: folder cleared");
    }

    @Test
    void mainTest() throws Exception {
        final Path filePath = Paths.get(folderPath.toString(), "test-file.bin");

        int maxCp = 2;

        FileUtils.setFileKeepCopiesCount(maxCp);

        // generate and write random bytes file

        byte[] b1 = new byte[2048];
        new Random().nextBytes(b1);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(b1)) {
            Files.copy(inputStream, filePath);
        }

        final Path copy1FilePath = Paths.get(FileUtils.getCopyFileName(filePath.toAbsolutePath().toString(), 1));

        // check if it is rolled

        Assertions.assertTrue(FileUtils.saveCopies(filePath.toAbsolutePath().toString()));
        Assertions.assertTrue(Files.exists(copy1FilePath));
        Assertions.assertArrayEquals(b1, Files.readAllBytes(copy1FilePath));

        // generate and write random bytes file 2

        byte[] b2 = new byte[2048];
        new Random().nextBytes(b2);

        Files.deleteIfExists(filePath);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(b2)) {
            Files.copy(inputStream, filePath);
        }

        final Path copy2FilePath = Paths.get(FileUtils.getCopyFileName(filePath.toAbsolutePath().toString(), 2));

        // check if it is rolled

        Assertions.assertTrue(FileUtils.saveCopies(filePath.toAbsolutePath().toString()));
        Assertions.assertTrue(Files.exists(copy2FilePath));
        Assertions.assertArrayEquals(b1, Files.readAllBytes(copy2FilePath));
        Assertions.assertArrayEquals(b2, Files.readAllBytes(copy1FilePath));

        final Path copy3FilePath = Paths.get(FileUtils.getCopyFileName(filePath.toAbsolutePath().toString(), 3));

        Assertions.assertTrue(FileUtils.saveCopies(filePath.toAbsolutePath().toString()));
        Assertions.assertArrayEquals(b2, Files.readAllBytes(copy2FilePath));
        Assertions.assertArrayEquals(b2, Files.readAllBytes(copy1FilePath));
        Assertions.assertFalse(Files.exists(copy3FilePath));

        // check if file is a backup file

        Assertions.assertTrue(FileUtils.isBackupFileName(copy2FilePath.toAbsolutePath().toString()));
        Assertions.assertTrue(FileUtils.isBackupFileName(copy1FilePath.toAbsolutePath().toString()));
        Assertions.assertFalse(FileUtils.isBackupFileName(filePath.toAbsolutePath().toString()));
        Assertions.assertFalse(FileUtils.isBackupFileName("aaa"));
    }

    @Test
    void listCopiesTest() throws Exception {

        final Path filePath = Paths.get(folderListPath.toString(), "test-file-list.bin");

        int maxCp = 2;

        FileUtils.setFileKeepCopiesCount(maxCp);

        // generate and write random bytes file

        byte[] b1 = new byte[2048];
        new Random().nextBytes(b1);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(b1)) {
            Files.copy(inputStream, filePath);
        }

        final Path copy1FilePath = Paths.get(FileUtils.getCopyFileName(filePath.toAbsolutePath().toString(), 1));

        // check if it is rolled

        List<String> fileList = Files.list(folderListPath).map(path -> path.toAbsolutePath().toString()).collect(Collectors.toList());

        System.out.println("Listing path");
        System.out.println(fileList);
        System.out.println("Listing path completed");

        Assertions.assertTrue(FileUtils.saveListCopies(fileList));
        Assertions.assertTrue(Files.exists(copy1FilePath));
        Assertions.assertArrayEquals(b1, Files.readAllBytes(copy1FilePath));

        // generate and write random bytes file 2

        byte[] b2 = new byte[2048];
        new Random().nextBytes(b2);

        Files.deleteIfExists(filePath);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(b2)) {
            Files.copy(inputStream, filePath);
        }

        final Path copy2FilePath = Paths.get(FileUtils.getCopyFileName(filePath.toAbsolutePath().toString(), 2));

        // check if it is rolled

        fileList = Files.list(folderListPath).map(path -> path.toAbsolutePath().toString()).collect(Collectors.toList());
        Assertions.assertTrue(FileUtils.saveListCopies(fileList));
        Assertions.assertTrue(Files.exists(copy2FilePath));
        Assertions.assertArrayEquals(b1, Files.readAllBytes(copy2FilePath));
        Assertions.assertArrayEquals(b2, Files.readAllBytes(copy1FilePath));

        // another copy

        final Path copy3FilePath = Paths.get(FileUtils.getCopyFileName(filePath.toAbsolutePath().toString(), 3));

        fileList = Files.list(folderListPath).map(path -> path.toAbsolutePath().toString()).collect(Collectors.toList());
        Assertions.assertTrue(FileUtils.saveListCopies(fileList));
        Assertions.assertArrayEquals(b2, Files.readAllBytes(copy2FilePath));
        Assertions.assertArrayEquals(b2, Files.readAllBytes(copy1FilePath));
        Assertions.assertFalse(Files.exists(copy3FilePath));

    }
}
