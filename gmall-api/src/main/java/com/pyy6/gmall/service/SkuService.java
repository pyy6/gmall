package com.pyy6.gmall.service;

import com.pyy6.gmall.bean.SkuInfo;

import java.math.BigDecimal;
import java.util.List;

public interface SkuService {

    List<SkuInfo> getSkuListBySpu(String spuId);

    void saveSku(SkuInfo skuInfo);

    SkuInfo getSkuInfoBySkuId(String skuId);

    List<SkuInfo> getSkuInfoListByCatalog3Id(String s);

    Boolean checkPrice(String skuId, BigDecimal skuPrice);
}
