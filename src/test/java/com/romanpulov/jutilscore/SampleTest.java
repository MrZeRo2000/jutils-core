package com.romanpulov.jutilscore;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SampleTest {

    private static Path folderPath;

    @Test
    void testTrue(){
        Assertions.assertTrue(true);
    }

    @Test
    void testTemp() throws Exception {
        System.out.println("Creating folder");
        Files.createDirectory(folderPath);
        System.out.println("Folder created");
    }

    @BeforeAll
    static void beforeAll() {
        String folderName = System.getProperty("java.io.tmpdir") + "jutils-core-sample-test/";
        System.out.println("Folder Name:" + folderName);

        folderPath = Paths.get(folderName);
    }

    @AfterAll
    static void afterAll() throws Exception {
        Files.deleteIfExists(folderPath);
        System.out.println("Folder deleted");
    }
}
