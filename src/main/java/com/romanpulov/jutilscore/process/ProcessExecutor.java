package com.romanpulov.jutilscore.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProcessExecutor {
    public record ExecutionResult(int exitCode, String output) {}

    public static ExecutionResult executeCommand(String ...command)
            throws IOException, InterruptedException, ExecutionException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true); // stderr merged into stdout

        Process process = pb.start();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> outputFuture = executor.submit(() -> {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append(System.lineSeparator());
                }
            }
            return sb.toString();
        });

        int exitCode = process.waitFor();
        executor.shutdown();

        return new ExecutionResult(exitCode, outputFuture.get());
    }

    public static ExecutionResult executePowershell(String scriptPath, String... args)
            throws IOException, InterruptedException, ExecutionException {
        List<String> commands = new ArrayList<>(List.of(
                "powershell.exe",
                "-ExecutionPolicy", "Bypass",
                "-NonInteractive",
                "-File", scriptPath
        ));
        Collections.addAll(commands, args); // append script arguments

        return executeCommand(commands.toArray(new String[0]));
    }
}
