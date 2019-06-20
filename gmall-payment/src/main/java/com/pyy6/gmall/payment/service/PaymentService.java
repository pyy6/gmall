package com.pyy6.gmall.payment.service;

import com.pyy6.gmall.bean.PaymentInfo;

public interface PaymentService {
    void savePayment(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);

    void sendPAYMENT_SUCCESS_QUEUE(String outTradeNo, String aliTradeNo);

    void sendPaymentCheckQueue(String outTradeNo,int count);

    String checkPayment(String out);

    Boolean checkPaied(String out);
}
