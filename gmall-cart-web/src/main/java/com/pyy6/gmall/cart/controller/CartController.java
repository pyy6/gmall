package com.pyy6.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pyy6.gmall.annotation.LoginRequire;
import com.pyy6.gmall.bean.CartInfo;
import com.pyy6.gmall.bean.SkuInfo;
import com.pyy6.gmall.service.CartService;
import com.pyy6.gmall.service.SkuService;
import com.pyy6.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.pyy6.gmall.util.CookieUtil.getCookieValue;

@Controller
public class CartController {

    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;

    //订单系统中，必须登录才能访问的方法

    @LoginRequire(ifNeedSuccess = false)////可以验证失败，可以验证成功，处理分支不同
    @RequestMapping("checkCart")
    public String checkCart(HttpServletRequest request, HttpServletResponse response,CartInfo cartInfo,ModelMap map) {//购物车列表的操作

        String userId = (String) request.getAttribute("userId");
        //修改购物车的选中状态
        if(StringUtils.isBlank(userId)){
            //修改cookie中的购物车数据
            String value = CookieUtil.getCookieValue(request,"cartListCookie",true);
            List<CartInfo> cartInfos = JSON.parseArray(value, CartInfo.class);
            for (CartInfo info : cartInfos) {
                if(info.getSkuId().equals(cartInfo.getSkuId())){
                    info.setIsChecked(cartInfo.getIsChecked());
                }
            }
            CookieUtil.setCookie(request,response,"cartListCookie",JSON.toJSONString(cartInfos),60*60*24*7,true);
        }else{
           /* //修改db中的购物车数据
            cartInfo.setUserId(userId);
            CartInfo cartInfo1 = cartService.ifCartExistSku(cartInfo);
            cartInfo1.setIsChecked(cartInfo.getIsChecked());
            cartService.updateCart(cartInfo1);
            //同步到redis
            cartService.syncCache(userId);*/

           cartInfo.setUserId(userId);
           cartService.updateCartChecked(cartInfo);

           cartService.syncCache(userId);
        }

        //更新购物车操作后重新查询数据到页面
        List<CartInfo> cartInfos = new ArrayList<>();

        if(StringUtils.isBlank(userId)){
            //取cookie中的数据
            String cartListCookie = getCookieValue(request, "cartListCookie", true);

            if(StringUtils.isNotBlank(cartListCookie)){
                cartInfos = JSON.parseArray(cartListCookie,CartInfo.class);
            }
        }else{
            //取缓存中的数据
            cartInfos = cartService.getCartCache(userId);
        }
        BigDecimal b = getTotalPrice(cartInfos);
        map.put("cartList",cartInfos);
        map.put("totalPrice",b);

        return "cartListInner";
    }

    @LoginRequire(ifNeedSuccess = false)
    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request, ModelMap map){//查购物车列表

        List<CartInfo> cartInfos = new ArrayList<>();

        String userId = (String) request.getAttribute("userId");
        if(StringUtils.isBlank(userId)){
            //取cookie中的数据
            String cartListCookie = getCookieValue(request, "cartListCookie", true);

            if(StringUtils.isNotBlank(cartListCookie)){
                cartInfos = JSON.parseArray(cartListCookie,CartInfo.class);
            }
        }else{
            //取缓存中的数据
            cartInfos = cartService.getCartCache(userId);
        }
        BigDecimal b = getTotalPrice(cartInfos);
        map.put("cartList",cartInfos);
        map.put("totalPrice",b);

        return "cartList";
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

    @LoginRequire(ifNeedSuccess = false)
    @RequestMapping("addToCart")
    public String addToCart(HttpServletRequest request, HttpServletResponse response, CartInfo cartInfo){

        String skuId = cartInfo.getSkuId();
        SkuInfo skuInfo = skuService.getSkuInfoBySkuId(skuId);

        //根据加入购物车的skuid从后台查询sku信息封装购物车对象
        cartInfo.setCartPrice(skuInfo.getPrice().multiply(new BigDecimal(cartInfo.getSkuNum())));
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setIsChecked("1");
        cartInfo.setSkuName(skuInfo.getSkuName());
        cartInfo.setSkuPrice(skuInfo.getPrice());


        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartListCookie = new ArrayList<>();//这个公共变量六个分支都可以用，不会引发数据安全问题。因为只会走一个分支
        if(StringUtils.isBlank(userId)){
            //用户未登录，添加cookie
            String cartListCookieStr = getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isBlank(cartListCookieStr)){//cookie中没有购物车
//              cartListCookie = new ArrayList<>();
                cartListCookie.add(cartInfo);
//                CookieUtil.setCookie(request,response,"cartListCookie", JSON.toJSONString(cartListCookie),60*60*24*7,true);
            }else{//cookie中有购物车
                cartListCookie = new ArrayList<>();
                cartListCookie = JSON.parseArray(cartListCookieStr,CartInfo.class);
                //判断是否重复sku
                if(ifNewSku(cartListCookie,cartInfo)){
                    cartListCookie.add(cartInfo);//添加
//                    CookieUtil.setCookie(request,response,"cartListCookie", JSON.toJSONString(cartListCookie),60*60*24*7,true);
                }else{
                    //更新,直接更新cartListCookie，就不用添加了
                    for (CartInfo info : cartListCookie) {//cookie的购物车对象(没有用户id和购物车id的)
                        if(info.getSkuId().equals(cartInfo.getSkuId())){
                            info.setSkuNum(info.getSkuNum() + cartInfo.getSkuNum());
                            info.setCartPrice(info.getSkuPrice().multiply(new BigDecimal(info.getSkuNum())));
                        }
                    }
//                    CookieUtil.setCookie(request,response,"cartListCookie", JSON.toJSONString(cartListCookie),60*60*24*7,true);
                }
            }
            //操作完成后覆盖cookie，
            CookieUtil.setCookie(request,response,"cartListCookie", JSON.toJSONString(cartListCookie),60*60*24*7,true);
        }else{
            //用户已登录，添加DB
            //String skuId = cartInfo.getSkuId();
            //select * from cart_info where sku_id = skuId and user_id = userId;
            cartInfo.setUserId(userId);
            CartInfo cartInfoDB = cartService.ifCartExistSku(cartInfo);
            if(cartInfoDB != null){
                //更新数据库
                cartInfoDB.setSkuNum(cartInfoDB.getSkuNum()+cartInfo.getSkuNum());
                cartInfoDB.setCartPrice(cartInfoDB.getSkuPrice().multiply(new BigDecimal(cartInfoDB.getSkuNum())));
                cartService.updateCart(cartInfoDB);//一个用户的购物车可能有多个CartInfo的id。一条购物车记录对应一个CartInfo的id
            }else{
                //插入数据库
                cartService.saveCart(cartInfo);
            }
            //同步缓存redis
            cartService.syncCache(userId);//同步用户的缓存
            //carts:userId:info 一个用户对应多个购物车,是个集合，每次操作一个购物车都需要将整个集合取出来（hash）
            //sku:skuId:info 一个skuId对应一个sku，是一个对象
        }

        return "redirect:/cartSuccess";
    }

    private boolean ifNewSku(List<CartInfo> cartListCookie, CartInfo cartInfo) {
        Boolean b = true;
        for (CartInfo info : cartListCookie) {//cookie的购物车对象(没有用户id和购物车id的)
            if(info.getSkuId().equals(cartInfo.getSkuId())){
               b = false;
            }
        }
        return b;
    }

    @LoginRequire(ifNeedSuccess = false)
    @RequestMapping("cartSuccess")
    public String cartSuccess(){

        return "success";
    }
}
