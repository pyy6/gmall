package com.pyy6.gmall.payment.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.pyy6.gmall.bean.PaymentInfo;
import com.pyy6.gmall.payment.mapper.PaymentMapper;
import com.pyy6.gmall.payment.service.PaymentService;
import com.pyy6.gmall.util.ActiveMQUtil;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Date;
import java.util.HashMap;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentMapper paymentMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePayment(PaymentInfo paymentInfo) {
        paymentMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {

        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",paymentInfo.getOutTradeNo());
        paymentMapper.updateByExampleSelective(paymentInfo,example);
    }

    @Override
    public void sendPAYMENT_SUCCESS_QUEUE(String outTradeNo, String aliTradeNo) {

        //修改支付服务信息。
        PaymentInfo paymentInfo = new PaymentInfo();
//        paymentInfo.setCallbackContent(queryString);
        paymentInfo.setPaymentStatus("已支付");
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setAlipayTradeNo(aliTradeNo);
        updatePayment(paymentInfo);

////////////////////////////////////////////////////////////////////////////////
        //生成某个地址下面的连接池
        Connection connection = activeMQUtil.getConnection();
        try {
            //建立mq的连接
//            Connection connection = connect.createConnection();
            connection.start();
            //通过连接创建一次与mq的会话任务
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);//可以保证分布式事务，如果某个comsumer没有真正的消费成功这个消息，他会将这个消息放回队列被其他comsumer消费。
            Queue testqueue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
            //通过mq的会话任务将消息队列中的消息发送出去
            MessageProducer producer = session.createProducer(testqueue);
            MapMessage mapMessage = (MapMessage) new ActiveMQMessage();
            mapMessage.setString("trackingNo",aliTradeNo);
            mapMessage.setString("outTradeNo",outTradeNo);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);//哪怕是发送消息时没有一个comsumer，但是这条消息总是会被执行的
            producer.send(mapMessage);
            //提交任务
            session.commit();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.out.println("支付成功，发送支付服务的消息队列");
    }

    @Override
    public void sendPaymentCheckQueue(String outTradeNo,int count) {

        //生成某个地址下面的连接池
        Connection connection = activeMQUtil.getConnection();
        try {
            //建立mq的连接
//            Connection connection = connect.createConnection();
            connection.start();
            //通过连接创建一次与mq的会话任务
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);//可以保证分布式事务，如果某个comsumer没有真正的消费成功这个消息，他会将这个消息放回队列被其他comsumer消费。
            Queue testqueue = session.createQueue("PAYMENT_CHECK_QUEUE");
            //通过mq的会话任务将消息队列中的消息发送出去
            MessageProducer producer = session.createProducer(testqueue);
            MapMessage textMessage = (MapMessage) new ActiveMQMessage();
            textMessage.setInt("count",count);
            textMessage.setString("outTradeNo",outTradeNo);

            textMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,1000*10);

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);//哪怕是发送消息时没有一个comsumer，但是这条消息总是会被执行的
            producer.send(textMessage);
            //提交任务
            session.commit();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.out.println("第"+(6-count)+"次延迟队列的检查");
    }

    @Override
    public String checkPayment(String out) {
        String status  = "";
        //调用支付宝检查支付状态的接口
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

        HashMap<String , Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("out_trade_no",out);
        String s = JSON.toJSONString(objectObjectHashMap);
        request.setBizContent(s);

        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            status = response.getTradeStatus();
        }else{
            System.out.println("用户未扫码");
        }
        return status;
    }

    @Override
    public Boolean checkPaied(String out) {

        Boolean b = false;

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(out);
        PaymentInfo paymentInfo1 = paymentMapper.selectOne(paymentInfo);

        if(paymentInfo1 != null && paymentInfo1.getPaymentStatus().equals("已支付")){
            b = true;
        }

        return b;
    }
}
