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
    private final boolean showThread;
    private final boolean showHttp;
    private final boolean micros;
    private final boolean simpleClassNames;

    public FilePrettyExporter(Path path) {
        this(path, false, true, true, false);
    }

    public FilePrettyExporter(Path path, boolean showThread, boolean showHttp, boolean micros, boolean simpleClassNames) {
        this.path = path;
        this.showThread = showThread;
        this.showHttp = showHttp;
        this.micros = micros;
        this.simpleClassNames = simpleClassNames;
    }

    @Override
    public void export(TraceEvent e) {
        if ("SQL".equals(e.getType())) {
            int depth = e.getDepth() != null ? e.getDepth() : 0;
            String indent = "  ".repeat(Math.max(0, depth));
            String tid = shortId(e.getTraceId());
            String sql = (e instanceof com.debugflow.core.event.SqlEvent se) ? se.getSql() : null;
            Integer rows = (e instanceof com.debugflow.core.event.SqlEvent se2) ? se2.getRows() : null;
            String snippet = sql == null ? "SQL" : truncate(sql, 120);
            String dur = formatDuration(e);
            String line = String.format("%s  %s [%-8s] %-14s SQL %s  %s", tsFmt.format(LocalDateTime.now()), indent, tid, safe(e.getService()), snippet, dur);
            if (rows != null) line = line + " rows=" + rows;
            write(line);
            return;
        }
        if (!"FLOW".equals(e.getType())) return;
        int depth = e.getDepth() != null ? e.getDepth() : 0;
        String indent = "  ".repeat(Math.max(0, depth));
        String tid = shortId(e.getTraceId());
        String op = e.getOp();
        if (simpleClassNames && op != null) {
            int hash = op.lastIndexOf('#');
            if (hash > 0) {
                int dot = op.lastIndexOf('.', hash);
                if (dot > 0) op = op.substring(dot + 1);
            }
        }
        String base = String.format("%s  %s [%-8s] %-14s %s",
                tsFmt.format(LocalDateTime.now()), indent,
                tid, safe(e.getService()), safe(op));
        String line;
        if ("START".equals(e.getPhase())) {
            // exactly two spaces between method name and arrow; longer arrow "-->"
            line = base + "  -->";
        } else { // END
            String dstr = formatDuration(e);
            // exactly two spaces before arrow; use "<--" and keep duration after a single space
            line = base + String.format("  <-- %6s", dstr);
            if (Boolean.TRUE.equals(e.getError())) {
                String err = e.getErrorType() != null ? e.getErrorType() : "ERROR";
                if (e.getErrorMsg() != null) err = err + ": " + e.getErrorMsg();
                line = line + "  " + err;
            }
            if (showHttp && e.getHttpStatus() != null) {
                line = line + "  " + e.getHttpStatus();
            }
        }
        if (showThread && e.getThread() != null) {
            line = line + "  [" + e.getThread() + "]";
        }
        write(line);
    }

    private String shortId(String id) { return id == null ? "-" : (id.length() > 8 ? id.substring(0, 8) : id); }
    private String safe(String s) { return s == null ? "-" : s; }
    private String padRight(String s, int n) { return s.length() >= n ? s : s + " ".repeat(n - s.length()); }
    private String formatDuration(TraceEvent e) {
        Long ms = e.getDurMs();
        Long ns = e.getDurNs();
        if (ms != null && ms > 0) return ms + "ms";
        if (micros && ns != null) {
            long us = ns / 1_000;
            return us + "µs";
        }
        return ms == null ? "?ms" : (ms + "ms");
    }
    private String truncate(String s, int n) { return s.length() > n ? s.substring(0, n - 1) + "…" : s; }
    private void write(String line) {
        // Inter-process safe append using FileChannel + FileLock
        synchronized (lock) {
            java.nio.channels.FileChannel ch = null;
            try {
                java.nio.file.Path p = this.path;
                java.nio.file.Files.createDirectories(p.getParent() != null ? p.getParent() : p.toAbsolutePath().getParent());
                ch = java.nio.channels.FileChannel.open(p,
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.WRITE,
                        java.nio.file.StandardOpenOption.APPEND);
                try (java.nio.channels.FileLock ignored = ch.lock()) {
                    byte[] bytes = (line + System.lineSeparator()).getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    java.nio.ByteBuffer buf = java.nio.ByteBuffer.wrap(bytes);
                    while (buf.hasRemaining()) ch.write(buf);
                }
            } catch (IOException ex) {
                // best effort
            } finally {
                if (ch != null) try { ch.close(); } catch (IOException ignored) {}
            }
        }
    }
}
