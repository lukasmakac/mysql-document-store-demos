package com.boyzoid.domain.dto;

public class ScoreDto {
  private Long id;

  private String username;
  private Long score;

  public ScoreDto() {
  }

  public ScoreDto(Long id, Long score) {
    this.id = id;
    this.score = score;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getScore() {
    return score;
  }

  public void setScore(Long score) {
    this.score = score;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
