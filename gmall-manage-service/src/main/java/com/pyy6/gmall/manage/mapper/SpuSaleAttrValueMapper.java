package com.pyy6.gmall.manage.mapper;

import com.pyy6.gmall.bean.SkuInfo;
import com.pyy6.gmall.bean.SpuSaleAttr;
import com.pyy6.gmall.bean.SpuSaleAttrValue;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SpuSaleAttrValueMapper extends Mapper<SpuSaleAttrValue> {

    List<SpuSaleAttr> selectSpuSaleAttrListCheckValueBySku(Map<String,String> map);
    List<SkuInfo> selectSkuSaleAttrValueListBySpu(@Param("spuId") String spuId);
}
