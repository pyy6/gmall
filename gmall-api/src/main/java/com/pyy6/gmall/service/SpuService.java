package com.pyy6.gmall.service;

import com.pyy6.gmall.bean.*;

import java.util.List;
import java.util.Map;

public interface SpuService {
    List<SpuInfo> spuList(String catalog3Id);

    List<BaseSaleAttr> baseSaleAttrList();

    void saveSpu(SpuInfo spuInfo);

    List<SpuSaleAttr> getSaleAttrListBySpuId(String spuId);

    List<SpuImage> getSpuImgListBySpuId(String spuId);

    List<SpuSaleAttr> getSpuSaleAttrListCheckValueBySku(Map<String, String> map1);

    List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId);
}
