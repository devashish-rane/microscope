package com.example.target.scenarios;

import org.springframework.stereotype.Service;

@Service
public class ChainBService {
    private final ChainCService chainC;

    public ChainBService(ChainCService chainC) {
        this.chainC = chainC;
    }

    public String b() {
        sleep(10);
        return chainC.c();
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { }
    }
}

