package com.pyy6.gmall.manage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {
//页面跳转
    @RequestMapping("spuListPage")
    public String spuListPage(){

        return "spuListPage";
    }

    @RequestMapping("attListPage")
    public String attListPage(){

        return "attListPage";
    }

    @RequestMapping("index")
    public String index(){

        return "index";
    }
}
