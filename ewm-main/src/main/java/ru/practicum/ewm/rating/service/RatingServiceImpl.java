package ru.practicum.ewm.rating.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.rating.dto.RatingDto;
import ru.practicum.ewm.rating.mapper.RatingMapper;
import ru.practicum.ewm.rating.model.Rating;
import ru.practicum.ewm.rating.repository.RatingRepository;
import ru.practicum.ewm.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public void deleteRate(Long userId, Long ratingId) {
        userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException("User not found"));
        Rating rating = ratingRepository.findByIdAndUserId(ratingId, userId).orElseThrow(() ->
                new BadRequestException("You haven't this rate"));
        ratingRepository.delete(rating);
    }

    @Override
    public RatingDto createRate(Long userId, RatingDto ratingDto) {
        Event event = eventRepository.findById(ratingDto.getEventId())
                .orElseThrow(() -> new ObjectNotFoundException("Event not found"));
        userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException("User not found"));
        if (event.getEventState() != EventState.PUBLISHED) {
            throw new BadRequestException("You cant rate not published events");
        }
        Rating rating = RatingMapper.toRating(ratingDto);
        rating.setEvent(event);
        rating.setUserId(userId);
        return RatingMapper.toRatingDto(ratingRepository.save(rating));
    }
}
