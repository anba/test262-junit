/**
 * Copyright (c) 2011-2012 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/test262-junit>
 */
package com.github.anba.test262.environment;

import static com.github.anba.test262.util.Functional.of;
import static com.github.anba.test262.util.Reflection.__new__;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Evaluator;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.ScriptNode;

import com.github.anba.test262.util.CollectingErrorReporter;
import com.github.anba.test262.util.EcmaErrorMatcher;
import com.github.anba.test262.util.Functional.Tuple2;

/**
 * 
 * @author André Bargull
 * 
 */
abstract class RhinoEnv<GLOBAL extends Scriptable & GlobalObject> implements
        Environment<GLOBAL> {
    private Context context() {
        return Context.getCurrentContext();
    }

    private final CompilerEnvirons compilerEnv;
    {
        compilerEnv = new CompilerEnvirons();
        compilerEnv.initFromContext(context());
        // compilerEnv.setIdeMode(true);
        // compilerEnv.setRecordingComments(true);
        // compilerEnv.setRecoverFromErrors(true);
    }

    @Override
    public abstract GLOBAL global();

    protected abstract String getEvaluator();

    protected abstract String getCharsetName();

    @Override
    public Class<?>[] exceptions() {
        return new Class[] { EcmaError.class, EvaluatorException.class,
                JavaScriptException.class };
    }

    @Override
    public EcmaErrorMatcher<RhinoException> matcher(String errorType) {
        return new EcmaErrorMatcher<RhinoException>() {
            @Override
            public boolean matches(RhinoException error, String errorType) {
                // errorType is now a regular expression
                Pattern p = Pattern
                        .compile(errorType, Pattern.CASE_INSENSITIVE);
                String name;
                if (error instanceof EcmaError) {
                    name = ((EcmaError) error).getName();
                } else if (error instanceof JavaScriptException) {
                    Object value = ((JavaScriptException) error).getValue();
                    if (value != null
                            && value.getClass()
                                    .getName()
                                    .equals("org.mozilla.javascript.NativeError")) {
                        Object message = ((ScriptableObject) value)
                                .get("message");
                        name = ScriptRuntime.toString(message);
                    } else {
                        name = error.details();
                    }
                } else {
                    name = "";
                }

                return p.matcher(name).find();
                // return errorType.equals(name);
            }

            @Override
            public Class<? extends RhinoException> exception() {
                return RhinoException.class;
            }
        };
    }

    /**
     * Parses, compiles and executes the javascript file
     */
    @Override
    public void eval(String sourceName, InputStream source) throws IOException {
        Reader reader = newReader(source, getCharsetName());
        Tuple2<AstRoot, List<EvaluatorException>> parsed = parse(sourceName,
                reader);
        if (!parsed._2().isEmpty()) {
            throw parsed._2().get(0);
        }
        execute(parsed._1());
    }

    /**
     * Parses the test file with the current settings
     */
    protected final Tuple2<AstRoot, List<EvaluatorException>> parse(
            String sourceName, Reader source) throws IOException {
        CollectingErrorReporter errorCollector = new CollectingErrorReporter();
        Parser p = new Parser(compilerEnv, errorCollector);
        AstRoot ast = p.parse(source, sourceName, 1);
        return of(ast, errorCollector.getErrors());
    }

    /**
     * Executes the parsed AST with the current settings
     */
    protected final Object execute(AstRoot ast) {
        Script script = compile(ast);
        return script.exec(context(), global());
    }

    /**
     * Compiles {@code ast} with the current settings and returns the created
     * {@link Script}
     */
    protected final Script compile(AstRoot ast) {
        IRFactory irf = new IRFactory(compilerEnv);
        ScriptNode tree = irf.transformTree(ast);
        Evaluator compiler = __new__(getEvaluator());
        Object bytecode = compiler.compile(compilerEnv, tree,
                tree.getEncodedSource(), false);
        Script script = compiler.createScriptObject(bytecode, null);
        return script;
    }

    /**
     * Returns a new {@link Reader} for the {@code stream} parameter
     */
    private static Reader newReader(InputStream stream, String defaultCharset)
            throws IOException {
        BOMInputStream bomstream = new BOMInputStream(stream,
                ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE,
                ByteOrderMark.UTF_16BE);
        String charset = defaultIfNull(bomstream.getBOMCharsetName(),
                defaultCharset);
        return new InputStreamReader(bomstream, charset);
    }
}