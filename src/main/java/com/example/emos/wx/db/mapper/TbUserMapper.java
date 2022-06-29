package com.example.emos.wx.db.mapper;

import com.example.emos.wx.db.pojo.TbUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 2657944563
* @description 针对表【tb_user(用户表)】的数据库操作Mapper
* @createDate 2022-06-29 16:33:12
* @Entity com.example.emos.wx.db.pojo.TbUser
*/
@Mapper
public interface TbUserMapper extends BaseMapper<TbUser> {

}




