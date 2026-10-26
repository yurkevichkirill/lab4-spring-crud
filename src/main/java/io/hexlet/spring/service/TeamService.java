package io.hexlet.spring.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.hexlet.spring.dto.PlayerDTO;
import io.hexlet.spring.dto.TeamCreateDTO;
import io.hexlet.spring.dto.TeamDTO;
import io.hexlet.spring.dto.TeamUpdateDTO;
import io.hexlet.spring.exception.ResourceConflictException;
import io.hexlet.spring.exception.ResourceNotFoundException;
import io.hexlet.spring.mapper.PlayerMapper;
import io.hexlet.spring.mapper.TeamMapper;
import io.hexlet.spring.model.Team;
import io.hexlet.spring.repository.PlayerRepository;
import io.hexlet.spring.repository.TeamRepository;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private PlayerMapper playerMapper;

    @Transactional(readOnly = true)
    public List<TeamDTO> getAll() {
        return teamRepository.findAll().stream()
                .map(teamMapper::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamDTO findById(Long id) {
        return teamMapper.map(getTeam(id));
    }

    @Transactional
    public TeamDTO create(TeamCreateDTO teamData) {
        if (teamRepository.findByName(teamData.getName()).isPresent()) {
            throw new ResourceConflictException("Team already exists: " + teamData.getName());
        }
        var team = teamMapper.map(teamData);
        teamRepository.save(team);
        return teamMapper.map(team);
    }

    @Transactional
    public TeamDTO update(TeamUpdateDTO teamData, Long id) {
        var team = getTeam(id);
        if (teamData.getName() != null && teamData.getName().isPresent()) {
            var newName = teamData.getName().get();
            teamRepository.findByName(newName)
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> {
                        throw new ResourceConflictException("Team already exists: " + newName);
                    });
        }
        teamMapper.update(teamData, team);
        teamRepository.saveAndFlush(team);
        return teamMapper.map(team);
    }

    @Transactional
    public void delete(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new ResourceNotFoundException("Team Not Found: " + id);
        }
        if (playerRepository.existsByTeamId(id)) {
            throw new ResourceConflictException("Team " + id + " has players; delete or move them first");
        }
        teamRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<PlayerDTO> getPlayers(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team Not Found: " + teamId);
        }
        return playerRepository.findByTeamId(teamId).stream()
                .map(playerMapper::map)
                .toList();
    }

    private Team getTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team Not Found: " + id));
    }
}
