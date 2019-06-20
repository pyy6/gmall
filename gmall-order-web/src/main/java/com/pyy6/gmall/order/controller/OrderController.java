package com.pyy6.gmall.order.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pyy6.gmall.annotation.LoginRequire;
import com.pyy6.gmall.bean.*;
import com.pyy6.gmall.bean.enums.PaymentWay;
import com.pyy6.gmall.service.CartService;
import com.pyy6.gmall.service.OrderService;
import com.pyy6.gmall.service.SkuService;
import com.pyy6.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class OrderController {

    @Reference
    SkuService skuService;
    @Reference
    CartService cartService;

    @Reference
    UserService userService;

    @Reference
    OrderService orderService;


    @LoginRequire
    @RequestMapping("submitOrder")
    public String submitOrder(String tradeCode, HttpServletRequest request, ModelMap map) {
        String userId = (String) request.getAttribute("userId");
//        String userId = "1";
        //比较交易码
        Boolean b = orderService.checkTradeCode(userId,tradeCode);
        if(b){//提交订单任务！！！！！
            //从购物车中获取被选中的数据
            List<CartInfo> cartCacheByChecked = cartService.getCartCacheByChecked(userId);
        
            //生成订单信息，将订单信息写入到数据库
            //校验sku价格、sku库存
            OrderInfo orderInfo = new OrderInfo();
            List<OrderDetail> orderDetailList = new ArrayList<>();
            for (CartInfo cartInfo : cartCacheByChecked) {
                OrderDetail orderDetail = new OrderDetail();
                BigDecimal skuPrice = cartInfo.getSkuPrice();
                String skuId = cartInfo.getSkuId();
                Boolean price = skuService.checkPrice(skuId,skuPrice);
                if(price){
                    orderDetail.setImgUrl(cartInfo.getImgUrl());
                    orderDetail.setOrderPrice(cartInfo.getCartPrice());
                    orderDetail.setSkuId(cartInfo.getSkuId());
                    orderDetail.setSkuName(cartInfo.getSkuName());
                    orderDetail.setSkuNum(cartInfo.getSkuNum());
//                    orderDetail.hasStock();
                    orderDetailList.add(orderDetail);
                }else{//sku校验价格失败,就需要将整个订单回退。
                    map.put("errMsg","获取订单信息失败（交易吗错误）");
                    orderDetailList.clear();
                    //可以修改购物车中信息的变更
                    return"tradeFail";
                }
            }
            orderInfo.setOrderDetailList(orderDetailList);
            orderInfo.setProcessStatus("订单未支付");

            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE,1);
            orderInfo.setExpireTime(c.getTime());

            orderInfo.setOrderStatus("未支付");

            String consignee = "测试";
            orderInfo.setConsignee(consignee);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String format = simpleDateFormat.format(new Date());
            String outTradeNo = "PYY6" + format +System.currentTimeMillis();
            orderInfo.setOutTradeNo(outTradeNo);
            orderInfo.setPaymentWay(PaymentWay.ONLINE);
            orderInfo.setUserId(userId);
            orderInfo.setTotalAmount(getTotalPrice(cartCacheByChecked));
            orderInfo.setOrderComment("");
            String address = "";
            orderInfo.setDeliveryAddress(address);
            orderInfo.setConsigneeTel("1234557");

            String orderId = orderService.saveOrder(orderInfo);//因为不是在一个系统中，是远程注入reference，所以orderInfo有了主键但是在这个里面不会有，因为不在一个spring容器里面

            //将购物车中被勾选的提交信息删除,并且同步缓存
            cartService.deleteCartById(cartCacheByChecked);

            //跳转到支付页面（跟支付服务的接口对接）
            return "redirect:http://payment.gmall.com:8089/index?orderId="+orderId;

        }else{//交易码过期
            map.put("errMsg","获取订单信息失败（交易吗错误）");
            return"tradeFail";
        }
    }

    @LoginRequire
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request, ModelMap map) {
        //用户没有登录需要被单点登录的拦截器拦截
        String userId = (String) request.getAttribute("userId");

        //查询用户的收货地址，让用户选择
        List<UserAddress> userAddressList = userService.getAddrListByUserId(userId);
        map.put("userAddressList",userAddressList);
        //展示支付方式

        //将被选中的购物车对象转化为订单对象，展示出来，只是展示，提交订单不会使用这个orderDetailList。
        List<CartInfo> cartInfos = cartService.getCartCacheByChecked(userId);
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (CartInfo cartInfo : cartInfos) {
            OrderDetail orderDetail = new OrderDetail();

            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());///?
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setSkuNum(cartInfo.getSkuNum());

            orderDetailList.add(orderDetail);
        }
        map.put("orderDetailList",orderDetailList);
        map.put("totalAmount",getTotalPrice(cartInfos));

        //在点击结算的时候生成交易码，保存到订单核对页面
        String tradeCode = orderService.genTradeCode(userId);
        map.put("tradeCode",tradeCode);

        return "trade";
    }

    private BigDecimal getTotalPrice(List<CartInfo> cartInfos) {
        BigDecimal totalPrice = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfos) {
            if(cartInfo.getIsChecked().equals("1")){
                totalPrice = totalPrice.add(cartInfo.getCartPrice());
            }
        }
        return totalPrice;
    }

}
