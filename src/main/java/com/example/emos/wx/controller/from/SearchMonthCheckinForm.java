package com.example.emos.wx.controller.from;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class SearchMonthCheckinForm {
    @NotBlank(message = "年月不能为空")
    @Range(min = 2000, max = 3000)
    private Integer year;
    @NotBlank(message = "年月不能为空")
    @Range(min = 1, max = 12)
    private Integer month;
}
