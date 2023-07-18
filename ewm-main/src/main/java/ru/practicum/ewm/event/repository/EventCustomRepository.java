package ru.practicum.ewm.event.repository;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventCustomRepository {
    private final EntityManager entityManager;

    public List<Event> findEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                  String rangeStart, String rangeEnd, Integer from, Integer size) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> eventCriteriaQuery = criteriaBuilder.createQuery(Event.class);
        Root<Event> eventRoot = eventCriteriaQuery.from(Event.class);
        List<Predicate> predicateList = new ArrayList<>();

        TypedQuery<Event> typedQuery = entityManager.createQuery(eventCriteriaQuery);
        typedQuery.setFirstResult(from);
        typedQuery.setMaxResults(size);

        if (users != null) {
            predicateList.add(criteriaBuilder.and(eventRoot.get("initiator").get("id").in(users)));
        }

        if (categories != null) {
            predicateList.add(criteriaBuilder.and(eventRoot.get("category").get("id").in(categories)));
        }

        if (states != null) {
            predicateList.add(criteriaBuilder.and(states.stream()
                    .map(eventState -> criteriaBuilder.equal(eventRoot.get("eventState"), eventState))
                    .toArray(Predicate[]::new)));
        }

        if (rangeStart != null && rangeEnd != null) {
            LocalDateTime start = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime end = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            predicateList.add(criteriaBuilder.and(criteriaBuilder.between(eventRoot.get("eventDate"), start, end)));
        }

        return entityManager.createQuery(eventCriteriaQuery.select(eventRoot)
                .where(predicateList.toArray(Predicate[]::new))).getResultList();
    }
}
