package com.datacraft.auth.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datacraft.auth.infrastructure.persistence.entity.MenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

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

    @Select("SELECT m.id, m.code, m.title, m.path, m.icon, m.parent_id, p.title AS parent_title, m.sort_order, m.enabled, m.created_at, m.updated_at FROM dc_menu m LEFT JOIN dc_menu p ON p.id = m.parent_id ORDER BY m.parent_id NULLS FIRST, m.sort_order, m.id")
    List<MenuEntity> findForAdmin();

    @Select("SELECT m.id, m.code, m.title, m.path, m.icon, m.parent_id, p.title AS parent_title, m.sort_order, m.enabled, m.created_at, m.updated_at FROM dc_menu m LEFT JOIN dc_menu p ON p.id = m.parent_id WHERE m.id = #{id}")
    MenuEntity findForAdminById(Long id);

    @Insert("INSERT INTO dc_menu(code, title, path, icon, parent_id, sort_order, enabled) VALUES (#{code}, #{title}, #{path}, #{icon}, #{parentId}, #{sortOrder}, #{enabled})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAdmin(MenuEntity entity);

    @Update("UPDATE dc_menu SET code = #{code}, title = #{title}, path = #{path}, icon = #{icon}, parent_id = #{parentId}, sort_order = #{sortOrder}, enabled = #{enabled}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateAdmin(@Param("id") Long id, @Param("code") String code, @Param("title") String title,
                    @Param("path") String path, @Param("icon") String icon, @Param("parentId") Long parentId,
                    @Param("sortOrder") Integer sortOrder, @Param("enabled") Boolean enabled);
}
