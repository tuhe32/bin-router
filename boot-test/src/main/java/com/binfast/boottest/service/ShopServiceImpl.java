package com.binfast.boottest.service;

import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * @author 刘斌
 * @date 2022/11/14 4:20 下午
 */
@Service
public class ShopServiceImpl {

    public String addShop() {
        return "success";
    }

    public Shop getDetail(Long id) {
        Shop shop = new Shop();
        shop.setId(id);
        shop.setName("托尔斯泰");
        return shop;
    }

    public static class Shop implements Serializable {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
