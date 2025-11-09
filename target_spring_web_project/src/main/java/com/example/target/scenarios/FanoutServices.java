package com.example.target.scenarios;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class FanoutServices {
    private final Executor appExecutor;
    private final FanoutWorker worker;

    public FanoutServices(Executor appExecutor, FanoutWorker worker) {
        this.appExecutor = appExecutor;
        this.worker = worker;
    }

    public String merge() {
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(worker::alpha, appExecutor);
        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(worker::beta, appExecutor);
        return f1.thenCombine(f2, (a, b) -> a + "+" + b).join();
    }

    @Async("appExecutor")
    public CompletableFuture<String> asyncWork(String name) {
        sleep(25);
        return CompletableFuture.completedFuture("async-" + name);
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { }
    }
}
