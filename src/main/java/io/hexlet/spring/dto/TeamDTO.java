package io.hexlet.spring.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamDTO {

    private Long id;
    private String name;
    private String city;
    private Integer foundedYear;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
