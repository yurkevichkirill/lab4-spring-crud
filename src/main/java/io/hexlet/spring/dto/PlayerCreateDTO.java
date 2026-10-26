package io.hexlet.spring.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerCreateDTO {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String position;

    @Min(1)
    @Max(99)
    private Integer shirtNumber;

    @Past
    private LocalDate birthDate;

    @NotNull
    private Long teamId;
}
