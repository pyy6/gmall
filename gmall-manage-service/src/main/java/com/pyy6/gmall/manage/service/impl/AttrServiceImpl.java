package com.pyy6.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyy6.gmall.bean.BaseAttrInfo;
import com.pyy6.gmall.bean.BaseAttrValue;
import com.pyy6.gmall.manage.mapper.BaseAttrInfoMapper;
import com.pyy6.gmall.manage.mapper.BaseAttrValueMapper;
import com.pyy6.gmall.service.AttrService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;
    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {

        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);

        return baseAttrInfoMapper.select(baseAttrInfo);
    }

    @Override
    public void saveAttr(BaseAttrInfo baseAttrInfo) {
        //两个作用，返回在数据库生成的主键、有几个参数就增加几个
        baseAttrInfoMapper.insertSelective(baseAttrInfo);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        //为即将插入baseAttrValue的列表添加baseAttrInfo的主键
        for (BaseAttrValue baseAttrValue : attrValueList) {
            baseAttrValue.setAttrId(baseAttrInfo.getId());
            baseAttrValueMapper.insert(baseAttrValue);
        }
    }

    @Override
    public List<BaseAttrInfo> getAttrListByCtg3Id(String catalog3Id) {

        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.select(baseAttrInfo);

        for(BaseAttrInfo baseAttrInfo1 : baseAttrInfoList) {
            BaseAttrValue baseAttrValue = new BaseAttrValue();
            baseAttrValue.setAttrId(baseAttrInfo1.getId());
            List<BaseAttrValue> baseAttrValueInfoList = baseAttrValueMapper.select(baseAttrValue);
            baseAttrInfo1.setAttrValueList(baseAttrValueInfoList);
        }
        return baseAttrInfoList;
    }

    @Override
    public List<BaseAttrInfo> getAttrListByValueIds(String join) {

        List<BaseAttrInfo> baseAttrInfos = baseAttrValueMapper.selectAttrListByValueIds(join);
        return baseAttrInfos;
    }
}
