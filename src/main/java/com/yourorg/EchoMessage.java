package com.yourorg;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.*;
import org.openrewrite.text.PlainTextParser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;

/**
 * An OpenRewrite recipe that executes the {@code echo} CLI command with a
 * configurable message and writes the output to {@code echo-output.txt}.
 * <p>
 * Demonstrates cross-platform CLI invocation from within a recipe.
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class EchoMessage extends ScanningRecipe<EchoMessage.Accumulator> {

    private static final Path OUTPUT_PATH = Paths.get("echo-output.txt");

    @Option(displayName = "Message",
            description = "The message to pass to the echo command.")
    String message;

    @Override
    public String getDisplayName() {
        return "Echo message";
    }

    @Override
    public String getDescription() {
        return "Executes the echo command with the provided message and writes the output to echo-output.txt.";
    }

    static class Accumulator {
        boolean hasSourceFiles;
        boolean outputExists;
    }

    @Override
    public Accumulator getInitialValue(ExecutionContext ctx) {
        return new Accumulator();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Accumulator acc) {
        return new TreeVisitor<Tree, ExecutionContext>() {
            @Override
            public Tree visit(Tree tree, ExecutionContext ctx) {
                if (tree instanceof SourceFile) {
                    acc.hasSourceFiles = true;
                    if (((SourceFile) tree).getSourcePath().equals(OUTPUT_PATH)) {
                        acc.outputExists = true;
                    }
                }
                return tree;
            }
        };
    }

    @Override
    public Collection<? extends SourceFile> generate(Accumulator acc, ExecutionContext ctx) {
        if (!acc.hasSourceFiles || acc.outputExists) {
            return Collections.emptyList();
        }

        ExecHelper.ExecResult result = ExecHelper.exec(
                ExecHelper.buildCommand("echo", message), null, 1);

        if (result.getExitCode() != 0) {
            throw new RuntimeException("Echo command failed with exit code "
                    + result.getExitCode() + ": " + result.getOutput());
        }

        return Collections.singletonList(
                PlainTextParser.builder().build()
                        .parse(result.getOutput())
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Failed to create output file"))
                        .withSourcePath(OUTPUT_PATH)
        );
    }
}
