package com.pyy6.gmall.list;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyy6.gmall.bean.SkuInfo;
import com.pyy6.gmall.bean.SkuLsInfo;
import com.pyy6.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.beanutils.BeanUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

    @Autowired
    JestClient jestClient;

    @Reference
    SkuService skuService;

    @Test
    public void search(){

        ////如何查询es中的数据
        List<SkuLsInfo> skuLsInfos = new ArrayList<>();
        Search search = new Search.Builder(getMyDsl()).addIndex("gmall").addType("SkuLsInfo").build();
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
    }

    private static  String getMyDsl() {//与kibana上拼接的dsl 语句一样

        //query - filter - term
        //query - must - match

        //生成dsl对象的工具
        SearchSourceBuilder dslBuilder = new SearchSourceBuilder();
        //创建一个先过滤后搜索的bool query对象
        BoolQueryBuilder query = new BoolQueryBuilder();

        //过滤
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id","1");
        query.filter(termQueryBuilder);

        TermQueryBuilder termQueryBuilder1 = new TermQueryBuilder("skuAttrValueList.valueId","71");
        query.filter(termQueryBuilder1);

        //搜索
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", "华为");
        query.must(matchQueryBuilder);

        //将query放入dsl工具对象
        dslBuilder.query(query);
        dslBuilder.size(3);
        dslBuilder.from(0);
//        dslBuilder.highlight();

        System.out.println(dslBuilder.toString());
        return dslBuilder.toString();
    }

    @Test
    public void contextLoads() {

        //查询mysql中的sku信息
        List<SkuInfo> list = skuService.getSkuInfoListByCatalog3Id("1");

        //转化es中的sku信息
        List<SkuLsInfo> skuLsInfos = new ArrayList<>();

        for (SkuInfo skuInfo : list) {

            SkuLsInfo skuLsInfo = new SkuLsInfo();

            try {
                BeanUtils.copyProperties(skuLsInfo,skuInfo);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            skuLsInfos.add(skuLsInfo);
        }
        //导入到es中
        for (SkuLsInfo skuLsInfo : skuLsInfos) {
            Index build = new Index.Builder(skuLsInfo).index("gmall").type("SkuLsInfo").id(skuLsInfo.getId()).build();
            try {
                jestClient.execute(build);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
