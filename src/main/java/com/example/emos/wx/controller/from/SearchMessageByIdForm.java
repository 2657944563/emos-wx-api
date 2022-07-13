package com.example.emos.wx.controller.from;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class SearchMessageByIdForm {
    @NotBlank
    private String id;
}
