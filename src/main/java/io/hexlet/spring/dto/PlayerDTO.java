package io.hexlet.spring.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String position;
    private Integer shirtNumber;
    private LocalDate birthDate;
    private Long teamId;
    private String teamName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
