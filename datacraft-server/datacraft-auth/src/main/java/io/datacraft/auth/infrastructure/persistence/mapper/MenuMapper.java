package io.datacraft.auth.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.datacraft.auth.infrastructure.persistence.entity.MenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<MenuEntity> {
    @Select({
            "<script>",
            "SELECT DISTINCT m.id, m.code, m.title, m.path, m.icon, m.parent_id, m.sort_order, m.enabled",
            "FROM dc_menu m JOIN dc_role_menu rm ON rm.menu_id = m.id JOIN dc_role r ON r.id = rm.role_id",
            "WHERE m.enabled = TRUE AND r.enabled = TRUE AND r.code IN",
            "<foreach item='role' collection='roles' open='(' separator=',' close=')'>#{role}</foreach>",
            "ORDER BY m.parent_id NULLS FIRST, m.sort_order, m.id",
            "</script>"
    })
    List<MenuEntity> findForRoles(List<String> roles);
}
