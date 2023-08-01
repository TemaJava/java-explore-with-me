package ru.practicum.ewm.rating.service;

import ru.practicum.ewm.rating.dto.RatingDto;

public interface RatingService {
    void deleteRate(Long userId, Long ratingId);

    RatingDto createRate(Long userId, RatingDto ratingDto);
}
