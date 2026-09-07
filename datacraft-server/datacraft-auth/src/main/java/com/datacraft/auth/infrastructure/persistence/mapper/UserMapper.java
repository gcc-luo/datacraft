package com.datacraft.auth.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datacraft.auth.infrastructure.persistence.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    @Select("SELECT id, username, display_name, password_hash, enabled, created_at, updated_at FROM dc_user WHERE username = #{username}")
    UserEntity findByUsername(String username);

    @Select("SELECT id, username, display_name, password_hash, enabled, created_at, updated_at FROM dc_user ORDER BY id")
    List<UserEntity> findForAdmin();

    @Select("SELECT id, username, display_name, password_hash, enabled, created_at, updated_at FROM dc_user WHERE id = #{id}")
    UserEntity findForAdminById(Long id);

    @Update("UPDATE dc_user SET username = #{username}, display_name = #{displayName}, password_hash = #{passwordHash}, enabled = #{enabled}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateAdmin(@Param("id") Long id, @Param("username") String username,
                    @Param("displayName") String displayName, @Param("passwordHash") String passwordHash,
                    @Param("enabled") Boolean enabled);
}
