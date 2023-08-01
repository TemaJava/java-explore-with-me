package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.user.dto.UserDto;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> findAllUsersInId(List<Long> ids, Pageable pageable) {
        return userRepository.findAllByIdIn(ids, pageable).stream()
                .map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public List<UserDto> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).stream()
                .map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto)));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> {
            throw new ObjectNotFoundException("User not found");
        });
        userRepository.deleteById(userId);
    }
}
