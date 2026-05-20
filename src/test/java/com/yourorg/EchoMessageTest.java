package com.yourorg;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.test.SourceSpecs.text;

class EchoMessageTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new EchoMessage("Hello World"));
    }

    @Test
    void echoesMessage() {
        rewriteRun(
                // An existing source file is required so the scanner marks work as needed
                text("placeholder"),
                // The recipe generates echo-output.txt with the command output
                text(null, "Hello World", spec -> spec.path("echo-output.txt"))
        );
    }

    @Test
    void echoesCustomMessage() {
        rewriteRun(
                spec -> spec.recipe(new EchoMessage("OpenRewrite")),
                text("placeholder"),
                text(null, "OpenRewrite", spec -> spec.path("echo-output.txt"))
        );
    }
}
