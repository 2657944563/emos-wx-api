package com.example.emos.wx.controller.from;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author 2657944563
 */
@ApiModel
@Data
public class RegisterFrom {

    @NotBlank(message = "微信临时授权码不能为空")
    private String code;
    @Pattern(regexp = "^[0-9]{6}&]", message = "注册码为6位数字")
    @NotBlank(message = "注册码不能为空")
    private String registerCode;
    @NotBlank(message = "昵称不能为空")
    private String nickName;
    @NotBlank(message = "头像地址不能为空")
    private String url;
}
