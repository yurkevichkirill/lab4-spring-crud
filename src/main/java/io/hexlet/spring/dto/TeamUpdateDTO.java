package io.hexlet.spring.dto;

import org.openapitools.jackson.nullable.JsonNullable;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Частичное обновление: поля, отсутствующие в запросе, не изменяются.
 */
@Getter
@Setter
public class TeamUpdateDTO {

    @NotBlank
    @Size(max = 100)
    private JsonNullable<String> name = JsonNullable.undefined();

    @Size(max = 100)
    private JsonNullable<String> city = JsonNullable.undefined();

    @Min(1850)
    @Max(2100)
    private JsonNullable<Integer> foundedYear = JsonNullable.undefined();
}
