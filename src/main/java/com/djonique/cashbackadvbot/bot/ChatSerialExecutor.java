package com.djonique.cashbackadvbot.bot;

import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Выполняет задачи одного чата последовательно на общем пуле потоков.
 * Это сохраняет порядок апдейтов пользователя и не блокирует остальных.
 */
@Slf4j
public final class ChatSerialExecutor implements Executor {

    private final Executor delegate;
    private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public ChatSerialExecutor(Executor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(Runnable command) {
        tasks.add(command);
        scheduleNext();
    }

    private void scheduleNext() {
        if (running.compareAndSet(false, true)) {
            delegate.execute(this::drain);
        }
    }

    private void drain() {
        while (true) {
            Runnable task = tasks.poll();
            if (task == null) {
                running.set(false);
                if (!tasks.isEmpty() && running.compareAndSet(false, true)) {
                    delegate.execute(this::drain);
                }
                return;
            }
            try {
                task.run();
            } catch (RuntimeException e) {
                log.error("Ошибка выполнения задачи чата", e);
            }
        }
    }
}
