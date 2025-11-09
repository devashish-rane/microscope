package com.debugflow.core.exporter;

import com.debugflow.core.event.TraceEvent;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FilePrettyExporter implements Exporter {
    private final Path path;
    private final Object lock = new Object();
    private final DateTimeFormatter tsFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FilePrettyExporter(Path path) {
        this.path = path;
    }

    @Override
    public void export(TraceEvent e) {
        if (!"FLOW".equals(e.getType())) return;
        int depth = e.getDepth() != null ? e.getDepth() : 0;
        String indent = "  ".repeat(Math.max(0, depth));
        String tid = shortId(e.getTraceId());
        String base = String.format("%s  %s [%-8s] %-14s %s",
                tsFmt.format(LocalDateTime.now()), indent,
                tid, safe(e.getService()), safe(e.getOp()));
        String line;
        if ("START".equals(e.getPhase())) {
            // exactly two spaces between method name and arrow; longer arrow "-->"
            line = base + "  -->";
        } else { // END
            long dur = e.getDurMs() != null ? e.getDurMs() : -1;
            String dstr = dur < 0 ? "?ms" : (dur + "ms");
            // exactly two spaces before arrow; use "<--" and keep duration after a single space
            line = base + String.format("  <-- %6s", dstr);
            if (Boolean.TRUE.equals(e.getError())) {
                String err = e.getErrorType() != null ? e.getErrorType() : "ERROR";
                line = line + "  " + err;
            }
        }
        synchronized (lock) {
            try (PrintWriter out = new PrintWriter(new FileWriter(path.toFile(), true))) {
                out.println(line);
            } catch (IOException ex) {
                // best effort: swallow
            }
        }
    }

    private String shortId(String id) { return id == null ? "-" : (id.length() > 8 ? id.substring(0, 8) : id); }
    private String safe(String s) { return s == null ? "-" : s; }
    private String padRight(String s, int n) { return s.length() >= n ? s : s + " ".repeat(n - s.length()); }
}
