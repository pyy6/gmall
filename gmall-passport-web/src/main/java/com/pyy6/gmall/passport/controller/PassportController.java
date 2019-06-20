package com.pyy6.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pyy6.gmall.bean.CartInfo;
import com.pyy6.gmall.bean.UserInfo;
import com.pyy6.gmall.service.CartService;
import com.pyy6.gmall.service.UserService;
import com.pyy6.gmall.util.CookieUtil;
import com.pyy6.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import tk.mybatis.mapper.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PassportController {
    //两个功能：
    // 1、登录成功之后颁发证书，生成token，颁发token；
    // 2、为拦截器验证证书，验证身份    获取浏览器携带过来的token以及salt。
    @Reference
    UserService userService;

    //理论上不能存在这个
    @Reference
    CartService cartService;

    @RequestMapping("login")
    @ResponseBody
    public String login(HttpServletRequest request, HttpServletResponse response, UserInfo userInfo) {//颁发token

        //调用用户服务验证用户名和密码
        UserInfo user = userService.login(userInfo);
        if(user == null){
            //用户名密码错误
            return "username or password error";
        }else{
            //颁发token
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("userId",user.getId());
            hashMap.put("nickName",user.getNickName());

            String token = JwtUtil.encode("pyy6", hashMap, getMyIp(request));
            //重定向原始业务??在登录页面经过异步请求后进行重定向
///////////////////////////////////////////////////////////////////
            //合并购物车（先放在这，之后有消息队列）
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            List<CartInfo> cartInfos = null;
            if(StringUtils.isNotBlank(cartListCookie)){
                cartInfos = JSON.parseArray(cartListCookie, CartInfo.class);
            }
            //因为需要将购物车中的数据放到缓存，所以空的时候也要放数据到缓存
            cartService.combineCart(cartInfos,user.getId());
            //删除cookie中的购物车数据
            CookieUtil.deleteCookie(request,response,"cartListCookie");
//////////////////////////////////////////////////////////////////////////////////
            return token;
        }
    }

    private String getMyIp(HttpServletRequest request) {
        String ip = "";
        ip = request.getHeader("x-forwarded-for");//经过负载均衡的ip
        if(StringUtils.isBlank(ip)){
            ip = request.getRemoteAddr();//直接获取ip
        }
        if(StringUtils.isBlank(ip)){
            ip = "127.0.0.1";
        }
        return ip;
    }

    @ResponseBody
    @RequestMapping("verify")
    public String verify(String token,String currentip) {//验证token
        Map userMap = null;
        try {
            userMap = JwtUtil.decode("pyy6", token, currentip);
        }catch (Exception e){
            return "fail";
        }
        if(userMap != null){
            return "success";
        }else{
            return "fail";
        }
    }

    //登录页
    @RequestMapping("index")
    public String index(String returnUrl, ModelMap map) {//让用户定位到登录的方法

        map.put("returnUrl",returnUrl);
        return "index";
    }
}
