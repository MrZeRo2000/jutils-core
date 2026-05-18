package com.romanpulov.jutilscore;

import com.romanpulov.jutilscore.process.ProcessExecutor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProcessExecutorTest {

    private static final Path folderPath = Paths.get(System.getProperty("java.io.tmpdir") + "jutils-core-process-executor-test/");

    private static final String SUCCESS_SCRIPT = """
            Write-Output 'Hello from PowerShell'
            exit 0
            """;

    private static final String ERROR_SCRIPT = """
            Write-Output 'About to fail'
            exit 42
            """;

    @BeforeAll
    static void beforeAll() throws Exception {
        FileUtilsTest.clearFolder(folderPath);
        Files.createDirectory(folderPath);
        System.out.println("Folder " + folderPath.toAbsolutePath() + " created");
    }

    @AfterAll
    static void afterAll() throws Exception {
        FileUtilsTest.clearFolder(folderPath);
        System.out.println("Finalizing ProcessExecutorTest: folder cleared");
    }

    private static Path writeScript(String name, String content) throws Exception {
        Path scriptPath = Paths.get(folderPath.toString(), name);
        Files.writeString(scriptPath, content);
        return scriptPath;
    }

    @Test
    void testExecutePowershellSuccess() throws Exception {
        Path scriptPath = writeScript("success.ps1", SUCCESS_SCRIPT);

        ProcessExecutor.ExecutionResult result =
                ProcessExecutor.executePowershell(scriptPath.toAbsolutePath().toString());

        System.out.println("Success exit code: " + result.exitCode());
        System.out.println("Success output: " + result.output());

        Assertions.assertEquals(0, result.exitCode());
        Assertions.assertTrue(result.output().contains("Hello from PowerShell"));
    }

    @Test
    void testExecutePowershellError() throws Exception {
        Path scriptPath = writeScript("error.ps1", ERROR_SCRIPT);

        ProcessExecutor.ExecutionResult result =
                ProcessExecutor.executePowershell(scriptPath.toAbsolutePath().toString());

        System.out.println("Error exit code: " + result.exitCode());
        System.out.println("Error output: " + result.output());

        Assertions.assertNotEquals(0, result.exitCode());
        Assertions.assertEquals(42, result.exitCode());
        Assertions.assertTrue(result.output().contains("About to fail"));
    }
}
