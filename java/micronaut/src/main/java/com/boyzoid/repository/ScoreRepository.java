package com.boyzoid.repository;

import com.boyzoid.domain.entity.Score;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
public interface ScoreRepository extends CrudRepository<Score, Long> {

}
