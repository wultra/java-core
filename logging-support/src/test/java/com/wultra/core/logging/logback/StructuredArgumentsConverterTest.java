/*
 * Copyright 2026 Wultra s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wultra.core.logging.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.Test;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link StructuredArgumentsConverter}.
 *
 * @author Vit Kotacka, vit.kotacka@wultra.com
 */
class StructuredArgumentsConverterTest {

    private final StructuredArgumentsConverter converter = new StructuredArgumentsConverter();

    @Test
    void convert_withStructuredArguments_rendersKeyValuePairs() {
        final LoggingEvent event = buildEvent("action: test, state: succeeded",
                kv("processId", "abc-123"), kv("userId", "user-42"));

        assertEquals(" {processId=abc-123, userId=user-42}", converter.convert(event));
    }

    @Test
    void convert_withNoArguments_returnsEmpty() {
        final LoggingEvent event = buildEvent("plain message");
        assertEquals("", converter.convert(event));
    }

    @Test
    void convert_withNullArgumentArray_returnsEmpty() {
        final LoggingEvent event = buildEvent("plain message", (Object[]) null);
        assertEquals("", converter.convert(event));
    }

    @Test
    void convert_withMixedArguments_rendersOnlyStructuredOnes() {
        final LoggingEvent event = buildEvent("msg {}", "plainValue", kv("id", "xyz"));
        assertEquals(" {id=xyz}", converter.convert(event));
    }

    private static LoggingEvent buildEvent(final String message, final Object... args) {
        final LoggerContext ctx = new LoggerContext();
        final Logger logger = ctx.getLogger(StructuredArgumentsConverterTest.class);
        return new LoggingEvent(
                StructuredArgumentsConverterTest.class.getName(),
                logger, Level.INFO, message, null, args);
    }

}
