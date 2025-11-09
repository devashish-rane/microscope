package com.debugflow.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "debugflow")
public class DebugFlowProperties {
    /** Default enable state on startup. Runtime toggle still possible via API. */
    private boolean enabled = false;
    /** Default TTL in minutes when session enabled. */
    private int ttlMinutes = 20;
    /** Optional cap for number of traces stored (not used in MVP). */
    private int maxTraces = 500;
    /** Emit compact JSON lines to console logger. */
    private boolean consoleJson = true;
    /** Emit pretty, colored single-line console output. */
    private boolean consolePretty = false;
    /** Enable ANSI colors in pretty output. */
    private boolean color = true;
    /** Optional path to write pretty lines to a file (appended). */
    private String prettyFile;
    /** Show thread name at end of line in pretty/file output. */
    private boolean showThread = false;
    /** Show HTTP method/path/status when available (controller spans). */
    private boolean showHttp = true;
    /** Use microseconds for very short durations (<1ms). */
    private boolean micros = true;
    /** Render simple class names (strip package) in op. */
    private boolean simpleClassNames = false;
    /** Log flows when an inbound traceparent/traceId is present even if session is disabled. */
    private boolean followInboundTraces = true;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getTtlMinutes() { return ttlMinutes; }
    public void setTtlMinutes(int ttlMinutes) { this.ttlMinutes = ttlMinutes; }

    public int getMaxTraces() { return maxTraces; }
    public void setMaxTraces(int maxTraces) { this.maxTraces = maxTraces; }

    public boolean isConsoleJson() { return consoleJson; }
    public void setConsoleJson(boolean consoleJson) { this.consoleJson = consoleJson; }

    public boolean isConsolePretty() { return consolePretty; }
    public void setConsolePretty(boolean consolePretty) { this.consolePretty = consolePretty; }

    public boolean isColor() { return color; }
    public void setColor(boolean color) { this.color = color; }

    public String getPrettyFile() { return prettyFile; }
    public void setPrettyFile(String prettyFile) { this.prettyFile = prettyFile; }

    public boolean isShowThread() { return showThread; }
    public void setShowThread(boolean showThread) { this.showThread = showThread; }

    public boolean isShowHttp() { return showHttp; }
    public void setShowHttp(boolean showHttp) { this.showHttp = showHttp; }

    public boolean isMicros() { return micros; }
    public void setMicros(boolean micros) { this.micros = micros; }

    public boolean isSimpleClassNames() { return simpleClassNames; }
    public void setSimpleClassNames(boolean simpleClassNames) { this.simpleClassNames = simpleClassNames; }

    public boolean isFollowInboundTraces() { return followInboundTraces; }
    public void setFollowInboundTraces(boolean followInboundTraces) { this.followInboundTraces = followInboundTraces; }
}
