package ru.practicum.ewm.compilation.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.event.model.Event;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "compilations")
public class Compilation {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToMany
    @JoinTable(name = "compilation_event",
                joinColumns = @JoinColumn(name = "compilation_id"),
                inverseJoinColumns = @JoinColumn(name = "event_id"))
    private List<Event> events;
    private Boolean pinned;
    private String title;
}
