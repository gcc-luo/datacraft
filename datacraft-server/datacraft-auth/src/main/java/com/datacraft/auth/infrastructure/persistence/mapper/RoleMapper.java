package com.datacraft.auth.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datacraft.auth.infrastructure.persistence.entity.RoleEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<RoleEntity> {
    @Select("SELECT r.code FROM dc_role r JOIN dc_user_role ur ON ur.role_id = r.id WHERE ur.user_id = #{userId} AND r.enabled = TRUE ORDER BY r.code")
    List<String> findCodesByUserId(Long userId);

    @Select("SELECT id FROM dc_role WHERE code = #{code} AND enabled = TRUE")
    Long findIdByCode(String code);

    @Insert("INSERT INTO dc_user_role(user_id, role_id) VALUES (#{userId}, #{roleId}) ON CONFLICT DO NOTHING")
    int insertUserRole(Long userId, Long roleId);

    @Select("SELECT id, code, name, enabled, created_at, updated_at FROM dc_role ORDER BY id")
    List<RoleEntity> findForAdmin();

    @Select("SELECT id, code, name, enabled, created_at, updated_at FROM dc_role WHERE id = #{id}")
    RoleEntity findForAdminById(Long id);

    @Select({"<script>", "SELECT id FROM dc_role WHERE code IN", "<foreach item='code' collection='codes' open='(' separator=',' close=')'>#{code}</foreach>", "</script>"})
    List<Long> findIdsByCodes(@Param("codes") List<String> codes);

    @Select("SELECT menu_id FROM dc_role_menu WHERE role_id = #{roleId} ORDER BY menu_id")
    List<Long> findMenuIdsByRoleId(Long roleId);

    @Delete("DELETE FROM dc_user_role WHERE user_id = #{userId}")
    int deleteUserRoles(Long userId);

    @Delete("DELETE FROM dc_role_menu WHERE role_id = #{roleId}")
    int deleteRoleMenus(Long roleId);

    @Insert("INSERT INTO dc_role_menu(role_id, menu_id) VALUES (#{roleId}, #{menuId}) ON CONFLICT DO NOTHING")
    int insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    @Update("UPDATE dc_role SET code = #{code}, name = #{name}, enabled = #{enabled}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateAdmin(@Param("id") Long id, @Param("code") String code, @Param("name") String name,
                    @Param("enabled") Boolean enabled);
}
