package com.pyy6.gmall.user.mapper;

import com.pyy6.gmall.bean.UserInfo;
import tk.mybatis.mapper.common.Mapper;
//继承通用mapper可以避免写sql语句
public interface UserInfoMapper extends Mapper<UserInfo> {
}
