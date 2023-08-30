package com.mqtt.demo;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttExample {
    /**
     * 1. MqttClient 类：
     * - `MqttClient(String serverURI, String clientId)`：使用指定的 MQTT 代理服务器 URI 和客户端标识符创建一个 MQTT 客户端实例。
     * - `connect()`：连接到 MQTT 代理服务器。
     * - `disconnect()`：断开与 MQTT 代理服务器的连接。
     * - `subscribe(String topicFilter, int qos)`：订阅一个主题，并指定服务质量（QoS）等级。
     * - `unsubscribe(String topicFilter)`：取消订阅一个主题。
     * - `publish(String topic, MqttMessage message)`：发布一条 MQTT 消息到指定的主题。
     * 2. MqttCallback 接口：
     * - `connectionLost(Throwable cause)`：当与 MQTT 代理服务器的连接丢失时调用。
     * - `messageArrived(String topic, MqttMessage message)`：当收到 MQTT 消息时调用。
     * - `deliveryComplete(IMqttDeliveryToken token)`：当消息成功发送到 MQTT 代理服务器时调用。
     * 3. MqttMessage 类：
     * - `MqttMessage(byte[] payload)`：使用指定的字节数组创建一个 MQTT 消息对象。
     * - `setQos(int qos)`：设置消息的服务质量（QoS）等级。
     * - `setRetained(boolean retained)`：设置是否将消息保留在 MQTT 代理服务器上。
     */

    public static void main(String[] args) {
        String broker = "tcp://broker.hivemq.com:1883";
        String clientId = "JavaMqttClient";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            // 创建 MQTT 客户端连接
            MqttClient client = new MqttClient(broker, clientId, persistence);

            // 设置回调函数来处理收到的消息
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("连接断开: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println(String.format("收到消息: %s; 主题: %s", new String(message.getPayload()), topic));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("消息已发送成功");
                }
            });

            // 连接到 MQTT 代理服务器
            client.connect();

            // 订阅主题
            String topic = "test/topic";
            int qos = 1;
            client.subscribe(topic, qos);
            System.out.println("已订阅主题：" + topic);

            // 发布消息
            String messageStr = "Hello MQTT!";
            MqttMessage message = new MqttMessage(messageStr.getBytes());
            message.setQos(qos);//设置服务质量 0，1，2
            client.publish(topic, message);
            System.out.println("已发布消息：" + messageStr);

            // 等待 5 秒钟，然后断开连接
            Thread.sleep(5000);
            client.disconnect();
            System.out.println("已断开连接");
        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
