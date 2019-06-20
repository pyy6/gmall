package com.pyy6.gmall.service;

import com.pyy6.gmall.bean.BaseAttrInfo;
import com.pyy6.gmall.bean.SkuLsInfo;
import com.pyy6.gmall.bean.SkuLsParam;

import java.util.List;

public interface SearchService {
    List<SkuLsInfo> search(SkuLsParam skuLsParam);

    List<BaseAttrInfo> getAttrValueIds(List<SkuLsInfo> skuLsInfos);
}
