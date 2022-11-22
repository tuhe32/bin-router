package com.binfast.boottest.controller;

import com.binfast.boottest.service.ShopServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author 刘斌
 * @date 2022/11/14 4:24 下午
 * @apiNote 店铺管理
 */
@RestController
@RequestMapping("/api/shop")
public class ShopController {

    @Autowired
    private ShopServiceImpl shopService;

    /**
     * @apiNote 增加店铺
     */
    @PostMapping("/addSHop")
    public String addShop(ShopServiceImpl.Shop shop) {
        return shopService.addShop();
    }

    @GetMapping("/shopDetail/{id}")
    public ShopServiceImpl.Shop shopDetail(@PathVariable Long id) {
        return shopService.getDetail(id);
    }
}
