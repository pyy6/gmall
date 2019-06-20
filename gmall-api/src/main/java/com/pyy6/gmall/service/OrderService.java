package com.pyy6.gmall.service;

import com.pyy6.gmall.bean.OrderInfo;

public interface OrderService {
    String genTradeCode(String userId);

    Boolean checkTradeCode(String userId, String tradeCode);

    String saveOrder(OrderInfo orderInfo);

    OrderInfo getOrderById(String orderId);

    void updateOrderStatus(OrderInfo orderInfo);

    void sendORDER_RESULT_QUEUE(String outTradeNo);
}
