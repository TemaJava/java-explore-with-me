package ru.practicum.ewm.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.model.Request;

@UtilityClass
public class RequestMapper {
    public RequestDto toRequestDto(Request request) {
        return new RequestDto(request.getId(), request.getCreated(),
                request.getEvent().getId(), request.getRequester().getId(), request.getStatus());
    }
}
