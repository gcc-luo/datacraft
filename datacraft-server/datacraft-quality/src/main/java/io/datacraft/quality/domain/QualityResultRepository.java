package io.datacraft.quality.domain;

import java.util.List;
import java.util.Optional;

public interface QualityResultRepository {
    QualityResult save(QualityResult result);
    QualitySample save(QualitySample sample);
    List<QualityResult> findAll();
    Optional<QualityResult> findById(Long id);
    List<QualitySample> findSamples(Long resultId);
}
