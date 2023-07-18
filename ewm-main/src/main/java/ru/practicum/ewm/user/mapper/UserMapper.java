package ru.practicum.ewm.user.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.model.User;

@UtilityClass
public class UserMapper {
    public UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getName());
    }

    public User toUser(UserDto userDto) {
        return new User(userDto.getId(), userDto.getEmail(), userDto.getName());
    }

    public UserShortDto toUserShortDto(User user) {
        return new UserShortDto(user.getId(), user.getName());
    }
}
