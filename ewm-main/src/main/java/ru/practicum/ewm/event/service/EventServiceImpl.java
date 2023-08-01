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
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.location.repository.LocationRepository;
import ru.practicum.ewm.rating.model.Rating;
import ru.practicum.ewm.rating.repository.RatingRepository;
import ru.practicum.ewm.user.pagination.Pagination;
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
    private final EventRepository eventCustomRepository;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestsRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final StatService statService;
    LocalDateTime start = null;
    LocalDateTime end = null;

    @Override
    public List<EventFullDto> adminFindEvents(List<Long> users, List<EventState> states,
                                              List<Long> categories, String rangeStart, String rangeEnd,
                                              Integer from, Integer size) {

        setRanges(rangeStart, rangeEnd);
        List<EventFullDto> fullEventDtoList = eventCustomRepository.findEventsByParams(users, states, categories,
                        start, end, Pagination.toPageable(from, size))
                .stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
        getConfirmedRequests(fullEventDtoList);
        getRatingForFullEvent(fullEventDtoList);

        return getViews(fullEventDtoList);
    }

    @Override
    @Transactional
    public EventFullDto adminUpdateEvent(Long eventId, UpdateEventRequest updateEventRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new ObjectNotFoundException("Event not found");
        });
        if (updateEventRequest.getEventDate() != null) {
            if (LocalDateTime.parse(updateEventRequest.getEventDate(),
                    dateTimeFormatter).isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Date in the past");
            } else {
                event.setEventDate(LocalDateTime.parse(updateEventRequest.getEventDate(),
                        dateTimeFormatter));
            }
        }
        if (event.getEventState() == EventState.PUBLISHED
                && updateEventRequest.getStateAction().equalsIgnoreCase(AdminEventState.PUBLISH_EVENT.name())) {
            throw new ValidationException("Event is already published");
        }
        if (event.getEventState() == EventState.CANCELED
                && updateEventRequest.getStateAction().equalsIgnoreCase(AdminEventState.PUBLISH_EVENT.name())) {
            throw new ValidationException("Event is canceled");
        }
        if (event.getEventState() == EventState.PUBLISHED
                && updateEventRequest.getStateAction().equalsIgnoreCase(AdminEventState.REJECT_EVENT.name())) {
            throw new ValidationException("Event is published. You can't reject it");
        }
        if (updateEventRequest.getStateAction() != null) {
            if (updateEventRequest.getStateAction().equals(AdminEventState.PUBLISH_EVENT.name())) {
                event.setEventState(EventState.PUBLISHED);
            } else if (updateEventRequest.getStateAction().equals(AdminEventState.REJECT_EVENT.name())
                    && event.getEventState() != EventState.PUBLISHED) {
                event.setEventState(EventState.CANCELED);
            }
        }
        if (updateEventRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventRequest.getCategory()).orElseThrow(() -> {
                throw new ObjectNotFoundException("Category not found for update");
            });
            event.setCategory(category);
        }
        if (updateEventRequest.getLocation() != null) {
            event.setLocation(locationRepository.save(updateEventRequest.getLocation()));
        }
        toEventFromUpdateRequest(event, updateEventRequest);
        eventRepository.save(event);
        EventFullDto fullEventDto = EventMapper.toEventFullDto(event);
        getConfirmedRequests(Collections.singletonList(fullEventDto));
        getRatingForFullEvent(Collections.singletonList(fullEventDto));
        return getViews(Collections.singletonList(fullEventDto)).get(0);
    }

    @Override
    public List<EventShortDto> privateGetCreatorsEvents(Long userId, Pageable pageable) {
        List<EventShortDto> shortEventDtos = eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(EventMapper::toShortEventDto)
                .collect(Collectors.toList());
        getConfirmedRequestsToShort(shortEventDtos);
        getRatingForShortEvent(shortEventDtos);
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
        fullEventDto.setRating(0L);

        return fullEventDto;
    }

    @Override
    public EventFullDto privateGetEventInfoByCreator(Long userId, Long eventId) {
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId).orElseThrow();

        EventFullDto fullEventDto = EventMapper.toEventFullDto(event);
        fullEventDto.setConfirmedRequests(requestsRepository
                .findAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED).size());
        getRatingForFullEvent(Collections.singletonList(fullEventDto));
        return getViews(Collections.singletonList(fullEventDto)).get(0);
    }


    @Override
    @Transactional
    public EventFullDto privateUpdateEventByCreator(Long userId, Long eventId,
                                                    UpdateEventRequest updateEventRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ObjectNotFoundException("You can't update this event");
        }
        if (updateEventRequest.getEventDate() != null) {
            LocalDateTime time = LocalDateTime.parse(updateEventRequest.getEventDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (LocalDateTime.now().isAfter(time.minusHours(2))) {
                throw new BadRequestException("Event starts in less then 2 hours");
            }
        }
        if (event.getEventState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("You can't update published event");
        }
        if (updateEventRequest.getCategory() != null && !Objects.equals(updateEventRequest.getCategory(),
                event.getCategory().getId())) {
            Category category = categoryRepository.findById(updateEventRequest.getCategory()).orElseThrow();
            event.setCategory(category);
        }
        if (updateEventRequest.getLocation() != null) {
            Location location = locationRepository.save(updateEventRequest.getLocation());
            event.setLocation(location);
        }
        toEventFromUpdateRequest(event, updateEventRequest);
        EventFullDto fullEventDto = EventMapper.toEventFullDto(event);
        fullEventDto.setConfirmedRequests(requestsRepository.findAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)
                .size());
        getRatingForFullEvent(Collections.singletonList(fullEventDto));
        return getViews(Collections.singletonList(fullEventDto)).get(0);
    }

    @Override
    public List<EventShortDto> publicFindEvents(String text, List<Long> categories, Boolean paid, String rangeStart,
                                                String rangeEnd, Boolean onlyAvailable, String sort,
                                                Pageable pageable, HttpServletRequest request) {
        setRanges(rangeStart, rangeEnd);
        if (text == null) text = "";
        List<Event> events = eventRepository.findByParamsOrderByDate(text.toLowerCase(), List.of(EventState.PUBLISHED),
                categories, paid, start, end, pageable);
        if (events.isEmpty()) {
            throw new BadRequestException("Event must be published");
        }
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
        getRatingForFullEvent(fullEventDtoList);
        return eventsShort;
    }

    @Override
    public EventFullDto publicFindEventById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id).orElseThrow(() -> {
            throw new ObjectNotFoundException("Event not found");
        });

        if (event.getEventState() != EventState.PUBLISHED) {
            throw new ObjectNotFoundException("event");
        }

        EventFullDto fullEventDto = EventMapper.toEventFullDto(event);
        fullEventDto.setConfirmedRequests(requestsRepository.findAllByEventIdAndStatus(event.getId(),
                RequestStatus.CONFIRMED).size());
        getRatingForFullEvent(Arrays.asList(fullEventDto));
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
                        true)
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

    public static void toEventFromUpdateRequest(Event event, UpdateEventRequest updateEventRequest) {
        if (Objects.equals(updateEventRequest.getStateAction(), UserEventState.CANCEL_REVIEW.name())) {
            event.setEventState(EventState.CANCELED);
        }
        if (Objects.equals(updateEventRequest.getStateAction(), UserEventState.SEND_TO_REVIEW.name())) {
            event.setEventState(EventState.PENDING);
        }
        if (updateEventRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventRequest.getAnnotation());
        }
        if (updateEventRequest.getDescription() != null) {
            event.setDescription(updateEventRequest.getDescription());
        }
        if (updateEventRequest.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(updateEventRequest.getEventDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (updateEventRequest.getPaid() != null) {
            event.setPaid(updateEventRequest.getPaid());
        }
        if (updateEventRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventRequest.getParticipantLimit());
        }
        if (updateEventRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventRequest.getRequestModeration());
        }
        if (updateEventRequest.getTitle() != null) {
            event.setTitle(updateEventRequest.getTitle());
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

    private void setRanges(String startRange, String endRange) {
        start = null;
        end = null;
        if (startRange != null) {
            start = LocalDateTime.parse(startRange, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (startRange != null) {
            end = LocalDateTime.parse(endRange, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    private void getRatingForFullEvent(List<EventFullDto> dtos) {
        List<Long> ids = dtos.stream().map(EventFullDto::getId).collect(Collectors.toList());
        //айди ивента - лист оценок
        HashMap<Long, List<Long>> ratingMap = new HashMap<>();
        List<Rating> rates = ratingRepository.findAllByEventIdIn(ids);

        if (!rates.isEmpty()) {
            rates.forEach(element -> {
                if (ratingMap.containsKey(element.getEvent().getId())) {
                    List<Long> updateList = new ArrayList<>(ratingMap.get(element.getEvent().getId()));
                    updateList.add(element.getRate());
                    ratingMap.put(element.getEvent().getId(), updateList);
                } else {
                    ratingMap.put(element.getEvent().getId(), Arrays.asList(element.getRate()));
                }
            });

            dtos.forEach(element -> {
                Long total = 0L;
                for (Long rate : ratingMap.get(element.getId())) {
                    total += rate;
                }
                total = total / ratingMap.get(element.getId()).size();
                element.setRating(total);
            });
        }
    }


    private void getRatingForShortEvent(List<EventShortDto> dtos) {
        List<Long> ids = dtos.stream().map(EventShortDto::getId).collect(Collectors.toList());
        //айди ивента - лист оценок
        HashMap<Long, List<Long>> ratingMap = new HashMap<>();
        List<Rating> rates = ratingRepository.findAllByEventIdIn(ids);

        if (!rates.isEmpty()) {
            rates.forEach(element -> {
                if (ratingMap.containsKey(element.getEvent().getId())) {
                    List<Long> updateList = new ArrayList<>(ratingMap.get(element.getEvent().getId()));
                    updateList.add(element.getRate());
                    ratingMap.put(element.getEvent().getId(), updateList);
                } else {
                    ratingMap.put(element.getEvent().getId(), Arrays.asList(element.getRate()));
                }
            });

            dtos.forEach(element -> {
                Long total = 0L;
                for (Long rate : ratingMap.get(element.getId())) {
                    total += rate;
                }
                total = total / ratingMap.get(element.getId()).size();
                element.setRating(total);
            });
        }
    }
}
