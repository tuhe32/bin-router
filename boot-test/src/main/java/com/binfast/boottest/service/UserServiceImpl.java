package com.binfast.boottest.service;

import com.alibaba.cola.exception.BizException;
import com.binfast.adpter.core.annotations.ApiMapping;
import com.binfast.adpter.core.annotations.GetApiMapping;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public UserInfo getUser(String userId,String username) {
        if (userId == null) {
            throw new BizException("参数 'userId'不能为空");
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
    public UserInfo login(@PathVariable String userId, @PathVariable String password)  {
        System.out.println("userId"+userId);
        System.out.println("password"+password);
        return mock();
    }

}
