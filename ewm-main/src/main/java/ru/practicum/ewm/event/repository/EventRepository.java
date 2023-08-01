package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("select event from Event event " +
            "where event.id IN (:ids)")
    List<Event> findByIds(@Param("ids") List<Long> ids);

    List<Event> findAllByInitiatorId(long initiatorId, Pageable pageable);

    Optional<Event> findByInitiatorIdAndId(Long userId, Long eventId);

    @Query("SELECT e " +
            "FROM Event AS e " +
            "WHERE " +
            "(" +
            ":text IS NULL " +
            "OR LOWER(e.description) LIKE CONCAT('%', :text, '%') " +
            "OR LOWER(e.annotation) LIKE CONCAT('%', :text, '%')" +
            ")" +
            "AND (:states IS NULL OR e.eventState IN (:states)) " +
            "AND (:categories IS NULL OR e.category.id IN (:categories)) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (CAST(:rangeStart AS date) IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (CAST(:rangeEnd AS date) IS NULL OR e.eventDate <= :rangeEnd) " +
            "order by e.eventDate")
    List<Event> findByParamsOrderByDate(@Param("text") String text, @Param("states") List<EventState> states,
            @Param("categories") List<Long> categories, @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart, @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    List<Event> findAllByIdIn(List<Long> eventIds);

    @Query("SELECT e FROM Event e " +
            "WHERE (:users IS NULL OR e.initiator.id IN (:users)) " +
            "AND (:states IS NULL OR e.eventState IN (:states)) " +
            "AND (:categories IS NULL OR e.category.id IN (:categories)) " +
            "AND (CAST(:rangeStart AS date) IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (CAST(:rangeEnd AS date) IS NULL OR e.eventDate <= :rangeEnd) ")
    List<Event> findEventsByParams(
            @Param("users") List<Long> users,
            @Param("states") List<EventState> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);
}
