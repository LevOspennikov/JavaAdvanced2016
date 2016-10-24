package ru.ifmo.ctddev.ospennikov.ParallelMapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    private final Thread[] threads;
    private final Deque<Runnable> queue = new LinkedList<>();

    public ParallelMapperImpl(int threadsCount) {
       threads = new Thread[threadsCount];
        for (int i = 0; i < threadsCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    Runnable target;
                    while (!Thread.interrupted()) {
                        synchronized (queue) {
                            while (queue.isEmpty()) {
                                queue.wait();
                            }
                            target = queue.poll();
                        }
                        if (target != null) {
                            target.run();
                        }
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }


            });
            threads[i].start();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        List<R> answer = new ArrayList<>();
        for (int i = 0; i < args.size(); i++){
            answer.add(null);
        }
        AtomicInteger readyCount = new AtomicInteger(0);
        for (int i = 0; i < args.size(); i++) {
            int effI = i;
            synchronized (queue) {
                queue.push(() -> {
                    R result = f.apply(args.get(effI));
                    synchronized (queue) {
                        answer.set(effI, result);
                        if (readyCount.incrementAndGet() == answer.size()) {
                            queue.notifyAll();
                        }
                    }
                });
                queue.notifyAll();
            }
        }
        synchronized (queue) {
            if (readyCount.get() < answer.size()) {
                queue.wait();
            }
        }
        return answer;
    }

    @Override
    public void close() throws InterruptedException {
        for (Thread thread : threads) {
            thread.interrupt();
            thread.join();
        }
    }
}
