package com.yourorg;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Cross-platform command execution utility.
 * <p>
 * On Windows, commands are wrapped with {@code cmd.exe /c} to support
 * shell built-ins like {@code echo}. On Unix/Mac, commands are executed directly.
 */
public class ExecHelper {

    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    /**
     * Builds a command list suitable for the current OS.
     * On Windows, prepends {@code cmd.exe /c} and joins all args into a single
     * command string so that shell built-ins (like {@code echo}) work correctly.
     * On Unix/Mac, the args are passed directly to {@link ProcessBuilder}.
     */
    public static List<String> buildCommand(String... args) {
        List<String> command = new ArrayList<>();
        if (isWindows()) {
            command.add("cmd.exe");
            command.add("/c");
            command.add(String.join(" ", args));
        } else {
            Collections.addAll(command, args);
        }
        return command;
    }

    /**
     * Executes a command and returns the result.
     *
     * @param command        the command and arguments
     * @param workingDir     optional working directory (may be {@code null})
     * @param timeoutMinutes maximum time to wait for the process
     * @return the exit code and trimmed combined stdout/stderr output
     */
    public static ExecResult exec(List<String> command, Path workingDir, long timeoutMinutes) {
        ProcessBuilder builder = new ProcessBuilder(command);
        if (workingDir != null) {
            builder.directory(workingDir.toFile());
        }
        builder.redirectErrorStream(true);

        try {
            Process process = builder.start();
            String output;
            try (InputStream is = process.getInputStream()) {
                output = new String(is.readAllBytes());
            }
            if (!process.waitFor(timeoutMinutes, TimeUnit.MINUTES)) {
                process.destroyForcibly();
                throw new RuntimeException(
                        String.format("Command '%s' timed out after %d minute(s)",
                                String.join(" ", command), timeoutMinutes));
            }
            return new ExecResult(process.exitValue(), output.trim());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to execute command: " + String.join(" ", command), e);
        }
    }

    public static class ExecResult {
        private final int exitCode;
        private final String output;

        public ExecResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getOutput() {
            return output;
        }
    }
}
