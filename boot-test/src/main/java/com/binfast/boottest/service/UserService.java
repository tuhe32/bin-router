package com.binfast.boottest.service;

import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * @author εζ
 * @date 2022/11/28 3:06 δΈε
 */
@Validated
public interface UserService {

    UserInfo getUser(@NotNull @Min(1) Long userId);

    UserInfo saveUser(@NotNull @Valid UserInfo userInfo);

    UserInfo login(@NotNull @Positive Long userId, @NotNull String password);
}
