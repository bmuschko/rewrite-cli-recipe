# rewrite-cli-recipe

An [OpenRewrite](https://docs.openrewrite.org/) recipe that demonstrates cross-platform CLI command execution from within a recipe. Uses the `echo` command as an example.

## Project Structure

```
src/main/java/com/yourorg/
├── ExecHelper.java      # Cross-platform command execution utility
└── EchoMessage.java     # ScanningRecipe that runs echo via CLI

src/test/java/com/yourorg/
└── EchoMessageTest.java # Tests verifying correct echo output
```

## How It Works

**`ExecHelper`** handles OS differences using `ProcessBuilder`:
- **Windows**: wraps commands with `cmd.exe /c` (required for shell built-ins like `echo`)
- **Linux/Mac**: executes commands directly

**`EchoMessage`** is a `ScanningRecipe` that:
1. Accepts a `message` option
2. Scans the source set to ensure idempotency (skips if `echo-output.txt` already exists)
3. Executes `echo <message>` in the generate phase
4. Produces `echo-output.txt` containing the command output

## Prerequisites

- Java 17+

## Building & Testing

```bash
./gradlew build
```

Run tests only:

```bash
./gradlew test
```

## CI

A [GitHub Actions workflow](.github/workflows/ci.yml) runs tests on a matrix of **Ubuntu**, **Windows**, and **macOS**.

## Using the Recipe

Register the recipe in a `rewrite.yml` file:

```yaml
type: specs.openrewrite.org/v1beta/recipe
name: com.yourorg.EchoMessage
recipeList:
  - com.yourorg.EchoMessage:
      message: "Hello from OpenRewrite"
```

## Adapting for Other CLI Tools

To invoke a different CLI tool, follow the same pattern:

1. Use `ExecHelper.buildCommand("your-tool", "--flag", "arg")` to build a cross-platform command
2. Use `ExecHelper.exec(command, workingDir, timeoutMinutes)` to run it and capture output
3. Check the exit code and process the output in your recipe's `generate` or `getVisitor` method
