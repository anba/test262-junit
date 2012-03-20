/**
 * Copyright (c) 2011-2012 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/test262-junit>
 */
package com.github.anba.test262.util;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

/**
 * {@link ErrorReporter} implementation which collects all errors reported to
 * {@link ErrorReporter#error(String, String, int, String, int)}. Reported
 * errors are available through {@link CollectingErrorReporter#getErrors()}. Any
 * warnings reported to
 * {@link ErrorReporter#warning(String, String, int, String, int)} are ignored.
 * Runtime errors reported to
 * {@link ErrorReporter#runtimeError(String, String, int, String, int)} are
 * re-thrown as {@link EvaluatorException}s.
 * 
 * @author André Bargull
 * 
 */
public class CollectingErrorReporter implements ErrorReporter {
    private List<EvaluatorException> errors = new ArrayList<>();

    @Override
    public void warning(String message, String sourceName, int line,
            String lineSource, int lineOffset) {
    }

    @Override
    public EvaluatorException runtimeError(String message, String sourceName,
            int line, String lineSource, int lineOffset) {
        return new EvaluatorException(message, sourceName, line, lineSource,
                lineOffset);
    }

    @Override
    public void error(String message, String sourceName, int line,
            String lineSource, int lineOffset) {
        errors.add(new EvaluatorException(message, sourceName, line,
                lineSource, lineOffset));
    }

    /**
     * Returns the collected errors
     */
    public List<EvaluatorException> getErrors() {
        return errors;
    }
}
