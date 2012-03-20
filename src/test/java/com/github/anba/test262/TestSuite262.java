/**
 * Copyright (c) 2011-2012 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/test262-junit>
 */
package com.github.anba.test262;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.anba.test262.Test262;

/**
 * The standard test262 test suite only includes the tests from the "test/suite"
 * directory
 * 
 * @author André Bargull
 * 
 */
@RunWith(Suite.class)
@SuiteClasses({ Test262.class })
public final class TestSuite262 {
}
