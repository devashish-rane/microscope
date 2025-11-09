package com.example.target.scenarios;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/scenarios")
public class ScenarioController {
    private final ChainServices chain;
    private final ChainBService chainB;
    private final FanoutServices fanout;

    public ScenarioController(ChainServices chain, ChainBService chainB, FanoutServices fanout) {
        this.chain = chain;
        this.chainB = chainB;
        this.fanout = fanout;
    }

    // 1) 3-service chain A -> B -> C
    @GetMapping("/chain3")
    public ResponseEntity<String> chain3() {
        String out = chain.a();
        return ResponseEntity.ok("chain:" + out);
    }

    // 2) fanout: one service calls two services and merges results (parallel)
    @GetMapping("/fanout")
    public ResponseEntity<String> fanout() {
        String out = fanout.merge();
        return ResponseEntity.ok("fanout:" + out);
    }

    // 3) callable endpoint: work runs later in MVC async thread
    @GetMapping("/callable")
    public Callable<ResponseEntity<String>> callable() {
        return () -> {
            String out = chainB.b();
            return ResponseEntity.ok("callable:" + out);
        };
    }

    // 4) exception scenario: flow consumed due to exception
    @GetMapping("/exception")
    public ResponseEntity<String> exception() {
        throw new IllegalStateException("simulated failure");
    }

    // 5) async calls using @Async executor (3 threads)
    @GetMapping("/async")
    public ResponseEntity<String> async() {
        CompletableFuture<String> a = fanout.asyncWork("one");
        CompletableFuture<String> b = fanout.asyncWork("two");
        CompletableFuture<String> c = fanout.asyncWork("three");
        String out = a.thenCombine(b, (x, y) -> x + "," + y).thenCombine(c, (xy, z) -> xy + "," + z).join();
        return ResponseEntity.ok(out);
    }
}
