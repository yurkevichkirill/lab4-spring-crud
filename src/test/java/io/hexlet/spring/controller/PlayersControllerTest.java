package io.hexlet.spring.controller;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hexlet.spring.dto.PlayerCreateDTO;
import io.hexlet.spring.model.Player;
import io.hexlet.spring.model.Team;
import io.hexlet.spring.repository.PlayerRepository;
import io.hexlet.spring.repository.TeamRepository;
import net.datafaker.Faker;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PlayersControllerTest {

    private static final List<String> POSITIONS =
            List.of("Вратарь", "Защитник", "Полузащитник", "Нападающий");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private final Faker faker = new Faker();

    private Team testTeam;
    private Team otherTeam;
    private Player testPlayer;

    @BeforeEach
    public void setUp() {
        playerRepository.deleteAll();
        teamRepository.deleteAll();

        testTeam = teamRepository.save(newTeam());
        otherTeam = teamRepository.save(newTeam());

        testPlayer = Instancio.of(Player.class)
                .ignore(Select.field(Player::getId))
                .ignore(Select.field(Player::getCreatedAt))
                .ignore(Select.field(Player::getUpdatedAt))
                .supply(Select.field(Player::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(Player::getLastName), () -> faker.name().lastName())
                .supply(Select.field(Player::getPosition),
                        () -> POSITIONS.get(faker.number().numberBetween(0, POSITIONS.size())))
                .supply(Select.field(Player::getShirtNumber), () -> faker.number().numberBetween(1, 100))
                .supply(Select.field(Player::getBirthDate),
                        () -> LocalDate.of(faker.number().numberBetween(1980, 2006), 5, 17))
                .supply(Select.field(Player::getTeam), () -> testTeam)
                .create();
    }

    private Team newTeam() {
        var team = new Team();
        team.setName("FC " + faker.address().cityName() + " " + faker.number().digits(6));
        team.setCity(faker.address().city());
        team.setFoundedYear(faker.number().numberBetween(1950, 2024));
        return team;
    }

    @Test
    public void testIndex() throws Exception {
        playerRepository.save(testPlayer);

        var body = mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThatJson(body).isArray().hasSize(1);
        assertThatJson(body).inPath("[0].teamName").isEqualTo(testTeam.getName());
    }

    @Test
    public void testCreate() throws Exception {
        var dto = new PlayerCreateDTO();
        dto.setFirstName(testPlayer.getFirstName());
        dto.setLastName(testPlayer.getLastName());
        dto.setPosition(testPlayer.getPosition());
        dto.setShirtNumber(testPlayer.getShirtNumber());
        dto.setBirthDate(testPlayer.getBirthDate());
        dto.setTeamId(testTeam.getId());

        var body = mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("id").isPresent(),
                v -> v.node("lastName").isEqualTo(dto.getLastName()),
                v -> v.node("position").isEqualTo(dto.getPosition()),
                v -> v.node("teamId").isEqualTo(testTeam.getId()),
                v -> v.node("birthDate").isEqualTo(dto.getBirthDate().toString())
        );
        assertThat(playerRepository.findByTeamId(testTeam.getId())).hasSize(1);
    }

    @Test
    public void testCreateWithUnknownTeam() throws Exception {
        var dto = new PlayerCreateDTO();
        dto.setFirstName("Иван");
        dto.setLastName("Петров");
        dto.setPosition("Вратарь");
        dto.setTeamId(999999L);

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
        assertThat(playerRepository.count()).isZero();
    }

    @Test
    public void testCreateInvalid() throws Exception {
        var data = Map.of("firstName", "", "shirtNumber", 150);

        var body = mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("firstName").isPresent(),
                v -> v.node("shirtNumber").isPresent(),
                v -> v.node("teamId").isPresent()
        );
    }

    @Test
    public void testShow() throws Exception {
        playerRepository.save(testPlayer);

        var body = mockMvc.perform(get("/api/players/" + testPlayer.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(testPlayer.getId()),
                v -> v.node("firstName").isEqualTo(testPlayer.getFirstName()),
                v -> v.node("lastName").isEqualTo(testPlayer.getLastName()),
                v -> v.node("shirtNumber").isEqualTo(testPlayer.getShirtNumber()),
                v -> v.node("teamId").isEqualTo(testTeam.getId())
        );
    }

    @Test
    public void testShowNotFound() throws Exception {
        var body = mockMvc.perform(get("/api/players/999999"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).isEqualTo("Player Not Found: 999999");
    }

    @Test
    public void testUpdate() throws Exception {
        playerRepository.save(testPlayer);
        var data = Map.of("shirtNumber", 10, "teamId", otherTeam.getId());

        var body = mockMvc.perform(put("/api/players/" + testPlayer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("shirtNumber").isEqualTo(10),
                v -> v.node("teamId").isEqualTo(otherTeam.getId())
        );
        var player = playerRepository.findById(testPlayer.getId()).orElseThrow();
        assertThat(player.getShirtNumber()).isEqualTo(10);
        assertThat(player.getTeam().getId()).isEqualTo(otherTeam.getId());
        assertThat(player.getLastName()).isEqualTo(testPlayer.getLastName());
    }

    @Test
    public void testUpdateWithUnknownTeam() throws Exception {
        playerRepository.save(testPlayer);

        mockMvc.perform(put("/api/players/" + testPlayer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("teamId", 999999))))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDestroy() throws Exception {
        playerRepository.save(testPlayer);

        mockMvc.perform(delete("/api/players/" + testPlayer.getId()))
                .andExpect(status().isNoContent());

        assertThat(playerRepository.existsById(testPlayer.getId())).isFalse();
    }

    @Test
    public void testDestroyNotFound() throws Exception {
        mockMvc.perform(delete("/api/players/999999"))
                .andExpect(status().isNotFound());
    }
}
