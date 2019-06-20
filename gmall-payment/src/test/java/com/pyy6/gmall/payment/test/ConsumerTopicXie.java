package com.pyy6.gmall.payment.test;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class ConsumerTopicXie {
    public static void main(String[] args) {
        ConnectionFactory connect = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,ActiveMQConnection.DEFAULT_PASSWORD,"tcp://localhost:61616");
        try {
            Connection connection = connect.createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);//
            Destination testtopic = session.createTopic("KAIHUI");//监听倒水队列,监听消息的时候也需要看监听消息的类型和监听消息的名称

            MessageConsumer consumer = session.createConsumer(testtopic);
            consumer.setMessageListener(new MessageListener() {//时时刻刻监听消息，从消息队列拖拽消息进行处理
                @Override
                public void onMessage(Message message) {
                    if(message instanceof TextMessage){
                        try {
                            String text = ((TextMessage) message).getText();//根据消息队列里面消息的类型强壮
                            System.out.println(text+"Xie加班");

                            //session.rollback();
                        } catch (JMSException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            });


        }catch (Exception e){
            e.printStackTrace();;
        }

    }
}
