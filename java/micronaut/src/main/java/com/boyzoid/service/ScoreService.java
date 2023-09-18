package com.boyzoid.service;

import com.boyzoid.domain.dto.ScoreDto;
import com.boyzoid.mapper.ScoreMapper;
import com.boyzoid.repository.ScoreRepository;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class ScoreService {
    private final ScoreRepository scoreRepository;
    private final ScoreMapper scoreMapper;

    public ScoreService(ScoreRepository scoreRepository, ScoreMapper scoreMapper) {
        this.scoreRepository = scoreRepository;
        this.scoreMapper = scoreMapper;
    }

    public List<ScoreDto> getAllScoresByRepository() {
        return StreamSupport.stream(scoreRepository.findAll().spliterator(), false)
                .map(scoreMapper::toDto)
                .collect(Collectors.toList());
    }
}
