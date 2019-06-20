package com.pyy6.gmall.manage.mapper;

import com.pyy6.gmall.bean.BaseAttrInfo;
import com.pyy6.gmall.bean.BaseAttrValue;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrValueMapper extends Mapper<BaseAttrValue> {
    List<BaseAttrInfo> selectAttrListByValueIds(@Param("ids") String join);
}
