package com.boyzoid.controller;

import com.boyzoid.domain.dto.ScoreDto;
import com.boyzoid.repository.ScoreRepository;
import com.boyzoid.service.ScoreService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;

import java.util.List;

@Controller("/repo")
public class ScoreController {


    @Inject
    ScoreService scoreService;

    @Get(value = "/list", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<List<ScoreDto>> listAll() {
        return HttpResponse.ok(scoreService.getAllScoresByRepository());
    }
}
