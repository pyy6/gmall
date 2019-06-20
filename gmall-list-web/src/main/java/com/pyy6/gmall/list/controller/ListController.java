package com.pyy6.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyy6.gmall.bean.*;
import com.pyy6.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    SearchService searchService;

    @RequestMapping("list")
    public String search(SkuLsParam skuLsParam, ModelMap map){

        List<SkuLsInfo> skuLsInfos = searchService.search(skuLsParam);
        map.put("skuLsInfoList",skuLsInfos);

        //封装平台属性的列表,并且排除已经选中的属性
        List<BaseAttrInfo> baseAttrInfos = searchService.getAttrValueIds(skuLsInfos);
        //在list页面点击平台属性，并且排除已经选中的属性
        String[] valueId = skuLsParam.getValueId();
        //制作面包屑
        List<Crumb> attrValueSelectedList = new ArrayList<>();
        if(valueId != null && valueId.length > 0){
            for (String s : valueId) {
                Crumb crumb = new Crumb();
                //制作面包屑url
                String urlParam = getUrlParamForCeumb(skuLsParam,s);
                //制作面包屑名字
                String valueName = "";
                Iterator<BaseAttrInfo> iterator = baseAttrInfos.iterator();
                while(iterator.hasNext()){
                    BaseAttrInfo baseAttrInfo = iterator.next();
                    List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                    for (BaseAttrValue baseAttrValue : attrValueList) {
                            if(baseAttrValue.getId().equals(s)){
                                valueName = baseAttrInfo.getAttrName();
                                crumb.setUrlParam(urlParam);
                                crumb.setValueName(valueName);
                                attrValueSelectedList.add(crumb);
                                iterator.remove();
                            }
                    }
                }

            }
        }
        map.put("attrList",baseAttrInfos);

        //点击属性列表制作新的请求
        String urlParam = getUrlParam(skuLsParam);
        map.put("urlParam",urlParam);


        map.put("attrValueSelectedList",attrValueSelectedList);

        return "list";
    }
    //制作普通url
    private String getUrlParam(SkuLsParam skuLsParam) {

        String keyword = skuLsParam.getKeyword();
        //过滤条件（属性值id valueId、三级分类id）
        String[] valueId = skuLsParam.getValueId();
        String catalog3Id = skuLsParam.getCatalog3Id();

        String urlParam = "";
        if(valueId != null && valueId.length > 0){
            for (String s : valueId) {
                urlParam += "&valueId=" + s;
            }
        }
        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam += "&catalog3Id=" + catalog3Id;
            }
            urlParam +=  "catalog3Id=" + catalog3Id;
        }
        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(urlParam)) {
                urlParam += "&keyword=" + keyword;
            }
            urlParam += "keyword=" + keyword;
        }
        return urlParam;
    }
    //制作面包屑url
    private String getUrlParamForCeumb(SkuLsParam skuLsParam,String id) {

        String keyword = skuLsParam.getKeyword();
        //过滤条件（属性值id valueId、三级分类id）
        String[] valueId = skuLsParam.getValueId();
        String catalog3Id = skuLsParam.getCatalog3Id();

        String urlParam = "";
        if(valueId != null && valueId.length > 0){
            for (String s : valueId) {
                if(!id.equals(s)){
                    urlParam += "&valueId=" + s;
                }
            }
        }
        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam += "&catalog3Id=" + catalog3Id;
            }
            urlParam +=  "catalog3Id=" + catalog3Id;
        }
        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(urlParam)) {
                urlParam += "&keyword=" + keyword;
            }
            urlParam += "keyword=" + keyword;
        }
        return urlParam;
    }
    @RequestMapping("index")
    public String index(){
        return "index";
    }
}
