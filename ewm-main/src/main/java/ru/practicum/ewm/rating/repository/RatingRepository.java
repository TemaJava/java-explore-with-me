package ru.practicum.ewm.rating.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.rating.model.Rating;

import java.util.List;
import java.util.Optional;


public interface RatingRepository extends JpaRepository<Rating, Long> {
 
    Optional<Rating> findByIdAndUserId(Long ratingId, Long userId);
    
    List<Rating> findAllByEventIdIn(@Param("eventId") List<Long> eventId);
}
