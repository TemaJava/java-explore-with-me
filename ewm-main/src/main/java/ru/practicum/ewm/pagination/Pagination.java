package ru.practicum.ewm.pagination;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


@UtilityClass
public class Pagination {
    public Pageable toPageable(Integer from, Integer size) {
        if (from == null || size == null) {
            return null;
        }

        if (size <= 0 || from < 0) {
          //  throw new ValidationException("Ошибка пагинации");
        }

        int page = from / size;
        return PageRequest.of(page, size);
    }
}
