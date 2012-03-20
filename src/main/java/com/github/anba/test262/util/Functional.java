/**
 * Copyright (c) 2011-2012 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/test262-junit>
 */
package com.github.anba.test262.util;

import java.util.Arrays;
import java.util.Collection;

/**
 * Java8 compatibility shim
 * 
 * @author André Bargull
 * 
 */
public final class Functional {
    private Functional() {
    }

    private static final Predicate<Object> TRUE = new Predicate<Object>() {
        @Override
        public boolean eval(Object t) {
            return true;
        }
    };

    /**
     * The predicate which always returns {@code true}
     */
    @SuppressWarnings("unchecked")
    private static <T> Predicate<T> alwaysTrue() {
        return (Predicate<T>) TRUE;
    }

    /**
     * Predicate interface
     */
    public static interface Predicate<T> {
        boolean eval(T t);
    }

    /**
     * Mapper interface
     */
    public static interface Mapper<T, U> {
        U map(T t);
    }

    /**
     * Simple tuple class
     */
    public static interface Tuple2<T1, T2> {
        T1 _1();

        T2 _2();
    }

    /**
     * Constructs a tuple of the input values
     */
    public static <T1, T2> Tuple2<T1, T2> of(final T1 _1, final T2 _2) {
        return new Tuple2<T1, T2>() {
            @Override
            public T1 _1() {
                return _1;
            }

            @Override
            public T2 _2() {
                return _2;
            }
        };
    }

    /**
     * Returns a new {@link Iterable} for the input rest-argument
     */
    @SafeVarargs
    public static <T> Iterable<T> iterable(T... rest) {
        return Arrays.asList(rest);
    }

    /**
     * Applies the mapper on the input (lazy operation)
     */
    public static <T, U> Iterable<U> map(Iterable<T> base,
            Mapper<? super T, ? extends U> mapper) {
        return new $FilterMap<>(base, Functional.<T> alwaysTrue(), mapper);
    }

    /**
     * Filters and maps the input (lazy operation)
     */
    public static <T, U> Iterable<U> filterMap(Iterable<T> base,
            Predicate<? super T> predicate,
            Mapper<? super T, ? extends U> mapper) {
        return new $FilterMap<>(base, predicate, mapper);
    }

    /**
     * Adds all entries into the specified collection
     */
    public static <C extends Collection<? super T>, T> C intoCollection(
            Iterable<T> itr, C collection) {
        for (T value : itr) {
            collection.add(value);
        }
        return collection;
    }

    /**
     * {@link FilterMap} based on {@link Predicate} and {@link Mapper}
     */
    private static final class $FilterMap<T, U> extends FilterMap<T, U> {
        private final Predicate<? super T> predicate;
        private final Mapper<? super T, ? extends U> mapper;

        public $FilterMap(Iterable<T> base, Predicate<? super T> predicate,
                Mapper<? super T, ? extends U> mapper) {
            super(base);
            this.predicate = predicate;
            this.mapper = mapper;
        }

        @Override
        protected boolean filter(T value) {
            return predicate.eval(value);
        }

        @Override
        protected U map(T value) {
            return mapper.map(value);
        }
    }
}
