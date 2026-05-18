package com.dreamshare.service;

import com.dreamshare.entity.User;
import com.dreamshare.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired private UserMapper userMapper;

    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }
}
