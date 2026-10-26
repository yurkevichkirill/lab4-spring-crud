package io.hexlet.spring.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.hexlet.spring.dto.PlayerCreateDTO;
import io.hexlet.spring.dto.PlayerDTO;
import io.hexlet.spring.dto.PlayerUpdateDTO;
import io.hexlet.spring.exception.ResourceNotFoundException;
import io.hexlet.spring.mapper.PlayerMapper;
import io.hexlet.spring.model.Player;
import io.hexlet.spring.repository.PlayerRepository;
import io.hexlet.spring.repository.TeamRepository;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerMapper playerMapper;

    @Transactional(readOnly = true)
    public List<PlayerDTO> getAll() {
        return playerRepository.findAll().stream()
                .map(playerMapper::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlayerDTO findById(Long id) {
        return playerMapper.map(getPlayer(id));
    }

    @Transactional
    public PlayerDTO create(PlayerCreateDTO playerData) {
        checkTeamExists(playerData.getTeamId());
        var player = playerMapper.map(playerData);
        playerRepository.save(player);
        return playerMapper.map(player);
    }

    @Transactional
    public PlayerDTO update(PlayerUpdateDTO playerData, Long id) {
        var player = getPlayer(id);
        if (playerData.getTeamId() != null && playerData.getTeamId().isPresent()) {
            checkTeamExists(playerData.getTeamId().get());
        }
        playerMapper.update(playerData, player);
        playerRepository.saveAndFlush(player);
        return playerMapper.map(player);
    }

    @Transactional
    public void delete(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Player Not Found: " + id);
        }
        playerRepository.deleteById(id);
    }

    private Player getPlayer(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player Not Found: " + id));
    }

    private void checkTeamExists(Long teamId) {
        if (teamId == null || !teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team Not Found: " + teamId);
        }
    }
}
