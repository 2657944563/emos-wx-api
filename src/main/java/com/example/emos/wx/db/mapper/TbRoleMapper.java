package com.example.emos.wx.db.mapper;

import com.example.emos.wx.db.pojo.TbRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 2657944563
* @description 针对表【tb_role(角色表)】的数据库操作Mapper
* @createDate 2022-06-29 16:33:12
* @Entity com.example.emos.wx.db.pojo.TbRole
*/
@Mapper
public interface TbRoleMapper extends BaseMapper<TbRole> {
    @Select("select * from tb_role")
    public List<TbRole> selectAllByIdTbRole();
}




