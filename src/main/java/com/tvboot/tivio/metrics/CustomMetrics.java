package com.tvboot.tivio.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomMetrics {

    private final MeterRegistry meterRegistry;

    // Compteurs
    public void incrementChannelCreated() {
        Counter.builder("tvboot.channels.created.total")
                .description("Total number of TV channels created")
                .register(meterRegistry)
                .increment();
    }

    public void incrementAuthenticationAttempt(String result) {
        Counter.builder("tvboot.auth.attempts.total")
                .description("Total authentication attempts")
                .tag("result", result)
                .register(meterRegistry)
                .increment();
    }

    public void incrementApiCall(String endpoint, String method, String status) {
        Counter.builder("tvboot.api.calls.total")
                .description("Total API calls")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    // Timers
    public Timer.Sample startDatabaseTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordDatabaseQuery(Timer.Sample sample, String operation) {
        sample.stop(Timer.builder("tvboot.database.queries.duration")
                .description("Database query execution time")
                .tag("operation", operation)
                .register(meterRegistry));
    }
}