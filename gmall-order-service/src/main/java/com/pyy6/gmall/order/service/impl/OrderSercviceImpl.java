package com.pyy6.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyy6.gmall.bean.OrderDetail;
import com.pyy6.gmall.bean.OrderInfo;
import com.pyy6.gmall.order.mapper.OrderDetailMapper;
import com.pyy6.gmall.order.mapper.OrderInfoMapper;
import com.pyy6.gmall.service.OrderService;
import com.pyy6.gmall.util.ActiveMQUtil;
import com.pyy6.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.List;
import java.util.UUID;

@Service
public class OrderSercviceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;
    
    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public String genTradeCode(String userId) {

        Jedis jedis = redisUtil.getJedis();
        String key = "user:"+userId+":tradeCode";
        String tradeCode = UUID.randomUUID().toString();

        jedis.setex(key,60*30,tradeCode);
        jedis.close();
        return tradeCode;
    }

    @Override
    public Boolean checkTradeCode(String userId, String tradeCode) {

        Boolean b = false;
        Jedis jedis = redisUtil.getJedis();
        String value = jedis.get("user:" + userId + ":tradeCode");

        if(StringUtils.isNotBlank(value) && value.equals(tradeCode)){
            b = true;
            jedis.del("user:" + userId + ":tradeCode");
        }
        return b;
    }

    @Override
    public String saveOrder(OrderInfo orderInfo) {
        orderInfoMapper.insertSelective(orderInfo);
        String id = orderInfo.getId();

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(id);
            orderDetailMapper.insertSelective(orderDetail);
        }
        return id;
    }

    @Override
    public OrderInfo getOrderById(String orderId) {
        OrderInfo orderInfo1 = new OrderInfo();
        orderInfo1.setId(orderId);
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderInfo1);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> select = orderDetailMapper.select(orderDetail);

        orderInfo.setOrderDetailList(select);

        return orderInfo;
    }
    @Override
    public void updateOrderStatus(OrderInfo orderInfo) {

        Example e = new Example(OrderInfo.class);
        e.createCriteria().andEqualTo("outTradeNo",orderInfo.getOutTradeNo());
        orderInfoMapper.updateByExampleSelective(orderInfo,e);
    }

    @Override
    public void sendORDER_RESULT_QUEUE(String outTradeNo) {

        //生成某个地址下面的连接池
        Connection connection = activeMQUtil.getConnection();
        try {
            //建立mq的连接
//            Connection connection = connect.createConnection();
            connection.start();
            //通过连接创建一次与mq的会话任务
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);//可以保证分布式事务，如果某个comsumer没有真正的消费成功这个消息，他会将这个消息放回队列被其他comsumer消费。
            Queue testqueue = session.createQueue("ORDER_RESULT_QUEUE");
            //通过mq的会话任务将消息队列中的消息发送出去
            MessageProducer producer = session.createProducer(testqueue);
            TextMessage textMessage = (TextMessage) new ActiveMQMessage();
            textMessage.setText(outTradeNo);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);//哪怕是发送消息时没有一个comsumer，但是这条消息总是会被执行的
            producer.send(textMessage);
            //提交任务
            session.commit();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.out.println("订单修改成功，发送订单服务的消息队列");
    }

}
