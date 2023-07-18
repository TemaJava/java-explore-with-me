package ru.practicum.ewm.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class EventMapper {
    public Event toEventFromNewDto(User initiator, Category category, NewEventDto createEventDto) {
        return new Event(0L, createEventDto.getAnnotation(), category, createEventDto.getDescription(),
                createEventDto.getEventDate(), createEventDto.getLocation(), createEventDto.getPaid(),
                createEventDto.getParticipantLimit(), createEventDto.getRequestModeration(), createEventDto.getTitle(),
                EventState.PENDING, LocalDateTime.now(), initiator, LocalDateTime.now());
    }

    public EventFullDto toEventFullDto(Event event) {
        return new EventFullDto(event.getId(), event.getAnnotation(), CategoryMapper.toCategoryDto(event.getCategory()),
                0, event.getCreatedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                event.getDescription(), event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                UserMapper.toUserShortDto(event.getInitiator()), event.getLocation(),
                event.getPaid(), event.getParticipantLimit(),
                event.getPublishedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                event.getRequestModeration(),
                event.getEventState().name(), event.getTitle(), 0L);
    };

    public EventShortDto toShortEventDto(Event event) {
        return new EventShortDto(event.getId(), event.getAnnotation(),
                CategoryMapper.toCategoryDto(event.getCategory()), 0,
                event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                UserMapper.toUserShortDto(event.getInitiator()), event.getPaid(), event.getTitle(), 0L);
    };

    public EventShortDto toShortFromFull(EventFullDto event) {
        return new EventShortDto(event.getId(), event.getAnnotation(),
                event.getCategory(), 0, event.getEventDate(),
                event.getInitiator(), event.getPaid(), event.getTitle(), 0L);
    };
}
