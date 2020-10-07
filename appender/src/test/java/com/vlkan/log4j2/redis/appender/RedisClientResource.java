package com.vlkan.log4j2.redis.appender;

import org.junit.rules.ExternalResource;
import redis.clients.jedis.Jedis;

class RedisClientResource extends ExternalResource {

    private final String host;

    private final int port;

    private final String password;

    private final Jedis client;

    RedisClientResource(String host, int port, String password) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.client = new Jedis(host, port);
    }

    @Override
    protected void before() {
        client.connect();
        client.auth(password);
    }

    @Override
    protected void after() {
        client.disconnect();
    }

    Jedis getClient() {
        return client;
    }

    @Override
    public String toString() {
        return String.format("RedisClientResource{host=%s, port=%d}", host, port);
    }

}
