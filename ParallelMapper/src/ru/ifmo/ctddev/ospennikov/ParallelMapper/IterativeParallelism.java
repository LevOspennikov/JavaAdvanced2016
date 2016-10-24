/**
 * Created by ospen_000 on 21.03.2016.
 */
package ru.ifmo.ctddev.ospennikov.ParallelMapper;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.*;

class SubListRun<E, T> {
    private final List<? extends E> list;
    private final int sizeOfSubList;
    private int threadCount;
    private final List<Thread> threads;
    private final BinaryOperator<T> binaryOperator;
    private final Function<Void, T> getNeutral;
    private final BiFunction<T, ? super E, T> biFunction;
    private String debug = "";
    private final ParallelMapper parallelMapper;


    SubListRun(ParallelMapper parallelMapper, int threadCount, List<? extends E> list, BinaryOperator<T> operation, Function<Void, T> getNeutral, BiFunction<T, ? super E, T> biFunction) {
        this.parallelMapper = parallelMapper;

        this.threadCount = threadCount;
        this.list = list;

        sizeOfSubList = Integer.max(1, (list.size() + 1) / this.threadCount);
        threads = new ArrayList<>(this.threadCount);
        binaryOperator = operation;
        this.getNeutral = getNeutral;
        this.biFunction = biFunction;

    }

    private static void ensureSize(ArrayList<?> list, int size) {
        list.ensureCapacity(size);
        while (list.size() < size) {
            list.add(null);
        }
    }

    protected T apply() {
        threadCount -= (sizeOfSubList * threadCount - list.size()) / sizeOfSubList;

        ArrayList<T> result = new ArrayList<>();
        ensureSize(result, threadCount);
        Function<List<? extends E>, T> function = null;
        List<List <? extends E> > subLists = new ArrayList<>(threadCount);
        if (parallelMapper != null){
            function = (subList) -> subList.stream().reduce(getNeutral.apply(null), biFunction, binaryOperator);
        }
        for (int i = 0; i < threadCount; i++) {
            int effI = i;

            List<? extends E> subList = list.subList(sizeOfSubList * i, threadCount - 1 == i? list.size() : Integer.min(list.size(), sizeOfSubList * (i + 1)));
            if (parallelMapper != null){
                subLists.add(subList);
            } else {
                threads.add(new Thread(() -> {
                    subList.size();
                    final T equal = subList.stream().reduce(getNeutral.apply(null), biFunction, binaryOperator);
                    synchronized (SubListRun.class) {
                        result.set(effI, equal);
                    }

                }));
                threads.get(i).start();
            }
        }
        if ( parallelMapper != null){
            try {
                List<T> tmpRes = parallelMapper.map(function, subLists);
                return tmpRes.stream().reduce(getNeutral.apply(null), binaryOperator);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        for (int i = 0; i < threadCount; i++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result.stream().reduce(getNeutral.apply(null), binaryOperator);
    }
}

public class IterativeParallelism implements ScalarIP, ListIP {

    private final ParallelMapper mapper;

    public IterativeParallelism() {
        this.mapper = null;
    }

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        BinaryOperator<T> maximum = (a, b) -> comparator.compare(a, b) >= 0 ? a : b;
        return new SubListRun<T, T>(mapper, threads, values, maximum, (x) -> values.get(0), maximum).apply();
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        BinaryOperator<T> minimum = (a, b) -> comparator.compare(a, b) > 0 ? b : a;
        return new SubListRun<T, T>(mapper, threads, values, minimum, (x)-> values.get(0), minimum).apply();
    }


    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        BiFunction<Boolean, T, Boolean> all = (a, b) -> a && predicate.test(b);
        return new SubListRun<T, Boolean>(mapper, threads, values, (a, b) -> a && b, (x)-> true , all).apply();
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        BiFunction<Boolean, T, Boolean> all = (a, b) -> a || predicate.test(b);
        return new SubListRun<T, Boolean>(mapper, threads, values, (a, b) -> a || b, (x)-> false, all).apply();
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        BiFunction<String, Object, String> join = (a, b) -> a + b;
        return new SubListRun<Object, String>(mapper, threads, values, (a, b)-> a + b, (x) -> "", join).apply();
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        BiFunction<List<T>, T, List<T>> filter = (a, b) -> {
            if (predicate.test(b)) a.addAll(Collections.singletonList(b));
            return a;
        };
        return new SubListRun<T, List<T>>(mapper, threads, values, (a, b) -> {
            a.addAll(b);
            return a;
        }, (x)->(new ArrayList<>()), filter).apply();
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> function) throws InterruptedException {
        BiFunction<List<U>, T, List<U>> bi = (a, b) -> {
            a.add(function.apply(b));
            return a;
        };
        return new SubListRun<T, List<U>>(mapper, threads, values, (a, b) -> {
            a.addAll(b);
            return a;
        }, (x)->(new ArrayList<>()), bi).apply();
    }


}
