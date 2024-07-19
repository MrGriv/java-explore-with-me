package ru.practicum.model.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    private String name;
    private String email;
    @ElementCollection
    @CollectionTable(name = "friends", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "friend_id")
    private List<Long> friends;
    @Enumerated(EnumType.STRING)
    @Column(name = "show_event_state")
    private ShowEventsState showEventsState;
    @ElementCollection
    @CollectionTable(name = "users_events", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "event_id")
    private List<Long> userEvents;
}
