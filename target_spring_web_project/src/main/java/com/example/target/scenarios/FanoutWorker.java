package com.example.target.scenarios;

import org.springframework.stereotype.Service;

@Service
public class FanoutWorker {
    public String alpha() {
        sleep(15);
        return "A";
    }
    public String beta() {
        sleep(20);
        return "B";
    }
    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { }
    }
}

