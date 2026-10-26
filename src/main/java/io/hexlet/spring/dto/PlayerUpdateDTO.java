package io.hexlet.spring.dto;

import java.time.LocalDate;

import org.openapitools.jackson.nullable.JsonNullable;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

/**
 * Частичное обновление: поля, отсутствующие в запросе, не изменяются.
 */
@Getter
@Setter
public class PlayerUpdateDTO {

    @NotBlank
    private JsonNullable<String> firstName = JsonNullable.undefined();

    @NotBlank
    private JsonNullable<String> lastName = JsonNullable.undefined();

    @NotBlank
    private JsonNullable<String> position = JsonNullable.undefined();

    @Min(1)
    @Max(99)
    private JsonNullable<Integer> shirtNumber = JsonNullable.undefined();

    @Past
    private JsonNullable<LocalDate> birthDate = JsonNullable.undefined();

    @NotNull
    private JsonNullable<Long> teamId = JsonNullable.undefined();
}
