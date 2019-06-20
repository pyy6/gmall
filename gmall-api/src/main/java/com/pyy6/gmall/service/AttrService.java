package com.pyy6.gmall.service;

import com.pyy6.gmall.bean.BaseAttrInfo;

import java.util.List;

public interface AttrService {

    List<BaseAttrInfo> getAttrList(String catalog3Id);

    void saveAttr(BaseAttrInfo baseAttrInfo);

    List<BaseAttrInfo> getAttrListByCtg3Id(String catalog3Id);

    List<BaseAttrInfo> getAttrListByValueIds(String join);
}
