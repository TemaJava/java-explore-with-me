package ru.practicum.ewm.rating.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.rating.dto.RatingDto;
import ru.practicum.ewm.rating.model.Rating;

@UtilityClass
public class RatingMapper {
    public Rating toRating(RatingDto ratingDto) {
        return new Rating(ratingDto.getId(), ratingDto.getUserId(), null,
                ratingDto.getRate());
    }

    public RatingDto toRatingDto(Rating rating) {
        return new RatingDto(rating.getId(), rating.getUserId(),
                rating.getEvent().getId(), rating.getRate());
    }
}
