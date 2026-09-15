package me.legrange.typelink;

import me.legrange.typelink.Selector.Type;

public final class Selects {
    private Selects() {
    }

    public static <N extends Number> N sum(N number) {
        return number;
    }

    public static <N extends Number> N max(N number) {
        return number;
    }

    public static <N extends Number> N min(N number) {
        return number;
    }

    public static <N extends Number> N avg(N number) {
        return number;
    }

    public static <T1> SelectFunction1<T1, Long> count() {
        return new Select1<>(Type.COUNT_ROWS, Long.class, null);
    }

    public static <T> Long count(T data) {
        return count().apply(data);
    }

    public static <T1> SelectFunction1<T1, Long> count(SelectFunction1<T1, ?> function) {
        return new Select1<>(Type.COUNT_COLUMN, Long.class, function);
    }

    public static <T1, T2> SelectFunction2<T1, T2, Long> count(SelectFunction2<T1, T2, ?> function) {
        return new Select2<>(Type.COUNT_COLUMN, Long.class, function);
    }

    public static <T1, T2, T3> SelectFunction3<T1, T2, T3, Long> count(SelectFunction3<T1, T2, T3, ?> function) {
        return new  Select3<>(Type.COUNT_COLUMN, Long.class, function);
    }

    public static <T1, N extends Number> SelectFunction1<T1, N> sum(SelectFunction1<T1, N> function) {
        return new Select1<>(Type.SUM_COLUMN, function);
    }

    public static <T1, T2, N extends Number> SelectFunction2<T1, T2, N> sum(SelectFunction2<T1, T2, N> function) {
        return new Select2<>(Type.SUM_COLUMN, function);
    }

    public static <T1, T2, T3, N extends Number> SelectFunction3<T1, T2, T3, N> sum(SelectFunction3<T1, T2, T3, N> function) {
        return new Select3<>(Type.SUM_COLUMN, function);
    }

    public static <T1, N extends Number> SelectFunction1<T1, N> min(SelectFunction1<T1, N> function) {
        return new Select1<>(Type.MIN_COLUMN, function);
    }

    public static <T1, T2, N extends Number> SelectFunction2<T1, T2, N> min(SelectFunction2<T1, T2, N> function) {
        return new Select2<>(Type.MIN_COLUMN, function);
    }

    public static <T1, T2, T3, N extends Number> SelectFunction3<T1, T2, T3, N> min(SelectFunction3<T1, T2, T3, N> function) {
        return new Select3<>(Type.MIN_COLUMN, function);
    }

    public static <T1, N extends Number> SelectFunction1<T1, N> max(SelectFunction1<T1, N> function) {
        return new Select1<>(Type.MAX_COLUMN, function);
    }

    public static <T1, T2, N extends Number> SelectFunction2<T1, T2, N> max(SelectFunction2<T1, T2, N> function) {
        return new Select2<>(Type.MAX_COLUMN, function);
    }

    public static <T1, T2, T3, N extends Number> SelectFunction3<T1, T2, T3, N> max(SelectFunction3<T1, T2, T3, N> function) {
        return new Select3<>(Type.MAX_COLUMN, function);
    }

    public static <T1, N extends Number> SelectFunction1<T1, Double> avg(SelectFunction1<T1, N> function) {
        return new Select1<>(Type.AVG_COLUMN, Double.class, function);
    }

    public static <T1, T2, N extends Number> SelectFunction2<T1, T2, Double> avg(SelectFunction2<T1, T2, N> function) {
        return new Select2<>(Type.AVG_COLUMN, Double.class, function);
    }

    public static <T1, T2, T3, N extends Number> SelectFunction3<T1, T2, T3, Double> avg(SelectFunction3<T1, T2, T3, N> function) {
        return new Select3<>(Type.AVG_COLUMN, Double.class, function);
    }



}
