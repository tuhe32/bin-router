package com.binfast.boottest.service;

import com.binfast.adpter.core.annotations.GetApiMapping;
import com.binfast.boottest.controller.PlatformException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

// DEMO

/**
 * @apiNote 用户信息
 */
@Service
public class UserServiceImpl {

    /**
     * @apiNote 获取用户资料
     * @param userId
     * @param username
     * @return
     */
    @GetApiMapping(value = "/getUser/{userId}", notes = "获取用户资料")
    public UserInfo getUser(Long userId,String username) {
        if (userId == 1) {
            throw new PlatformException("参数 'userId'不能为空");
        }
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

//    @ApiMapping(value = "bit.api.user.login")
    @GetApiMapping(value = "/login/{userId}/{password}", notes = "用户登录中")
    public UserInfo login(@PathVariable Long userId, @PathVariable String password)  {
        System.out.println("userId: "+userId);
        System.out.println("password: "+password);
        return mock();
    }

}
