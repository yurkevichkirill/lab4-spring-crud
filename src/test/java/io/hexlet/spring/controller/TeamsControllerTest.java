package io.hexlet.spring.controller;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import io.hexlet.spring.dto.TeamCreateDTO;
import io.hexlet.spring.model.Player;
import io.hexlet.spring.model.Team;
import io.hexlet.spring.repository.PlayerRepository;
import io.hexlet.spring.repository.TeamRepository;
import net.datafaker.Faker;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TeamsControllerTest {

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

    @BeforeEach
    public void setUp() {
        playerRepository.deleteAll();
        teamRepository.deleteAll();

        testTeam = Instancio.of(Team.class)
                .ignore(Select.field(Team::getId))
                .ignore(Select.field(Team::getCreatedAt))
                .ignore(Select.field(Team::getUpdatedAt))
                .supply(Select.field(Team::getName),
                        () -> "FC " + faker.address().cityName() + " " + faker.number().digits(5))
                .supply(Select.field(Team::getCity), () -> faker.address().city())
                .supply(Select.field(Team::getFoundedYear), () -> faker.number().numberBetween(1900, 2024))
                .create();
    }

    @Test
    public void testIndex() throws Exception {
        teamRepository.save(testTeam);

        var body = mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThatJson(body).isArray().hasSize(1);
        assertThatJson(body).inPath("[0].name").isEqualTo(testTeam.getName());
    }

    @Test
    public void testCreate() throws Exception {
        var dto = new TeamCreateDTO();
        dto.setName(testTeam.getName());
        dto.setCity(testTeam.getCity());
        dto.setFoundedYear(testTeam.getFoundedYear());

        var body = mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("id").isPresent(),
                v -> v.node("name").isEqualTo(dto.getName()),
                v -> v.node("city").isEqualTo(dto.getCity()),
                v -> v.node("foundedYear").isEqualTo(dto.getFoundedYear()),
                v -> v.node("createdAt").isPresent()
        );
        var team = teamRepository.findByName(dto.getName()).orElseThrow();
        assertThat(team.getCity()).isEqualTo(dto.getCity());
    }

    @Test
    public void testCreateDuplicateNameConflict() throws Exception {
        teamRepository.save(testTeam);
        var dto = new TeamCreateDTO();
        dto.setName(testTeam.getName());

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testCreateInvalid() throws Exception {
        var body = mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("name", "", "foundedYear", 1500))))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("name").isPresent(),
                v -> v.node("foundedYear").isPresent()
        );
        assertThat(teamRepository.count()).isZero();
    }

    @Test
    public void testShow() throws Exception {
        teamRepository.save(testTeam);

        var body = mockMvc.perform(get("/api/teams/" + testTeam.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(testTeam.getId()),
                v -> v.node("name").isEqualTo(testTeam.getName()),
                v -> v.node("city").isEqualTo(testTeam.getCity())
        );
    }

    @Test
    public void testShowNotFound() throws Exception {
        mockMvc.perform(get("/api/teams/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdate() throws Exception {
        teamRepository.save(testTeam);
        var data = Map.of("city", "Брест");

        var body = mockMvc.perform(put("/api/teams/" + testTeam.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThatJson(body).node("city").isEqualTo("Брест");
        var team = teamRepository.findById(testTeam.getId()).orElseThrow();
        assertThat(team.getCity()).isEqualTo("Брест");
        assertThat(team.getName()).isEqualTo(testTeam.getName());
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        mockMvc.perform(put("/api/teams/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("city", "Минск"))))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDestroy() throws Exception {
        teamRepository.save(testTeam);

        mockMvc.perform(delete("/api/teams/" + testTeam.getId()))
                .andExpect(status().isNoContent());

        assertThat(teamRepository.existsById(testTeam.getId())).isFalse();
    }

    @Test
    public void testDestroyNotFound() throws Exception {
        mockMvc.perform(delete("/api/teams/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDestroyWithPlayersConflict() throws Exception {
        teamRepository.save(testTeam);
        playerRepository.save(newPlayer(testTeam));

        mockMvc.perform(delete("/api/teams/" + testTeam.getId()))
                .andExpect(status().isConflict());

        assertThat(teamRepository.existsById(testTeam.getId())).isTrue();
    }

    @Test
    public void testTeamPlayers() throws Exception {
        teamRepository.save(testTeam);
        var player = playerRepository.save(newPlayer(testTeam));

        var body = mockMvc.perform(get("/api/teams/" + testTeam.getId() + "/players"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThatJson(body).isArray().hasSize(1);
        assertThatJson(body).inPath("[0].lastName").isEqualTo(player.getLastName());
        assertThatJson(body).inPath("[0].teamId").isEqualTo(testTeam.getId());
    }

    private Player newPlayer(Team team) {
        var player = new Player();
        player.setFirstName(faker.name().firstName());
        player.setLastName(faker.name().lastName());
        player.setPosition("Нападающий");
        player.setShirtNumber(9);
        player.setTeam(team);
        return player;
    }
}
