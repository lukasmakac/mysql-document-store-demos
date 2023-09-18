package com.boyzoid.mapper;

import com.boyzoid.domain.dto.ScoreDto;
import com.boyzoid.domain.entity.Score;
import jakarta.inject.Singleton;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "jakarta")
@Singleton
public interface ScoreMapper {

  ScoreDto toDto(Score entity);
  Score toEntity(ScoreDto dto);
  List<ScoreDto> toDtoList(List<Score> entities);

}
