package com.example.target.scenarios;

import org.springframework.stereotype.Service;

@Service
public class ChainServices { // acts as A
    private final ChainBService chainB;

    public ChainServices(ChainBService chainB) {
        this.chainB = chainB;
    }

    public String a() {
        sleep(10);
        return chainB.b();
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { }
    }
}
