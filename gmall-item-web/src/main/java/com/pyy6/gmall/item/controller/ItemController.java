package com.pyy6.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pyy6.gmall.bean.SkuInfo;
import com.pyy6.gmall.bean.SkuSaleAttrValue;
import com.pyy6.gmall.bean.SpuSaleAttr;
import com.pyy6.gmall.bean.UserInfo;
import com.pyy6.gmall.service.AttrService;
import com.pyy6.gmall.service.SkuService;
import com.pyy6.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    //不写这行Reference注入是不会在dubbo上注册服务的
   @Reference
    SkuService skuService;
    @Reference
    SpuService spuService;

//skuid肯定是从某一个页面上传参传递过来的
    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap  map){

        SkuInfo skuInfo = skuService.getSkuInfoBySkuId(skuId);
        map.put("skuInfo",skuInfo);

        String spuId = skuInfo.getSpuId();

//        //当前sku所包含的销售属性
//        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
//        //spu销售属性列表
//        List<SpuSaleAttr> saleAttrListBySpuId = spuService.getSaleAttrListBySpuId(spuId);

        //查找sku对应的spu销售属性以及该sku销售属性值
        Map<String,String > map1 = new HashMap<>();
        map1.put("spuId",spuId);
        map1.put("skuId",skuId);
        List<SpuSaleAttr> saleAttrListBySpuId = spuService.getSpuSaleAttrListCheckValueBySku(map1);

        map.put("spuSaleAttrListCheckBySku",saleAttrListBySpuId);

        //用于用户切换sku时一次性查找的hashmap->json,spu的sku和销售属性对应关系的hash表
        List<SkuInfo> infos = spuService.getSkuSaleAttrValueListBySpu(spuId);
        //hashmap
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        for(SkuInfo info:infos){
            String value = info.getId();
            List<SkuSaleAttrValue> skuSaleAttrValueList = info.getSkuSaleAttrValueList();
            String key = "";
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                key += skuSaleAttrValue.getSaleAttrValueId() + "-";
            }
            stringStringHashMap.put(key,value);
        }
        //json
        String skuJson = JSON.toJSONString(stringStringHashMap);
        map.put("skuJson",skuJson);
        return "item";
    }

    @RequestMapping("index")
    public String index(ModelMap  map){
        //ModelMap可以在demo页面上被取值？？？？？？？？？
        map.put("hello","thymeleaf");

        List<UserInfo> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            UserInfo userInfo = new UserInfo();
            userInfo.setName("xiao"+1);
            userInfo.setEmail("12345"+i);
            list.add(userInfo);
        }
        map.put("list",list);
        return "demo";
    }
}
