package com.pyy6.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyy6.gmall.bean.UserInfo;
import com.pyy6.gmall.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {
    @Reference//现在所在的spring没有了service，需要远程注入=RPC远程调用的代理，也就是存储了远程调用路径的信息（服务地址：dubbo：//......）
    UserService userService;

    @RequestMapping("userInfoList")
//    @ResponseBody
    public ResponseEntity<List<UserInfo>> userInfoList(){

        List<UserInfo> userInfoList = userService.userInfoList();
        return ResponseEntity.ok(userInfoList);
    }
}
