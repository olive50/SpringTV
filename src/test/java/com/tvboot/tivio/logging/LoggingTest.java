package com.tvboot.tivio.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.tvboot.tivio.service.TvChannelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class LoggingTest {

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(TvChannelService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    void shouldLogDebugMessages() {
        logger.debug("Test debug message");

        assertThat(listAppender.list)
                .hasSize(1)
                .extracting(ILoggingEvent::getMessage)
                .contains("Test debug message");
    }

    @Test
    void shouldLogWithMDC() {
        org.slf4j.MDC.put("traceId", "TEST123");
        logger.info("Test message with MDC");

        ILoggingEvent event = listAppender.list.get(0);
        assertThat(event.getMDCPropertyMap()).containsEntry("traceId", "TEST123");
    }
}