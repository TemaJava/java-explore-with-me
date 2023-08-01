package ru.practicum.ewm.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationDto {
    @Size(min = 1, max = 120)
    private String title;
    private Boolean pinned;
    private List<Long> events;
}
