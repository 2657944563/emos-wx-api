package com.example.emos.wx.controller.from;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

@Data
@ApiModel
public class SearchMonthCheckinForm {
    @NotNull(message = "年月不能为空")
    @Range(min = 2000, max = 3000)
//    @Min(message = "低于2000年",value = 2000)
//    @Max(message = "高于2000年" , value = 3000)
    private Integer year;
    @NotNull(message = "年月不能为空")
    @Range(min = 1, max = 12)
//    @Min(message = "低于1月",value = 1)
//    @Max(message = "高于12月" , value = 12)
    private Integer month;
}
