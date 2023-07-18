package ru.practicum.ewm.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.AdminEventState;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.UserEventState;
import ru.practicum.ewm.event.repository.EventCustomRepository;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.location.repository.LocationRepository;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.stat.HitMapper;
import ru.practicum.ewm.stat.StatService;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventCustomRepository eventCustomRepository;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestsRepository;
    private final UserRepository userRepository;
    private final StatService statService;

    @Override
    public List<EventFullDto> adminFindEvents(List<Long> users, List<EventState> states,
                                              List<Long> categories, String rangeStart, String rangeEnd,
                                              Integer from, Integer size) {
        List<EventFullDto> fullEventDtoList = eventCustomRepository.findEvents(users, states, categories,
                        rangeStart, rangeEnd, from, size)
                .stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
        getConfirmedRequests(fullEventDtoList);

        return getViews(fullEventDtoList);
    }

    @Override
    @Transactional
    public EventFullDto adminUpdateEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new ObjectNotFoundException("Event not found");
        });
        if (updateEventAdminRequest.getEventDate() != null) {
            if (LocalDateTime.parse(updateEventAdminRequest.getEventDate(),
                    dateTimeFormatter).isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Date in the past");
            } else {
                event.setEventDate(LocalDateTime.parse(updateEventAdminRequest.getEventDate(),
                        dateTimeFormatter));
            }
        }
        if (event.getEventState() == EventState.PUBLISHED
                && updateEventAdminRequest.getStateAction().equalsIgnoreCase(AdminEventState.PUBLISH_EVENT.name())) {
            throw new ValidationException("Event is already published");
        }
        if (event.getEventState() == EventState.CANCELED
                && updateEventAdminRequest.getStateAction().equalsIgnoreCase(AdminEventState.PUBLISH_EVENT.name())) {
            throw new ValidationException("Event is canceled");
        }
        if (event.getEventState() == EventState.PUBLISHED
                && updateEventAdminRequest.getStateAction().equalsIgnoreCase(AdminEventState.REJECT_EVENT.name())) {
            throw new ValidationException("Event is published. You can't reject it");
        }
        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction().equals(AdminEventState.PUBLISH_EVENT.name())) {
                event.setEventState(EventState.PUBLISHED);
            } else if (updateEventAdminRequest.getStateAction().equals(AdminEventState.REJECT_EVENT.name())
                    && event.getEventState() != EventState.PUBLISHED) {
                event.setEventState(EventState.CANCELED);
            }
        }
        if (updateEventAdminRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventAdminRequest.getCategory()).orElseThrow(() -> {
                throw new ObjectNotFoundException("Category not found for update");
            });
            event.setCategory(category);
        }
        if (updateEventAdminRequest.getLocation() != null) {
            event.setLocation(locationRepository.save(updateEventAdminRequest.getLocation()));
        }
        toEventFromUpdateEventAdminRequest(event, updateEventAdminRequest);
        eventRepository.save(event);
        EventFullDto fullEventDto = EventMapper.toEventFullDto(event);
        getConfirmedRequests(Collections.singletonList(fullEventDto));
        return getViews(Collections.singletonList(fullEventDto)).get(0);
    }

    @Override
    public List<EventShortDto> privateGetCreatorsEvents(Long userId, Pageable pageable) {
        List<EventShortDto> shortEventDtos = eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(EventMapper::toShortEventDto)
                .collect(Collectors.toList());
        getConfirmedRequestsToShort(shortEventDtos);
        return getViewsToShort(shortEventDtos, statService);
    }

    @Override
    @Transactional
    public EventFullDto privateCreateEvent(Long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Wrong date");
        }
        User initiator = userRepository.findById(userId).orElseThrow(() -> {
            throw new ObjectNotFoundException("User not found");
        });
        Category category = categoryRepository.findById(newEventDto.getCategory()).orElseThrow(() -> {
            throw new ObjectNotFoundException("Category not found");
        });
        newEventDto.setLocation(locationRepository.save(newEventDto.getLocation()));
        if (newEventDto.getPaid() == null) newEventDto.setPaid(false);
        if (newEventDto.getParticipantLimit() == null) newEventDto.setParticipantLimit(0);
        if (newEventDto.getRequestModeration() == null) newEventDto.setRequestModeration(true);
        Event event = eventRepository.save(EventMapper.toEventFromNewDto(initiator, category, newEventDto));
        EventFullDto fullEventDto = EventMapper.toEventFullDto(event);
        fullEventDto.setConfirmedRequests(0);

        return fullEventDto;
    }

    @Override
    public EventFullDto privateGetEventInfoByCreator(Long userId, Long eventId) {
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId).orElseThrow();

        EventFullDto fullEventDto = EventMapper.toEventFullDto(event);
        fullEventDto.setConfirmedRequests(requestsRepository
                .findAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED).size());
        return getViews(Collections.singletonList(fullEventDto)).get(0);
    }


    @Override
    @Transactional
    public EventFullDto privateUpdateEventByCreator(Long userId, Long eventId,
                                                    UpdateEventUserRequest updateEventUserRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ObjectNotFoundException("You can't update this event");
        }
        if (updateEventUserRequest.getEventDate() != null) {
            LocalDateTime time = LocalDateTime.parse(updateEventUserRequest.getEventDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (LocalDateTime.now().isAfter(time.minusHours(2))) {
                throw new BadRequestException("Event starts in less then 2 hours");
            }
        }
        if (event.getEventState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("You can't update published event");
        }
        if (updateEventUserRequest.getCategory() != null && !Objects.equals(updateEventUserRequest.getCategory(),
                event.getCategory().getId())) {
            Category category = categoryRepository.findById(updateEventUserRequest.getCategory()).orElseThrow();
            event.setCategory(category);
        }
        if (updateEventUserRequest.getLocation() != null) {
            Location location = locationRepository.save(updateEventUserRequest.getLocation());
            event.setLocation(location);
        }
        toEventFromUpdateEventPrivateRequest(event, updateEventUserRequest);
        EventFullDto fullEventDto = EventMapper.toEventFullDto(event);
        fullEventDto.setConfirmedRequests(requestsRepository.findAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)
                .size());
        return getViews(Collections.singletonList(fullEventDto)).get(0);
    }

    @Override
    public List<EventShortDto> publicFindEvents(String text, List<Long> categories, Boolean paid, String rangeStart,
                                                String rangeEnd, Boolean onlyAvailable, String sort,
                                                Pageable pageable, HttpServletRequest request) {
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (rangeStart != null) {
            start = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (rangeStart != null) {
            end = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (text == null) text = "";
        List<Event> events = eventRepository.findByParamsOrderByDate(text.toLowerCase(), List.of(EventState.PUBLISHED),
                categories, paid, start, end, pageable);
        List<EventFullDto> fullEventDtoList = events.stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
        fullEventDtoList.forEach(event -> event.setConfirmedRequests(requestsRepository
                .findByEventIdConfirmed(event.getId()).size()));
        if (onlyAvailable) {
            fullEventDtoList = fullEventDtoList.stream()
                    .filter(event -> event.getParticipantLimit() <= event.getConfirmedRequests())
                    .collect(Collectors.toList());
        }

        statService.createView(HitMapper.toEndpointHit("ewm-main-service", request));
        List<EventShortDto> eventsShort = getViews(fullEventDtoList).stream()
                .map(EventMapper::toShortFromFull)
                .collect(Collectors.toList());
        if (sort != null && sort.equalsIgnoreCase("VIEWS")) {
            eventsShort.sort((e1, e2) -> e2.getViews().compareTo(e1.getViews()));
        }
        getConfirmedRequests(fullEventDtoList);
        return eventsShort;
    }

    @Override
    public EventFullDto findEventById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id).orElseThrow(() -> {
            throw new ObjectNotFoundException("Event not found");
        });

        if (event.getEventState() != EventState.PUBLISHED) {
            throw new ObjectNotFoundException("event");
        }

        EventFullDto fullEventDto = EventMapper.toEventFullDto(event);
        fullEventDto.setConfirmedRequests(requestsRepository.findAllByEventIdAndStatus(event.getId(),
                RequestStatus.CONFIRMED).size());
        statService.createView(HitMapper.toEndpointHit("ewm-main-service", request));
        return getViews(Collections.singletonList(fullEventDto)).get(0);
    }

    public List<RequestDto> getEventRequests(long userId, long eventId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            throw new ObjectNotFoundException("User not found");
        });
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new ObjectNotFoundException("Event not found");
        });

        return requestsRepository
                .findAllByEventIdAndEventInitiatorId(eventId, userId)
                .stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    private void getConfirmedRequests(List<EventFullDto> eventList) {
        Map<Long, Long> requestsCountMap = new HashMap<>();

        List<Request> requests = requestsRepository.findAllConfirmedByEventIdIn(eventList
                .stream()
                .map(EventFullDto::getId)
                .collect(Collectors.toList())
        );

        requests.forEach(request -> {
            long eventId = request.getEvent().getId();

            if (!requestsCountMap.containsKey(eventId)) {
                requestsCountMap.put(eventId, 0L);
            }

            requestsCountMap.put(eventId, requestsCountMap.get(eventId) + 1);
        });

        eventList.forEach(event -> {
            if (requestsCountMap.containsKey(event.getId())) {
                event.setConfirmedRequests(Math.toIntExact(requestsCountMap.get(event.getId())));
            }
        });
    }

    public List<EventFullDto> getViews(List<EventFullDto> eventList) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime max =  LocalDateTime.parse("3000-01-01 00:00:00", format);
        LocalDateTime min =  LocalDateTime.parse("1999-01-01 00:00:00", format);

        Map<String, EventFullDto> views = eventList.stream()
                .collect(Collectors.toMap(fullEventDto -> "/events/" + fullEventDto.getId(),
                        fullEventDto -> fullEventDto));
        Object responseBody = statService.getViewStats(min.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        max.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        new ArrayList<>(views.keySet()),
                        false)
                .getBody();
        List<ViewStatsDto> viewStatsList = new ObjectMapper().convertValue(responseBody, new TypeReference<>() {
        });
        viewStatsList.forEach(viewStatsDto -> {
            if (views.containsKey(viewStatsDto.getUri())) {
                views.get(viewStatsDto.getUri()).setViews(viewStatsDto.getHits());
            }
        });
        return new ArrayList<>(views.values());
    }

    public static void toEventFromUpdateEventPrivateRequest(Event event,
                                                   UpdateEventUserRequest updateEventUserRequest) {
        if (Objects.equals(updateEventUserRequest.getStateAction(), UserEventState.CANCEL_REVIEW.name())) {
            event.setEventState(EventState.CANCELED);
        }
        if (Objects.equals(updateEventUserRequest.getStateAction(), UserEventState.SEND_TO_REVIEW.name())) {
            event.setEventState(EventState.PENDING);
        }
        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(updateEventUserRequest.getEventDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }
    }

    public static void toEventFromUpdateEventAdminRequest(Event event,
                                                          UpdateEventAdminRequest updateEventAdminRequest) {
        if (Objects.equals(updateEventAdminRequest.getStateAction(), UserEventState.CANCEL_REVIEW.name())) {
            event.setEventState(EventState.CANCELED);
        }
        if (Objects.equals(updateEventAdminRequest.getStateAction(), UserEventState.SEND_TO_REVIEW.name())) {
            event.setEventState(EventState.PENDING);
        }
        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(updateEventAdminRequest.getEventDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }
        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }
        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }
    }

    public List<EventShortDto> getViewsToShort(List<EventShortDto> eventList, StatService statService) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime max =  LocalDateTime.parse("3000-01-01 00:00:00", format);
        LocalDateTime min =  LocalDateTime.parse("1999-01-01 00:00:00", format);

        Map<String, EventShortDto> views = eventList.stream()
                .collect(Collectors.toMap(fullEventDto -> "/events/" + fullEventDto.getId(),
                        fullEventDto -> fullEventDto));
        Object responseBody = statService.getViewStats(min.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        max.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        new ArrayList<>(views.keySet()),
                        false)
                .getBody();
        List<ViewStatsDto> viewStatsDtos = new ObjectMapper().convertValue(responseBody, new TypeReference<>() {
        });
        viewStatsDtos.forEach(viewStatsDto -> {
            if (views.containsKey(viewStatsDto.getUri())) {
                views.get(viewStatsDto.getUri()).setViews(viewStatsDto.getHits());
            }
        });
        return new ArrayList<>(views.values());
    }

    public void getConfirmedRequestsToShort(List<EventShortDto> eventList) {
        List<Long> ids = eventList.stream()
                .map(EventShortDto::getId)
                .collect(Collectors.toList());
        List<Request> requests = requestsRepository.findConfirmedToListEvents(ids);
        Map<Long, Integer> counter = new HashMap<>();
        requests.forEach(element -> counter.put(element.getEvent().getId(),
                counter.getOrDefault(element.getEvent().getId(), 0) + 1));
        eventList.forEach(event -> event.setConfirmedRequests(counter.get(event.getId())));
    }
}
