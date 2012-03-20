/**
 * Copyright (c) 2011-2012 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/test262-junit>
 */
package com.github.anba.test262.environment;

import java.io.IOException;
import java.io.InputStream;

import com.github.anba.test262.util.EcmaErrorMatcher;

/**
 * @author André Bargull
 * 
 */
public interface Environment<GLOBAL extends GlobalObject> {
    void eval(String sourceName, InputStream source) throws IOException;

    GLOBAL global();

    void exit();

    Class<?>[] exceptions();

    EcmaErrorMatcher<? extends Throwable> matcher(String errorType);
}
