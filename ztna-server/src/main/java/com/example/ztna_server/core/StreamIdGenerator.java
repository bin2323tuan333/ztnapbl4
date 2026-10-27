package com.example.ztna_server.core;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class StreamIdGenerator {

    private final AtomicInteger counter = new AtomicInteger(0);

    public int next() {
        int id = counter.incrementAndGet();

        if (id <= 0) {
            counter.set(1);
            id = 1;
        }

        return id;
    }
}