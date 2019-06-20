package com.pyy6.gmall.user.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pyy6.gmall.bean.UserAddress;
import com.pyy6.gmall.bean.UserInfo;
import com.pyy6.gmall.service.UserService;
import com.pyy6.gmall.user.mapper.UserAddressMapper;
import com.pyy6.gmall.user.mapper.UserInfoMapper;
import com.pyy6.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;


import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired//这个注解还是spring中的注解，因为在这个service所在的spring容器中就有，
    UserInfoMapper userInfoMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    UserAddressMapper userAddressMapper;

    @Override
    public List<UserInfo> userInfoList() {
        return userInfoMapper.selectAll();
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        UserInfo userInfo1 = userInfoMapper.selectOne(userInfo);
        if(userInfo1 != null) {
            //同步缓存，下一次别的系统可以直接去这个数据
            Jedis jedis = redisUtil.getJedis();
            jedis.set("user:" + userInfo1.getId() + ":info", JSON.toJSONString(userInfo1));
            jedis.close();
        }
        return userInfo1;
    }

    @Override
    public List<UserAddress> getAddrListByUserId(String userId) {

        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        List<UserAddress> select = userAddressMapper.select(userAddress);
        return select;
    }
}
