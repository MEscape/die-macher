package com.die_macher.common.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class QueueManager<K> {
    private final Map<K, Queue<Runnable>> queueMap;

    public QueueManager() {
        this.queueMap = new HashMap<>();
    }

    public void addQueue(K key) {
        queueMap.putIfAbsent(key, new LinkedList<>());
    }

    public void enqueue(K key, Runnable command) {
        queueMap.computeIfAbsent(key, k -> new LinkedList<>()).offer(command);
    }

    public void executeNext(K key) {
        Queue<Runnable> queue = queueMap.get(key);
        if (queue != null) {
            Runnable command = queue.poll();
            if (command != null) {
                command.run();
            }
        }
    }

    public boolean isQueueEmpty(K key) {
        Queue<Runnable> queue = queueMap.get(key);
        return queue == null || queue.isEmpty();
    }
}
