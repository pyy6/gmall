package com.pyy6.gmall.payment.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class ProducerBossTopic {
    public static void main(String[] args) {
        //生成某个地址下面的连接池
        ConnectionFactory connect = new ActiveMQConnectionFactory("tcp://localhost:61616");
        try {
            //建立mq的连接
            Connection connection = connect.createConnection();
            connection.start();
            //通过连接创建一次与mq的会话任务
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);//可以保证分布式事务，如果某个comsumer没有真正的消费成功这个消息，他会将这个消息放回队列被其他comsumer消费。
            Topic testtopic = session.createTopic("KAIHUI");
            //通过mq的会话任务将消息队列中的消息发送出去
            MessageProducer producer = session.createProducer(testtopic);
            TextMessage textMessage=new ActiveMQTextMessage();
            textMessage.setText("为中华崛而努力奋斗！！！");
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);//哪怕是发送消息时没有一个comsumer，但是这条消息总是会被执行的
            producer.send(textMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
}
