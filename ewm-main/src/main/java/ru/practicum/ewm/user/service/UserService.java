package ru.practicum.ewm.user.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> findAllUsersInfo(List<Long> ids, Pageable pageable);

    UserDto createUser(UserDto userDto);

    void deleteUser(Long userId);
}
