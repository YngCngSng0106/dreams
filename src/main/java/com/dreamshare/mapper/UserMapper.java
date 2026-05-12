package com.dreamshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dreamshare.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
