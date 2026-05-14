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

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.argument.StructuredArgument;

/**
 * Logback {@link ClassicConverter} that renders {@link StructuredArgument} instances
 * (e.g. from {@code StructuredArguments.kv()}) in plain-text log output.
 *
 * <p>Usage in {@code logback.xml}:
 * <pre>{@code
 * <conversionRule conversionWord="sa"
 *     converterClass="com.wultra.core.logging.logback.StructuredArgumentsConverter"/>
 * <!-- Pattern example (no space before %sa — the converter prepends its own space) -->
 * <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%sa%n</pattern>
 * }</pre>
 *
 * <p>Output example:
 * <pre>
 * 10:35:22.123 [main] INFO  c.w.EnrollmentService - action: createIdentity, state: succeeded {processId=abc-123, identityVerificationId=def-456}
 * </pre>
 *
 * <p>When no structured arguments are present, an empty string is returned so no trailing
 * whitespace appears in the log line.
 *
 * @author Vit Kotacka, vit.kotacka@wultra.com
 */
public class StructuredArgumentsConverter extends ClassicConverter {

    @Override
    public String convert(final ILoggingEvent event) {
        final Object[] args = event.getArgumentArray();
        if (args == null || args.length == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (final Object arg : args) {
            if (arg instanceof StructuredArgument) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(arg);
            }
        }
        return sb.length() == 0 ? "" : " {" + sb + "}";
    }

}
