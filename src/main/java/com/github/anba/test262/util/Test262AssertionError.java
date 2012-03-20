/**
 * Copyright (c) 2011-2012 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/test262-junit>
 */
package com.github.anba.test262.util;

/**
 * 
 * @author André Bargull
 * 
 */
public class Test262AssertionError extends AssertionError {
    private static final long serialVersionUID = -727900497296323773L;

    public Test262AssertionError(Object detailMessage) {
        super(detailMessage);
    }
}
