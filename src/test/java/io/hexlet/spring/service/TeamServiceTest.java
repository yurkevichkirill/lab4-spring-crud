package io.hexlet.spring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;

import io.hexlet.spring.dto.PlayerDTO;
import io.hexlet.spring.dto.TeamCreateDTO;
import io.hexlet.spring.dto.TeamDTO;
import io.hexlet.spring.dto.TeamUpdateDTO;
import io.hexlet.spring.exception.ResourceConflictException;
import io.hexlet.spring.exception.ResourceNotFoundException;
import io.hexlet.spring.mapper.PlayerMapper;
import io.hexlet.spring.mapper.TeamMapper;
import io.hexlet.spring.model.Player;
import io.hexlet.spring.model.Team;
import io.hexlet.spring.repository.PlayerRepository;
import io.hexlet.spring.repository.TeamRepository;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private PlayerMapper playerMapper;

    @InjectMocks
    private TeamService teamService;

    private Team team;
    private TeamDTO teamDTO;

    @BeforeEach
    public void setUp() {
        team = new Team();
        team.setId(1L);
        team.setName("ФК Немига");
        team.setCity("Минск");
        team.setFoundedYear(2015);

        teamDTO = new TeamDTO();
        teamDTO.setId(1L);
        teamDTO.setName("ФК Немига");
        teamDTO.setCity("Минск");
        teamDTO.setFoundedYear(2015);
    }

    @Test
    public void testGetAll() {
        when(teamRepository.findAll()).thenReturn(List.of(team));
        when(teamMapper.map(team)).thenReturn(teamDTO);

        assertThat(teamService.getAll()).containsExactly(teamDTO);
    }

    @Test
    public void testCreate() {
        var createDTO = new TeamCreateDTO();
        createDTO.setName("ФК Немига");
        when(teamRepository.findByName("ФК Немига")).thenReturn(Optional.empty());
        when(teamMapper.map(createDTO)).thenReturn(team);
        when(teamMapper.map(team)).thenReturn(teamDTO);

        var result = teamService.create(createDTO);

        verify(teamRepository).save(team);
        assertThat(result).isEqualTo(teamDTO);
    }

    @Test
    public void testCreateDuplicate() {
        var createDTO = new TeamCreateDTO();
        createDTO.setName("ФК Немига");
        when(teamRepository.findByName("ФК Немига")).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.create(createDTO))
                .isInstanceOf(ResourceConflictException.class);
        verify(teamRepository, never()).save(any());
        verify(teamRepository, never()).saveAndFlush(any());
    }

    @Test
    public void testFindById() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(teamMapper.map(team)).thenReturn(teamDTO);

        assertThat(teamService.findById(1L).getName()).isEqualTo("ФК Немига");
    }

    @Test
    public void testFindByIdNotFound() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Team Not Found: 99");
    }

    @Test
    public void testUpdate() {
        var updateDTO = new TeamUpdateDTO();
        updateDTO.setCity(JsonNullable.of("Брест"));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(teamMapper.map(team)).thenReturn(teamDTO);

        var result = teamService.update(updateDTO, 1L);

        verify(teamMapper).update(updateDTO, team);
        verify(teamRepository).saveAndFlush(team);
        assertThat(result).isEqualTo(teamDTO);
    }

    @Test
    public void testUpdateNotFound() {
        var updateDTO = new TeamUpdateDTO();
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.update(updateDTO, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(teamRepository, never()).save(any());
        verify(teamRepository, never()).saveAndFlush(any());
    }

    @Test
    public void testDelete() {
        when(teamRepository.existsById(1L)).thenReturn(true);
        when(playerRepository.existsByTeamId(1L)).thenReturn(false);

        teamService.delete(1L);

        verify(teamRepository).deleteById(1L);
    }

    @Test
    public void testDeleteNotFound() {
        when(teamRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> teamService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(teamRepository, never()).deleteById(any());
    }

    @Test
    public void testDeleteWithPlayers() {
        when(teamRepository.existsById(1L)).thenReturn(true);
        when(playerRepository.existsByTeamId(1L)).thenReturn(true);

        assertThatThrownBy(() -> teamService.delete(1L))
                .isInstanceOf(ResourceConflictException.class);
        verify(teamRepository, never()).deleteById(any());
    }

    @Test
    public void testGetPlayers() {
        var player = new Player();
        var playerDTO = new PlayerDTO();
        when(teamRepository.existsById(1L)).thenReturn(true);
        when(playerRepository.findByTeamId(1L)).thenReturn(List.of(player));
        when(playerMapper.map(player)).thenReturn(playerDTO);

        assertThat(teamService.getPlayers(1L)).containsExactly(playerDTO);
    }
}
