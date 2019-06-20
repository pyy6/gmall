package com.pyy6.gmall.payment.test;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class ConsumerTopicPeng {
    public static void main(String[] args) {

        ConnectionFactory connect = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,ActiveMQConnection.DEFAULT_PASSWORD,"tcp://localhost:61616");
        try {
            Connection connection = connect.createConnection();
            connection.setClientID("2");//默认topic的消息模式是不会持久化等到被comsumer消费的，因为不知道消费端有多少个consumer还没有消费。
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);//
            Destination testtopic = session.createTopic("KAIHUI");//监听倒水队列

//            MessageConsumer consumer = session.createConsumer(testtopic);
            MessageConsumer consumer = session.createDurableSubscriber((Topic) testtopic,"2");
            consumer.setMessageListener(new MessageListener() {//时时刻刻监听消息，从消息队列拖拽消息进行处理
                @Override
                public void onMessage(Message message) {
                    if(message instanceof TextMessage){
                        try {
                            String text = ((TextMessage) message).getText();//根据消息队列里面消息的类型强壮
                            System.out.println(text+"Peng学习");

                            //session.rollback();
                        } catch (JMSException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
