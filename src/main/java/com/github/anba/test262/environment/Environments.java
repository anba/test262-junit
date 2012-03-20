/**
 * Copyright (c) 2011-2012 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/test262-junit>
 */
package com.github.anba.test262.environment;

import static com.github.anba.test262.util.Functional.filterMap;
import static com.github.anba.test262.util.Functional.intoCollection;
import static java.util.Collections.emptyList;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration.Configuration;
import org.junit.Assert;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

import com.github.anba.test262.util.Test262AssertionError;
import com.github.anba.test262.util.Test262Info;
import com.github.anba.test262.util.Functional.Mapper;
import com.github.anba.test262.util.Functional.Predicate;

/**
 * @author André Bargull
 * 
 */
public final class Environments {
    private Environments() {
    }

    public interface EnvironmentProvider<T extends GlobalObject> {
        Environment<T> environment(String testsuite, String sourceName,
                Test262Info info);
    }

    /**
     * Amended {@link Assert#fail()} method
     */
    private static void failWith(String message, String sourceName) {
        String msg = String.format("%s [file: %s]", message, sourceName);
        throw new Test262AssertionError(msg);
    }

    /**
     * Returns the default environment provider
     */
    public static <T extends GlobalObject> EnvironmentProvider<T> get(
            Configuration configuration) {
        String library = configuration.getString("test.provider", "rhino");
        switch (library) {
        case "rhino":
            return rhino(configuration);
        default:
            throw new IllegalArgumentException(library);
        }
    }

    /**
     * Creates a new Rhino environment
     */
    public static <T extends GlobalObject> EnvironmentProvider<T> rhino(
            final Configuration configuration) {
        final int version = configuration.getInt("rhino.version",
                Context.VERSION_DEFAULT);
        final String compiler = configuration
                .getString("rhino.compiler.default");
        List<?> enabledFeatures = configuration.getList(
                "rhino.features.enabled", emptyList());
        List<?> disabledFeatures = configuration.getList(
                "rhino.features.disabled", emptyList());
        final Set<Integer> enabled = intoCollection(
                filterMap(enabledFeatures, notEmptyString, toInteger),
                new HashSet<Integer>());
        final Set<Integer> disabled = intoCollection(
                filterMap(disabledFeatures, notEmptyString, toInteger),
                new HashSet<Integer>());

        /**
         * Initializes the global {@link ContextFactory} according to the
         * supplied configuration
         * 
         * @see ContextFactory#initGlobal(ContextFactory)
         */
        final ContextFactory factory = new ContextFactory() {
            @Override
            protected boolean hasFeature(Context cx, int featureIndex) {
                if (enabled.contains(featureIndex)) {
                    return true;
                } else if (disabled.contains(featureIndex)) {
                    return false;
                }
                return super.hasFeature(cx, featureIndex);
            }

            @Override
            protected Context makeContext() {
                Context context = super.makeContext();
                context.setLanguageVersion(version);
                return context;
            }
        };

        EnvironmentProvider<RhinoGlobalObject> provider = new EnvironmentProvider<RhinoGlobalObject>() {
            @Override
            public RhinoEnv<RhinoGlobalObject> environment(
                    final String testsuite, final String sourceName,
                    final Test262Info info) {
                Configuration c = configuration.subset(testsuite);
                final boolean strictSupported = c.getBoolean("strict", false);
                final String encoding = c.getString("encoding", "UTF-8");
                final String libpath = c.getString("lib_path");

                final Context cx = factory.enterContext();
                final AtomicReference<RhinoGlobalObject> $global = new AtomicReference<>();

                final RhinoEnv<RhinoGlobalObject> environment = new RhinoEnv<RhinoGlobalObject>() {
                    @Override
                    public RhinoGlobalObject global() {
                        return $global.get();
                    }

                    @Override
                    protected String getEvaluator() {
                        return compiler;
                    }

                    @Override
                    protected String getCharsetName() {
                        return encoding;
                    }

                    @Override
                    public void exit() {
                        Context.exit();
                    }
                };

                @SuppressWarnings({ "serial" })
                final RhinoGlobalObject global = new RhinoGlobalObject() {
                    {
                        cx.initStandardObjects(this, false);
                    }

                    @Override
                    protected boolean isStrictSupported() {
                        return strictSupported;
                    }

                    @Override
                    protected String getDescription() {
                        return info.getDescription();
                    }

                    @Override
                    protected void failure(String message) {
                        failWith(message, sourceName);
                    }

                    @Override
                    protected void include(Path path) throws IOException {
                        // resolve the input file against the library path
                        Path file = Paths.get(libpath).resolve(path);
                        InputStream source = Files.newInputStream(file);
                        environment.eval(file.getFileName().toString(), source);
                    }
                };

                $global.set(global);

                return environment;
            }
        };

        @SuppressWarnings("unchecked")
        EnvironmentProvider<T> p = (EnvironmentProvider<T>) provider;

        return p;
    }

    private static final Predicate<Object> notEmptyString = new Predicate<Object>() {
        @Override
        public boolean eval(Object value) {
            return (value != null && !value.toString().isEmpty());
        }
    };

    private static final Mapper<Object, Integer> toInteger = new Mapper<Object, Integer>() {
        @Override
        public Integer map(Object t) {
            return Integer.parseInt(t.toString());
        }
    };
}
