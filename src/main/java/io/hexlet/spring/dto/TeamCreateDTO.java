package io.hexlet.spring.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamCreateDTO {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String city;

    @Min(1850)
    @Max(2100)
    private Integer foundedYear;
}
