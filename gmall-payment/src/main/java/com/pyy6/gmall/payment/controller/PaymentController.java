package com.pyy6.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.pyy6.gmall.annotation.LoginRequire;
import com.pyy6.gmall.bean.OrderInfo;
import com.pyy6.gmall.bean.PaymentInfo;
import com.pyy6.gmall.payment.service.PaymentService;
import com.pyy6.gmall.payment.util.AlipayConfig;
import com.pyy6.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Reference
    OrderService orderService;

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    PaymentService paymentService;

//    @LoginRequire
    @RequestMapping("alipay/callback/return")
    public String callback(HttpServletRequest request,String orderId, ModelMap map){

        //确认是否是阿里给的返回信息（ali的私钥加密，gmall用阿里给的公钥解密）
        Map<String, String> paramsMap = null; //将异步通知中收到的所有参数都存放到map中,因为ali不能通过公网访问到我的支付服务项目，所以没有回跳地址
        boolean signVerified = true;//验签，验证签名
        try {
            signVerified = AlipaySignature.rsaCheckV1(paramsMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);//调用SDK验证签名
        } catch (Exception e) {
            e.printStackTrace();
        }
        String outTradeNo = "";
        if(signVerified){
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            ///修改支付信息
            //从ali传来的参数中修改
            String aliTradeNo = request.getParameter("trade_no");
            outTradeNo = request.getParameter("out_trade_no");
//        String tradeStatus = request.getParameter("trade_status");
            String queryString = request.getQueryString();

            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setCallbackContent(queryString);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setOutTradeNo(outTradeNo);
            paymentInfo.setAlipayTradeNo(aliTradeNo);
            paymentService.updatePayment(paymentInfo);

            //发送支付成功的消息PAYMENT_SUCCESS_QUEUE，发送消息是服务与服务之间进行的，不能将其放在web端
//            paymentService.sendPAYMENT_SUCCESS_QUEUE(outTradeNo,aliTradeNo);
            //幂等性检查
            Boolean b = paymentService.checkPaied(outTradeNo);
            if(!b){
                //发送支付成功队列
                paymentService.sendPAYMENT_SUCCESS_QUEUE("ahahha",outTradeNo);
            }
        }else{
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            //返回失败页面，钱付了，出现了问题
        }
        return "testPay";
    }
    @LoginRequire
    @RequestMapping("index")
    public String index(String orderId, ModelMap map){

        OrderInfo orderInfo = orderService.getOrderById(orderId);
        map.put("outTradeNo",orderInfo.getOutTradeNo());
        map.put("orderId",orderId);
        map.put("totalAmount",orderInfo.getTotalAmount());
        return "index";
    }

    @LoginRequire
    @RequestMapping("alipay/submit")
    @ResponseBody
    public String submit(String orderId){

        //生成和保存支付信息paymentinfo
        OrderInfo order = orderService.getOrderById(orderId);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(order.getOutTradeNo());
        paymentInfo.setTotalAmount(order.getTotalAmount());
        paymentInfo.setPaymentStatus("未支付");
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setSubject(order.getOrderDetailList().get(0).getSkuName());
        paymentInfo.setCreateTime(new Date());
        paymentService.savePayment(paymentInfo);

        //重定向到支付宝的平台
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址


        HashMap<String, Object> stringObjectHashMap = new HashMap<>();

        stringObjectHashMap.put("out_trade_no",order.getOutTradeNo());
        stringObjectHashMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        stringObjectHashMap.put("total_amount","0.01");
        stringObjectHashMap.put("subject","测试");

        String s = JSON.toJSONString(stringObjectHashMap);
        alipayRequest.setBizContent(s);//填充业务参数
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //设置一个定时巡检订单支付状态的延迟队列
        System.out.println("设置一个定时巡检订单支付状态的延迟队列");
        paymentService.sendPaymentCheckQueue(paymentInfo.getOutTradeNo(),5);//巡检五次
        return form;
    }

    @LoginRequire
    @RequestMapping("mx/submit")
    public void mx(String orderId){
        //重定向到微信的平台
        return;
    }
}
