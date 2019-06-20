package com.pyy6.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyy6.gmall.bean.SkuInfo;
import com.pyy6.gmall.service.SkuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SkuController {

    @Reference
    SkuService skuService;

    @RequestMapping("getSkuListBySpu")
    @ResponseBody
    public List<SkuInfo> getSkuListBySpu(String spuId){
        List<SkuInfo> list = skuService.getSkuListBySpu(spuId);
        return list;
    }
    @RequestMapping("saveSku")
    @ResponseBody
    public String saveSku(SkuInfo skuInfo){
        skuService.saveSku(skuInfo);
        return "success";
    }

}
