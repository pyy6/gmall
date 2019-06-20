package com.pyy6.gmall.order.mq;


import com.pyy6.gmall.bean.OrderInfo;
import com.pyy6.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderMQListener {

    @Autowired
    OrderService orderService;

    @JmsListener(containerFactory = "jmsQueueListener",destination = "PAYMENT_SUCCESS_QUEUE")
    public void consumerPaymentSuccess(MapMessage mapMessage) throws JMSException{

        String trackingNo = mapMessage.getString("trackingNo");
        String outTradeNo = mapMessage.getString("outTradeNo");

        //修改订单状态（order_info的四个字段）
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setTrackingNo(trackingNo);
        orderInfo.setOrderStatus("订单翼支付");
        orderInfo.setProcessStatus("准备出库");
        orderService.updateOrderStatus(orderInfo);

        //发送订单消息，给库存
        orderService.sendORDER_RESULT_QUEUE(orderInfo.getOutTradeNo());

        System.out.println("订单监听支付成功。。。");

    }
}
