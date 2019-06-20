package com.pyy6.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pyy6.gmall.bean.SkuAttrValue;
import com.pyy6.gmall.bean.SkuImage;
import com.pyy6.gmall.bean.SkuInfo;
import com.pyy6.gmall.bean.SkuSaleAttrValue;
import com.pyy6.gmall.manage.mapper.SkuAttrValueMapper;
import com.pyy6.gmall.manage.mapper.SkuImageMapper;
import com.pyy6.gmall.manage.mapper.SkuInfoMapper;
import com.pyy6.gmall.manage.mapper.SkuSaleAttrValueMapper;
import com.pyy6.gmall.service.SkuService;
import com.pyy6.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<SkuInfo> getSkuListBySpu(String spuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(spuId);
        return skuInfoMapper.select(skuInfo);
    }

    @Override
    public void saveSku(SkuInfo skuInfo) {
        skuInfoMapper.insertSelective(skuInfo);
        String skuId = skuInfo.getId();

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for(SkuImage skuImage:skuImageList){
            skuImage.setSkuId(skuId);
            skuImageMapper.insert(skuImage);
        }

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for(SkuAttrValue skuAttrValue:skuAttrValueList){
            skuAttrValue.setSkuId(skuId);
            skuAttrValueMapper.insert(skuAttrValue);
        }

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for(SkuSaleAttrValue skuSaleAttrValue:skuSaleAttrValueList){
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        }

    }

    @Override
    public SkuInfo getSkuInfoBySkuId(String skuId) {

        SkuInfo skuInfo = null;
        Jedis jedis = redisUtil.getJedis();
        //查询redis缓存
        String key = "sku" + skuId + "info";
        String value = jedis.get(key);
//        skuInfo = JSON.parseObject(value,SkuInfo.class);
        //下一个同伴在这个地方被通知，查看锁是否为空
        if("empty".equals(value)){
            return skuInfo;//???
        }

        if(StringUtils.isBlank(value) ){
            //申请分布式缓存锁
            String OK = jedis.set("sku" + skuId + "lock","1","nx","px",3000);

            if("OK".equals(OK)){//既包含了空字符串和null的校验,拿到缓存锁
                //查询db,如果查询到数据库里面的数据为空，就要告诉下一个不要访问数据库了
                skuInfo = getSkuInfoBySkuIdByDB(skuId);

                //数据库中有数据，就需要同步缓存并且归还缓存锁
                if(skuInfo != null){
                    //同步缓存
                    jedis.set(key,JSON.toJSONString(skuInfo));
                    //归还缓存锁
                }else{
                    //如果查询到数据库里面的数据为空，就要告诉下一个不要访问数据库了,归还锁
                    jedis.setex("sku" + skuId + "info",10,"empty");
                }
                //归还缓存锁
                jedis.del("sku" + skuId + "lock");
            }else{//没有拿到缓存锁，排队等待
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //自旋
                getSkuInfoBySkuId(skuId);
            }
        }else{
            skuInfo = JSON.parseObject(value,SkuInfo.class);
        }
        return skuInfo;
    }

    @Override
    public List<SkuInfo> getSkuInfoListByCatalog3Id(String s) {

        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setCatalog3Id(s);
        List<SkuInfo> select = skuInfoMapper.select(skuInfo);

        for (SkuInfo info : select) {
            String id = info.getId();

            SkuAttrValue skuAttrValue = new SkuAttrValue();
            skuAttrValue.setSkuId(id);
            List<SkuAttrValue> select1 = skuAttrValueMapper.select(skuAttrValue);

            info.setSkuAttrValueList(select1);
        }
        return select;
    }

    @Override
    public Boolean checkPrice(String skuId, BigDecimal skuPrice) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        SkuInfo skuInfo1 = skuInfoMapper.selectOne(skuInfo);
        boolean b = false;

        if(skuInfo1 != null){
            int i = skuInfo1.getPrice().compareTo(skuPrice);
            if(i == 0){
                b = true;
            }
        }
        return b;
    }

    public SkuInfo getSkuInfoBySkuIdByDB(String skuId){
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        SkuInfo sku = skuInfoMapper.selectOne(skuInfo);

        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        sku.setSkuImageList(skuImageMapper.select(skuImage));

        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuId);
        sku.setSkuSaleAttrValueList(skuSaleAttrValueMapper.select(skuSaleAttrValue));
        return sku;
    }
}
