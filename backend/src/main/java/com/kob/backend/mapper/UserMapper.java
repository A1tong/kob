package com.kob.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kob.backend.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper // @Mapper是个Mybatis看的，让他实现这个接口类并实例化一个Bean对象
public interface UserMapper extends BaseMapper<User> {
    // 接口UserMapper继承于BaseMapper，免费获得十几要做什么的逻辑，具体类是由Mybatis实现的
    // <User>被Mybatis读取后会按照默认规则操作指定的数据库表，User的字段根据默认规则一一对应数据表的列
}
