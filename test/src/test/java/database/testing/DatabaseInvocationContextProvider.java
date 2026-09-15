package database.testing;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import java.util.List;
import java.util.stream.Stream;

public final class DatabaseInvocationContextProvider implements TestTemplateInvocationContextProvider {

    private static final List<TestTemplateInvocationContext> contexts = List.of(
            invocationContext(new InMemoryDatabase(), "H2"),
            invocationContext(TestContainerDatabase.MARIADB, "MariaDB"),
            invocationContext(TestContainerDatabase.POSTGRES, "PostgreSQL")

    );

    private static TestTemplateInvocationContext invocationContext(TestDatabase db, String displayName) {
        return new TestTemplateInvocationContext() {
            @Override
            public String getDisplayName(int invocationIndex) {
                return displayName;
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
                return List.of(new ParameterResolver() {
                    @Override
                    public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) {
                        return pc.getParameter().getType() == TestDatabase.class;
                    }

                    @Override
                    public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
                        return db;
                    }
                }, (BeforeAllCallback) _ -> db.stop()); // Cleanup
            }
        };
    }

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        var env = System.getenv("TEST_DATABASES");
        if (env != null) {
            if (env.equalsIgnoreCase("all")) {
                return contexts.stream();
            }
            var some = contexts.stream().filter(ctx -> ctx.getDisplayName(0).equalsIgnoreCase(env)).toList();
            if (some.isEmpty()) {
                throw new IllegalArgumentException("Unknown database requested in '" + env + "'");
            }
            return some.stream();
        }
        return contexts.stream().filter(ctx -> ctx.getDisplayName(0).equalsIgnoreCase("h2"));
    }

}