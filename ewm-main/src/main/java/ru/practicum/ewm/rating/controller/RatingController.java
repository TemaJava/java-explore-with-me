package ru.practicum.ewm.rating.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.rating.dto.RatingDto;
import ru.practicum.ewm.rating.service.RatingService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/rating")
public class RatingController {
    private final RatingService service;

    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public RatingDto createRate(@Positive @PathVariable Long userId,
                                @Valid @RequestBody RatingDto ratingDto) {
        if (ratingDto.getRate() < 1 || ratingDto.getRate() > 10) {
            throw new BadRequestException("Ошибка оценки");
        }
        return service.createRate(userId, ratingDto);
    }

    @DeleteMapping("/{userId}/{ratingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRate(@Positive @PathVariable Long userId,
                           @Positive @PathVariable Long ratingId) {
        service.deleteRate(userId, ratingId);
    }
}
