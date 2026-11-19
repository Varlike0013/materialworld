package com.varlike.aetherui.api.binding;

import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface Bindable<T> {

    T get();

    default <R> Bindable<R> map(Function<? super T, ? extends R> mapper) {
        return () -> mapper.apply(get());
    }

    static <T> Bindable<T> of(T value) {
        return () -> value;
    }

    static <T> Bindable<T> supplier(Supplier<T> supplier) {
        return supplier::get;
    }
}