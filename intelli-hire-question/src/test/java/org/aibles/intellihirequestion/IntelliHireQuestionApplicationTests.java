package org.aibles.intellihirequestion;

import org.aibles.intellihirequestion.config.RedisTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(RedisTestConfig.class)
class IntelliHireQuestionApplicationTests {

    @Test
    void contextLoads() {
    }
}
