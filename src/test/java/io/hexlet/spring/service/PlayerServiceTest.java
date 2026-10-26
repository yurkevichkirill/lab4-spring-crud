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

import io.hexlet.spring.dto.PlayerCreateDTO;
import io.hexlet.spring.dto.PlayerDTO;
import io.hexlet.spring.dto.PlayerUpdateDTO;
import io.hexlet.spring.exception.ResourceNotFoundException;
import io.hexlet.spring.mapper.PlayerMapper;
import io.hexlet.spring.model.Player;
import io.hexlet.spring.repository.PlayerRepository;
import io.hexlet.spring.repository.TeamRepository;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PlayerMapper playerMapper;

    @InjectMocks
    private PlayerService playerService;

    private Player player;
    private PlayerDTO playerDTO;

    @BeforeEach
    public void setUp() {
        player = new Player();
        player.setId(1L);
        player.setFirstName("Иван");
        player.setLastName("Петров");
        player.setPosition("Нападающий");
        player.setShirtNumber(9);

        playerDTO = new PlayerDTO();
        playerDTO.setId(1L);
        playerDTO.setFirstName("Иван");
        playerDTO.setLastName("Петров");
        playerDTO.setPosition("Нападающий");
        playerDTO.setShirtNumber(9);
        playerDTO.setTeamId(1L);
    }

    @Test
    public void testGetAll() {
        when(playerRepository.findAll()).thenReturn(List.of(player));
        when(playerMapper.map(player)).thenReturn(playerDTO);

        assertThat(playerService.getAll()).containsExactly(playerDTO);
    }

    @Test
    public void testCreate() {
        var createDTO = new PlayerCreateDTO();
        createDTO.setFirstName("Иван");
        createDTO.setLastName("Петров");
        createDTO.setPosition("Нападающий");
        createDTO.setTeamId(1L);
        when(teamRepository.existsById(1L)).thenReturn(true);
        when(playerMapper.map(createDTO)).thenReturn(player);
        when(playerMapper.map(player)).thenReturn(playerDTO);

        var result = playerService.create(createDTO);

        verify(playerRepository).save(player);
        assertThat(result).isEqualTo(playerDTO);
    }

    @Test
    public void testCreateTeamNotFound() {
        var createDTO = new PlayerCreateDTO();
        createDTO.setTeamId(42L);
        when(teamRepository.existsById(42L)).thenReturn(false);

        assertThatThrownBy(() -> playerService.create(createDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Team Not Found: 42");
        verify(playerRepository, never()).save(any());
        verify(playerRepository, never()).saveAndFlush(any());
    }

    @Test
    public void testFindById() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(playerMapper.map(player)).thenReturn(playerDTO);

        assertThat(playerService.findById(1L).getLastName()).isEqualTo("Петров");
    }

    @Test
    public void testFindByIdNotFound() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Player Not Found: 99");
    }

    @Test
    public void testUpdate() {
        var updateDTO = new PlayerUpdateDTO();
        updateDTO.setShirtNumber(JsonNullable.of(10));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(playerMapper.map(player)).thenReturn(playerDTO);

        var result = playerService.update(updateDTO, 1L);

        verify(playerMapper).update(updateDTO, player);
        verify(playerRepository).saveAndFlush(player);
        assertThat(result).isEqualTo(playerDTO);
    }

    @Test
    public void testUpdateNotFound() {
        var updateDTO = new PlayerUpdateDTO();
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.update(updateDTO, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(playerRepository, never()).save(any());
        verify(playerRepository, never()).saveAndFlush(any());
    }

    @Test
    public void testUpdateTeamNotFound() {
        var updateDTO = new PlayerUpdateDTO();
        updateDTO.setTeamId(JsonNullable.of(42L));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(teamRepository.existsById(42L)).thenReturn(false);

        assertThatThrownBy(() -> playerService.update(updateDTO, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(playerRepository, never()).save(any());
        verify(playerRepository, never()).saveAndFlush(any());
    }

    @Test
    public void testDelete() {
        when(playerRepository.existsById(1L)).thenReturn(true);

        playerService.delete(1L);

        verify(playerRepository).deleteById(1L);
    }

    @Test
    public void testDeleteNotFound() {
        when(playerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> playerService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(playerRepository, never()).deleteById(any());
    }
}
