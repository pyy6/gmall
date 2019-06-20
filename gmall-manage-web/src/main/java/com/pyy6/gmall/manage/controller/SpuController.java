package com.pyy6.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyy6.gmall.bean.*;
import com.pyy6.gmall.manage.util.MyUploadUtil;
import com.pyy6.gmall.service.AttrService;
import com.pyy6.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class SpuController {

    @Reference
    SpuService spuService;


    @RequestMapping("getSpuImgListBySpuId")
    @ResponseBody
    public List<SpuImage> getSpuImgListBySpuId(String spuId){
        List<SpuImage> list = spuService.getSpuImgListBySpuId(spuId);
        return list;
    }

    @RequestMapping("getSaleAttrListBySpuId")
    @ResponseBody
    public List<SpuSaleAttr> getSaleAttrListBySpuId(String spuId){
        List<SpuSaleAttr> list = spuService.getSaleAttrListBySpuId(spuId);
        return list;
    }
    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile file){
    //fdfs的上传工具，把文件对象存到后台服务器上
        String imgUrl = MyUploadUtil.uploadImg(file);
        return imgUrl;
    }

    @RequestMapping("saveSpu")
    @ResponseBody
    public String saveSpu(SpuInfo spuInfo){

        spuService.saveSpu(spuInfo);
        return "success";
    }

    @RequestMapping("spuList")
    @ResponseBody
    public List<SpuInfo> spuList(String catalog3Id){

        List<SpuInfo> list = spuService.spuList(catalog3Id);
        return list;
    }

    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> baseSaleAttrList(){

        List<BaseSaleAttr> list = spuService.baseSaleAttrList();
        return list;
    }

}
