package com.example.emos.wx.controller.from;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 前端传递地址信息表格
 *
 * @author 2657944563
 */
@Data
@ApiModel
public class CheckinForm {
    private String address;
    private String country;
    private String province;
    private String city;
    private String district;

}
