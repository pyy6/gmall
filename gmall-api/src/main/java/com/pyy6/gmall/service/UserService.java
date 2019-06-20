package com.pyy6.gmall.service;
import com.pyy6.gmall.bean.UserAddress;
import com.pyy6.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {
    List<UserInfo> userInfoList();

    UserInfo login(UserInfo userInfo);

    List<UserAddress> getAddrListByUserId(String userId);
}
