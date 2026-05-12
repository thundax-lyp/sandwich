package com.github.thundax.common.mq;

import java.util.LinkedHashMap;
import java.util.Map;

public class SandwishMqMessage {

    private final String topic;
    private final String tag;
    private final String key;
    private final String exchange;
    private final String routingKey;
    private final Object payload;
    private final Map<String, String> headers;

    public SandwishMqMessage(
            String topic,
            String tag,
            String key,
            String exchange,
            String routingKey,
            Object payload,
            Map<String, String> headers) {
        this.topic = topic;
        this.tag = tag;
        this.key = key;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.payload = payload;
        this.headers = headers == null ? new LinkedHashMap<>() : new LinkedHashMap<>(headers);
    }

    public static SandwishMqMessage forTopic(String topic, String key, Object payload) {
        return new SandwishMqMessage(topic, null, key, null, null, payload, null);
    }

    public static SandwishMqMessage forTopicWithTag(String topic, String tag, String key, Object payload) {
        return new SandwishMqMessage(topic, tag, key, null, null, payload, null);
    }

    public static SandwishMqMessage forExchange(String exchange, String routingKey, String key, Object payload) {
        return new SandwishMqMessage(null, null, key, exchange, routingKey, payload, null);
    }

    public static SandwishMqMessage forQueue(String queue, String key, Object payload) {
        return new SandwishMqMessage(queue, null, key, null, queue, payload, null);
    }

    public String getTopic() {
        return topic;
    }

    public String getTag() {
        return tag;
    }

    public String getKey() {
        return key;
    }

    public String getExchange() {
        return exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public Object getPayload() {
        return payload;
    }

    public Map<String, String> getHeaders() {
        return new LinkedHashMap<>(headers);
    }

    public SandwishMqMessage withHeader(String name, String value) {
        LinkedHashMap<String, String> updatedHeaders = new LinkedHashMap<>(headers);
        if (value == null) {
            updatedHeaders.remove(name);
        } else {
            updatedHeaders.put(name, value);
        }
        return new SandwishMqMessage(topic, tag, key, exchange, routingKey, payload, updatedHeaders);
    }
}
