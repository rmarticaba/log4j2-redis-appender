package com.vlkan.log4j2.redis.appender;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.assertj.core.api.Assertions;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import redis.embedded.RedisServer;


public class RedisAppenderReconnectTest {

    private static final DebugLogger LOGGER =
            new DebugLogger(RedisAppenderReconnectTest.class);

    private static final RedisServerResource REDIS_SERVER_RESOURCE =
            new RedisServerResource(
                    RedisAppenderTestConfig.REDIS_PORT,
                    RedisAppenderTestConfig.REDIS_PASSWORD);

    private static final LoggerContextResource LOGGER_CONTEXT_RESOURCE =
            new LoggerContextResource(
                    RedisAppenderTestConfig.LOG4J2_CONFIG_FILE_URI);

    @ClassRule
    public static final RuleChain RULE_CHAIN = RuleChain
            .outerRule(REDIS_SERVER_RESOURCE)
            .around(LOGGER_CONTEXT_RESOURCE);

    @Test
    public void test_reconnect() throws InterruptedException {
        LoggerContext loggerContext = LOGGER_CONTEXT_RESOURCE.getLoggerContext();
        RedisServer redisServer = REDIS_SERVER_RESOURCE.getRedisServer();
        try {
            Logger logger = loggerContext.getLogger(RedisAppenderReconnectTest.class.getCanonicalName());
            append(logger, "append should succeed");
            Thread.sleep(2_000);
            LOGGER.debug("stopping server");
            redisServer.stop();
            try {
                append(logger, "append should fail silently");
                Thread.sleep(2_000);
                append(logger, "append should fail loudly");
                throw new IllegalStateException("should not have reached here");
            } catch (Throwable error) {
                Assertions.assertThat(error).isInstanceOf(AppenderLoggingException.class);
                Assertions.assertThat(error.getCause()).isNotNull();
                Assertions.assertThat(error.getCause()).hasMessageContaining("Unexpected end of stream.");
                LOGGER.debug("starting server");
                redisServer.start();
                append(logger, "append should succeed again");
            }
        } finally {
            LOGGER.debug("finally stopping server");
            redisServer.stop();
        }
    }

    private static void append(Logger logger, String message) {
        LOGGER.debug("trying to append: %s", message);
        logger.info(message);
    }

}
