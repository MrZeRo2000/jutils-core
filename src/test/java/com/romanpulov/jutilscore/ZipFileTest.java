package com.romanpulov.jutilscore;

import com.romanpulov.jutilscore.io.ZipFileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileTest {

    private static final Path folderPath = Paths.get(System.getProperty("java.io.tmpdir") + "jutils-core-zip-file-test/");

    static void clearFolder() throws Exception  {
        if (Files.exists(folderPath)) {
            try (Stream<Path> folderStreamPath = Files.walk(folderPath)) {
                folderStreamPath
                        .filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            System.out.println("Deleting " + path.toAbsolutePath());
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
                Files.delete(folderPath);

            }
        }
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        System.out.println("Starting ZipFileTest: clearing folder");
        clearFolder();

        Files.createDirectory(folderPath);
        System.out.println("Folder " + folderPath.toAbsolutePath() + " created");
    }

    @AfterAll
    static void afterAll() throws Exception {
        clearFolder();
        System.out.println("Finalizing ZipFileTest: folder cleared");
    }

    @Test
    void testZipFile() throws Exception {
        final Path filePath  = Paths.get(folderPath.toString(), "test-file.bin");
        final Path zipFilePath  = Paths.get(folderPath.toString(), ZipFileUtils.getZipFileName("test-file.bin"));
        final Path unZipFilePath  = Paths.get(folderPath.toString(), "test-file-unzip.bin");

        // generate and write random bytes file

        byte[] b = new byte[2048];
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

        // unzip with stream

        Assertions.assertFalse(Files.exists(unZipFilePath));
        try (
                InputStream inputStream = new FileInputStream(zipFilePath.toString());
                OutputStream outputStream = new FileOutputStream(unZipFilePath.toString())
        ) {
            String zipEntryName = ZipFileUtils.unZipStream(inputStream, outputStream);
            Assertions.assertEquals(filePath.getFileName().toString(), zipEntryName);
        }
        Assertions.assertTrue(Files.exists(unZipFilePath));

        // check if it is the same as bytes which were written

        Assertions.assertArrayEquals(b, Files.readAllBytes(unZipFilePath));
    }

    @Test
    void testZipFiles() throws Exception {
        final Path zipFilePath = Paths.get(folderPath.toString(), "test-files.zip");
        final String[] names = {"multi-1.bin", "multi-2.bin", "multi-3.bin"};

        // generate random bytes files

        Random random = new Random();
        Map<String, byte[]> content = new LinkedHashMap<>();
        List<File> files = new ArrayList<>();
        for (String name : names) {
            byte[] b = new byte[1024 + random.nextInt(2048)];
            random.nextBytes(b);
            Path path = Paths.get(folderPath.toString(), name);
            Files.write(path, b);
            content.put(name, b);
            files.add(path.toFile());
        }

        // zip all files into one archive

        Assertions.assertFalse(Files.exists(zipFilePath));
        Assertions.assertTrue(ZipFileUtils.zipFiles(zipFilePath.toString(), files));
        Assertions.assertTrue(Files.exists(zipFilePath));

        // check archive entries and their content

        Map<String, byte[]> actual = new LinkedHashMap<>();
        try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                try (InputStream inputStream = zipFile.getInputStream(entry)) {
                    actual.put(entry.getName(), inputStream.readAllBytes());
                }
            }
        }

        Assertions.assertEquals(content.keySet(), actual.keySet());
        for (String name : names) {
            Assertions.assertArrayEquals(content.get(name), actual.get(name));
        }

        // missing source file should fail

        Assertions.assertFalse(ZipFileUtils.zipFiles(
                Paths.get(folderPath.toString(), "test-files-missing.zip").toString(),
                List.of(new File(folderPath.toFile(), "does-not-exist.bin"))));
    }
}
