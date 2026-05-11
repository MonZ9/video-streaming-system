package com.video.mq;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

import java.io.UnsupportedEncodingException;

public class RocketMQProducer {
    private static DefaultMQProducer producer;

    static {
        try {
            producer = new DefaultMQProducer("feed-producer-group");
            producer.setNamesrvAddr("localhost:9876"); // 请确保 NameServer 已启动
            producer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void send(String topic, String tags, String body) {
        try {
            Message msg = new Message(topic, tags, body.getBytes("UTF-8"));
            producer.send(msg);
        } catch (UnsupportedEncodingException e) {
            // UTF-8 是标准编码，实际上不会抛出，仅为编译通过
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        if (producer != null) {
            producer.shutdown();
        }
    }
}