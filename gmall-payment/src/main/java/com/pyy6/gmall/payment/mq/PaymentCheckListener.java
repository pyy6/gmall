package com.pyy6.gmall.payment.mq;

import com.pyy6.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class PaymentCheckListener {

    @Autowired
    PaymentService paymentService;

    @JmsListener(containerFactory = "jmsQueueListener", destination = "PAYMENT_CHECK_QUEUE")
    public void consumerPaymentCheckListener(MapMessage mapMessage) throws JMSException {

        int count = mapMessage.getInt("count");
        String out = mapMessage.getString("outTradeNo");
        System.out.println("支付系统监听到延迟监察队列，执行延迟检查第" + (6 - count));

        //检查支付状态
        String status = paymentService.checkPayment(out);
        if (status.equals("TRADE_SUCCESS") || status.equals("TRADE_CLOSED")) {
            //幂等性检查
            Boolean b = paymentService.checkPaied(out);

            if(!b){
                //发送支付成功队列
                paymentService.sendPAYMENT_SUCCESS_QUEUE("ahahha",out);
            }
        }else{
            if (count > 0) {
                //继续发送延迟队列
                paymentService.sendPaymentCheckQueue(out, count - 1);
            } else {
                System.out.println("延迟检查次数耗尽");
            }

        }

    }

}
