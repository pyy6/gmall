package com.pyy6.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pyy6.gmall.bean.CartInfo;
import com.pyy6.gmall.cart.mapper.CartInfoMapper;
import com.pyy6.gmall.service.CartService;
import com.pyy6.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartInfoMapper cartInfoMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public CartInfo ifCartExistSku(CartInfo cartInfo) {

        CartInfo cartInfo1 = new CartInfo();
        cartInfo1.setUserId(cartInfo.getUserId());
        cartInfo1.setSkuId(cartInfo.getSkuId());
        CartInfo select = cartInfoMapper.selectOne(cartInfo1);
        return select;
    }

    @Override
    public void updateCart(CartInfo cartInfoDB) {

        cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
    }

    @Override
    public void saveCart(CartInfo cartInfo) {

        cartInfoMapper.insertSelective(cartInfo);
    }

    @Override
    public void syncCache(String userId) {
        Jedis jedis = redisUtil.getJedis();

        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> select = cartInfoMapper.select(cartInfo);

        if(select == null || select.size() == 0){
            jedis.del("carts:"+userId+":info");
            return;
        }
        HashMap<String, String> hashMap = new HashMap<>();
        for (CartInfo info : select) {
            hashMap.put(info.getId(), JSON.toJSONString(info));
        }
        jedis.hmset("carts:"+userId+":info",hashMap);
        jedis.close();
    }

    @Override
    public List<CartInfo> getCartCache(String userId) {

        List<CartInfo> cartInfos = new ArrayList<>();

        Jedis jedis = redisUtil.getJedis();

        List<String> hvals = jedis.hvals("carts:" + userId + ":info");

        if(hvals != null && hvals.size() > 0){
            for (String val : hvals) {
                CartInfo cartInfo = JSON.parseObject(val,CartInfo.class);
                cartInfos.add(cartInfo);
            }
        }
        return cartInfos;
    }

    @Override
    public void updateCartChecked(CartInfo cartInfo) {

        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("skuId",cartInfo.getSkuId()).andEqualTo("userId",cartInfo.getUserId());
//只更新有值的字段，所以可以先不用封装cartInfo
        cartInfoMapper.updateByExampleSelective(cartInfo,example);
    }

    @Override
    public void combineCart(List<CartInfo> cartInfos, String userId) {
        if(cartInfos != null){//合并
            for (CartInfo cartInfo : cartInfos) {
                cartInfo.setUserId(userId);
                CartInfo cartInfoDB = ifCartExistSku(cartInfo);
                if(cartInfoDB == null){
                    //插入
                    cartInfoMapper.insertSelective(cartInfo);
                }else{
                    cartInfo.setSkuNum(cartInfo.getSkuNum()+cartInfoDB.getSkuNum());
                    cartInfo.setCartPrice(cartInfo.getSkuPrice().multiply(new BigDecimal(cartInfo.getSkuNum())));
                    cartInfo.setId(cartInfoDB.getId());
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfo);
                }
            }
        }
        syncCache(userId);
    }

    @Override
    public List<CartInfo> getCartCacheByChecked(String userId) {

        List<CartInfo> cartInfos = new ArrayList<>();

        Jedis jedis = redisUtil.getJedis();

        List<String> hvals = jedis.hvals("carts:" + userId + ":info");

        if(hvals != null && hvals.size() > 0){
            for (String val : hvals) {
                CartInfo cartInfo = JSON.parseObject(val,CartInfo.class);
                if(cartInfo.getIsChecked().equals("1")){
                    cartInfos.add(cartInfo);
                }
            }
        }
        return cartInfos;
    }

    @Override
    public void deleteCartById(List<CartInfo> cartCacheByChecked) {
        for (CartInfo cartInfo : cartCacheByChecked) {
            cartInfoMapper.deleteByPrimaryKey(cartInfo);
        }
        syncCache(cartCacheByChecked.get(0).getUserId());
    }
}
