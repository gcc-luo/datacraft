package io.datacraft.quality.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.datacraft.quality.infrastructure.persistence.entity.QualitySampleEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QualitySampleMapper extends BaseMapper<QualitySampleEntity> {
    @Insert("INSERT INTO dc_quality_sample (quality_result_id, sample_index, data_json, created_at) "
            + "VALUES (#{qualityResultId}, #{sampleIndex}, CAST(#{dataJson} AS jsonb), #{createdAt})")
    int insertJson(QualitySampleEntity entity);

    @Select("SELECT id, quality_result_id, sample_index, data_json::text AS data_json, created_at "
            + "FROM dc_quality_sample WHERE quality_result_id = #{resultId} ORDER BY sample_index")
    List<QualitySampleEntity> selectByResultId(Long resultId);
}
