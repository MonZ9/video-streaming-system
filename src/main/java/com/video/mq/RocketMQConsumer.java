package com.video.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.video.core.BeanFactory;
import com.video.dao.FollowDao;
import com.video.util.RedisUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

public class RocketMQConsumer {
    public static void start() throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("feed-consumer-group");
        consumer.setNamesrvAddr("localhost:9876");
        consumer.subscribe("feed-update", "*");

        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                try {
                    String body = new String(msg.getBody(), "UTF-8");
                    JSONObject obj = JSON.parseObject(body);
                    int userId = obj.getIntValue("userId");

                    // 获取粉丝列表（确保 BeanFactory 已初始化）
                    FollowDao followDao = BeanFactory.getBean(FollowDao.class);
                    List<Integer> followers = followDao.getFollowersList(userId);

                    // 清除每个粉丝的 Feed 首页缓存
                    for (int fid : followers) {
                        RedisUtil.del("feed:user:" + fid + ":first");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        consumer.start();
    }
}