package com.binfast.boottest.service;

import com.binfast.adpter.core.annotations.GetApiMapping;
import com.binfast.boottest.controller.PlatformException;
import org.springframework.stereotype.Service;

// DEMO

/**
 * @apiNote 用户信息
 */
@Service
public class UserServiceImpl implements UserService{

    /**
     * @apiNote 获取用户资料
     * @param userId 用户标识
     * @return 用户信息
     */
    @Override
    @GetApiMapping(value = "/getUser")
    public UserInfo getUser(Long userId) {
        if (userId == 1) {
            throw new PlatformException("参数 'userId'不能为空");
        }
        return mock();
    }

    @Override
    public UserInfo saveUser(UserInfo userInfo) {
        return mock();
    }

    /**
     * @apiNote 用户登录
     * @param userId 用户标识
     * @param password 用户密码
     * @return 用户信息
     */
    @Override
    @GetApiMapping(value = "/login/{userId}/{password}")
    public UserInfo login(Long userId, String password)  {
        System.out.println("userId: "+userId);
        System.out.println("password: "+password);
        return mock();
    }

    public UserInfo mock() {
        UserInfo info = new UserInfo();
        info.setName("小明");
        info.setSex("男");
        info.setUserId(111L);
        info.setIdcard("430527198108145443");
        return info;
    }

}
