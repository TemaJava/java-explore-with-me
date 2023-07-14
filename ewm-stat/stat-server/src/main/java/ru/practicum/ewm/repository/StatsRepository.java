package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.EndpointHit;
import ru.practicum.ewm.model.ViewStats;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {
    @Query("select new ru.practicum.ewm.model.ViewStats(h.app,h.uri,  COUNT (h.ip)) "
            + "FROM EndpointHit as h "
            + "WHERE h.timestamp >= :start AND h.timestamp <= :end "
            + "GROUP BY h.app, h.uri "
            + "ORDER BY COUNT (h.ip) DESC")
    List<ViewStats> getAllStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select new ru.practicum.ewm.model.ViewStats(h.app,h.uri,  COUNT (distinct h.ip)) "
            + "FROM EndpointHit as h "
            + "WHERE h.timestamp >= :start AND h.timestamp <= :end "
            + "GROUP BY h.app, h.uri "
            + "ORDER BY COUNT (DISTINCT h.ip) DESC")
    List<ViewStats> getAllUniqueStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select new ru.practicum.ewm.model.ViewStats(h.app,h.uri, COUNT (h.ip)) "
            + "FROM EndpointHit as h "
            + "WHERE h.timestamp >= :start AND h.timestamp <= :end AND h.uri IN :uris "
            + "GROUP BY h.app, h.uri "
            + "ORDER BY COUNT (h.ip) DESC")
    List<ViewStats> getStatsByUrisAndTimestamps(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                               @Param("uris") List<String> uris);

    @Query("select new ru.practicum.ewm.model.ViewStats(h.app,h.uri,  COUNT (distinct h.ip)) "
            + "FROM EndpointHit as h "
            + "WHERE h.timestamp >= :start AND h.timestamp <= :end AND h.uri IN :uris "
            + "GROUP BY h.app, h.uri "
            + "ORDER BY COUNT (DISTINCT h.ip) DESC")
    List<ViewStats> getUniqueStatsByUrisAndTimestamps(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                                     @Param("uris") List<String> uris);
}
