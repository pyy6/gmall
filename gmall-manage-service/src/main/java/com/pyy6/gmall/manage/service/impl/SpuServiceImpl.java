package com.pyy6.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyy6.gmall.bean.*;
import com.pyy6.gmall.manage.mapper.*;
import com.pyy6.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    SpuInfoMapper spuMapper;
    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    SpuImageMapper spuImageMapper;

    @Override
    public List<SpuInfo> spuList(String catalog3Id) {

        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        return spuMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public void saveSpu(SpuInfo spuInfo) {

        //保存spuInfo，返回主键
        //保存spu图片信息
        //保存销售属性
        //保存销售属性值
        spuMapper.insertSelective(spuInfo);

        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for(SpuSaleAttr spuSaleAttr :spuSaleAttrList){
            spuSaleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insert(spuSaleAttr);

            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for(SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList){
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValueMapper.insert(spuSaleAttrValue);
            }
        }
        //保存图片信息
        List<SpuImage> imgList  = spuInfo.getSpuImageList();
        for(SpuImage img: imgList){
            img.setSpuId(spuInfo.getId());
            spuImageMapper.insert(img);
        }

    }
    @Override
    public List<SpuSaleAttr> getSaleAttrListBySpuId(String spuId) {
//添加sku时，需要先获取spu销售属性
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuId);
        List<SpuSaleAttr> select = spuSaleAttrMapper.select(spuSaleAttr);

        for(SpuSaleAttr spuSaleAttr1 : select){
            SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
            spuSaleAttrValue.setSpuId(spuId);
            spuSaleAttrValue.setSaleAttrId(spuSaleAttr1.getSaleAttrId());
            spuSaleAttr1.setSpuSaleAttrValueList(spuSaleAttrValueMapper.select(spuSaleAttrValue));
        }
        return select;
    }

    @Override
    public List<SpuImage> getSpuImgListBySpuId(String spuId) {
//添加sku时，需要先获取spu图片信息
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);

        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckValueBySku(Map<String, String> map1) {
        return spuSaleAttrValueMapper.selectSpuSaleAttrListCheckValueBySku(map1);
    }

    @Override
    public List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId) {
        return spuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
    }

}
