package io.datacraft.auth.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.datacraft.auth.infrastructure.persistence.entity.RoleEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<RoleEntity> {
    @Select("SELECT r.code FROM dc_role r JOIN dc_user_role ur ON ur.role_id = r.id WHERE ur.user_id = #{userId} AND r.enabled = TRUE ORDER BY r.code")
    List<String> findCodesByUserId(Long userId);

    @Select("SELECT id FROM dc_role WHERE code = #{code} AND enabled = TRUE")
    Long findIdByCode(String code);

    @Insert("INSERT INTO dc_user_role(user_id, role_id) VALUES (#{userId}, #{roleId}) ON CONFLICT DO NOTHING")
    int insertUserRole(Long userId, Long roleId);
}
