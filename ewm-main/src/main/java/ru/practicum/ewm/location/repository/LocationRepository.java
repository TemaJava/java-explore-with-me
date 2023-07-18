package ru.practicum.ewm.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.location.model.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
}