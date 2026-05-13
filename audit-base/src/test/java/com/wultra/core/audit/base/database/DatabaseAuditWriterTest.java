/*
 * Copyright 2025 Wultra s.r.o.
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
package com.wultra.core.audit.base.database;

import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.AuditFactory;
import com.wultra.core.audit.base.TestApplication;
import com.wultra.core.audit.base.model.AuditDetail;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link DatabaseAuditWriter}.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
class DatabaseAuditWriterTest {

    @SpringBootTest(
            classes = TestApplication.class,
            properties = {
                    "audit.cleanup.cron=0/3 * * * * *",
                    "audit.db.cleanup.days=-1" // time shift to the future to enable cleanup test
            }
    )
    @Sql("/db_schema.sql")
    @Nested
    class ScheduledCleanupOn {

        @Autowired
        private AuditFactory auditFactory;

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @Test
        void testAuditScheduledCleanup() {
            final Audit audit = auditFactory.getAudit();
            audit.info("test message", AuditDetail.builder().param("my_id", "test_id").build());
            audit.flush();

            assertEquals(1, countAuditLogs(jdbcTemplate));

            Awaitility.await()
                    .atMost(Duration.ofSeconds(6))
                    .until(() -> countAuditLogs(jdbcTemplate) == 0);
        }
    }

    @SpringBootTest(
            classes = TestApplication.class,
            properties = {
                    "audit.cleanup.cron=-",
                    "audit.db.cleanup.days=-1" // time shift to the future to enable cleanup test
            }
    )
    @Sql("/db_schema.sql")
    @Nested
    class ScheduledCleanupOff {

        @Autowired
        private AuditFactory auditFactory;

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @Test
        void testAuditScheduledCleanup() {
            final Audit audit = auditFactory.getAudit();
            audit.info("test message", AuditDetail.builder().param("my_id", "test_id").build());
            audit.flush();

            assertEquals(1, countAuditLogs(jdbcTemplate));

            Awaitility.await()
                    .atMost(Duration.ofSeconds(6))
                    .pollInterval(Duration.ofSeconds(5))
                    .until(() -> countAuditLogs(jdbcTemplate) == 1);
        }
    }

    private int countAuditLogs(final JdbcTemplate jdbcTemplate) {
        final SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT COUNT(*) FROM audit_log");
        assertTrue(rs.next());
        return rs.getInt(1);
    }
}
