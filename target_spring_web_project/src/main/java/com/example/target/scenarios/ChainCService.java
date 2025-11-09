package com.example.target.scenarios;

import org.springframework.stereotype.Service;

@Service
public class ChainCService {
    public String c() {
        sleep(10);
        return "C";
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { }
    }
}

