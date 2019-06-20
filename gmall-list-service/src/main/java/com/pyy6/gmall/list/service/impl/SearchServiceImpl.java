package com.pyy6.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.pyy6.gmall.bean.BaseAttrInfo;
import com.pyy6.gmall.bean.SkuLsAttrValue;
import com.pyy6.gmall.bean.SkuLsInfo;
import com.pyy6.gmall.bean.SkuLsParam;
import com.pyy6.gmall.service.AttrService;
import com.pyy6.gmall.service.SearchService;
import com.pyy6.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    JestClient jestClient;

    @Reference
    AttrService attrService;

    @Override
    public List<SkuLsInfo> search(SkuLsParam skuLsParam) {
        ////如何查询es中的数据
        List<SkuLsInfo> skuLsInfos = new ArrayList<>();
        Search search = new Search.Builder(getMyDsl(skuLsParam)).addIndex("gmall").addType("SkuLsInfo").build();
        try {
            SearchResult execute = jestClient.execute(search);//hits
            List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                SkuLsInfo skuLsInfo =  hit.source;
                skuLsInfos.add(skuLsInfo);
            }
            System.out.println(skuLsInfos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return skuLsInfos;
    }

    @Override
    public List<BaseAttrInfo> getAttrValueIds(List<SkuLsInfo> skuLsInfos) {

        Set<String> valueIds = new HashSet<>();
        for (SkuLsInfo skuLsInfo : skuLsInfos) {
            List<SkuLsAttrValue> skuAttrValueList = skuLsInfo.getSkuAttrValueList();
            for (SkuLsAttrValue skuLsAttrValue : skuAttrValueList) {
                String valueId = skuLsAttrValue.getValueId();
                valueIds.add(valueId);
            }
        }
        //根据去重后的valueid集合检索（平台属性id，平台属性名称，valueid对应的值），关联到平台属性列表
        List<BaseAttrInfo> baseAttrInfos = null;

        String join = StringUtils.join(valueIds, ",");
        baseAttrInfos = attrService.getAttrListByValueIds(join);

        return baseAttrInfos;
    }

    private static  String getMyDsl(SkuLsParam skuLsParam) {//与kibana上拼接的dsl 语句一样

        //搜索关键字(skuName、skuDesc)
        String keyword = skuLsParam.getKeyword();
        //过滤条件（属性值id valueId、三级分类id）
        String[] valueId = skuLsParam.getValueId();
        String catalog3Id = skuLsParam.getCatalog3Id();

        //query - filter - term
        //query - must - match

        //生成dsl对象的工具
        SearchSourceBuilder dslBuilder = new SearchSourceBuilder();
        //创建一个先过滤后搜索的bool query对象
        BoolQueryBuilder query = new BoolQueryBuilder();

        //过滤
        if(valueId != null && valueId.length > 0){
            //如果需要查询条件的并集就直接到terms
//            TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("catalog3Id","1");
//            query.filter(termsQueryBuilder);

            //如果需要查询条件的交集，就需要循环
            for (int i = 0; i < valueId.length; i++) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId",valueId[i]);
                query.filter(termQueryBuilder);
            }
        }
        if(StringUtils.isNotBlank(catalog3Id)){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",catalog3Id);
            query.filter(termQueryBuilder);
        }

        //搜索(默认交集)
        if(StringUtils.isNotBlank(keyword)){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", keyword);
            query.must(matchQueryBuilder);
        }

        //将query放入dsl工具对象
        dslBuilder.query(query);
        dslBuilder.size(3);
        dslBuilder.from(0);
//        dslBuilder.highlight();

        System.out.println(dslBuilder.toString());
        return dslBuilder.toString();
    }
}
