package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByRequesterId(Long userId);

    Optional<Request> findByIdAndRequesterId(Long requestId, Long userId);

    @Query("SELECT request FROM Request request " +
            "WHERE request.event.id = :eventId " +
            "AND request.requester.id = :userId")
    Optional<Request> findByEventIdAndRequesterId(@Param("eventId") Long eventId,
                                                               @Param("userId") Long userId);

    @Query("SELECT request FROM Request request " +
            "WHERE request.event.id = :eventId " +
            "AND request.event.initiator.id = :userId")
    List<Request> findByEventIdAndInitiatorId(@Param("eventId") Long eventId,
                                                           @Param("userId") Long userId);

    @Query("SELECT request FROM Request request " +
            "WHERE request.event.id = :eventId AND request.status = 'CONFIRMED'")
    List<Request> findByEventIdConfirmed(@Param("eventId") Long eventId);

    @Query("SELECT request FROM Request request " +
            "WHERE request.status = 'CONFIRMED' " +
            "AND request.event.id IN (:events)")
    List<Request> findConfirmedToListEvents(@Param("events") List<Long> events);

    @Query("SELECT request FROM Request request " +
            "WHERE request.event.id = :event " +
            "AND request.id IN (:requestIds)")
    List<Request> findByEventIdAndRequestsIds(@Param("event") Long eventId,
                                                           @Param("requestIds") List<Long> requestIds);

    List<Request> findAllByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByEventIdAndEventInitiatorId(Long eventId, Long initiatorId);

    @Query("SELECT r FROM Request r " +
            "WHERE r.event.id IN :eventId AND r.status = 'CONFIRMED'")
    List<Request> findAllConfirmedByEventIdIn(List<Long> eventId);
}
