package ru.practicum;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewStats {
    String app;
    String uri;
    Long hits;
}
