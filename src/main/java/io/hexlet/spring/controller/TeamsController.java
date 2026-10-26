package io.hexlet.spring.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.hexlet.spring.dto.PlayerDTO;
import io.hexlet.spring.dto.TeamCreateDTO;
import io.hexlet.spring.dto.TeamDTO;
import io.hexlet.spring.dto.TeamUpdateDTO;
import io.hexlet.spring.service.TeamService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class TeamsController {

    @Autowired
    private TeamService teamService;

    @GetMapping("/teams")
    @ResponseStatus(HttpStatus.OK)
    public List<TeamDTO> index() {
        return teamService.getAll();
    }

    @PostMapping("/teams")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamDTO create(@Valid @RequestBody TeamCreateDTO teamData) {
        return teamService.create(teamData);
    }

    @GetMapping("/teams/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TeamDTO show(@PathVariable Long id) {
        return teamService.findById(id);
    }

    @PutMapping("/teams/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TeamDTO update(@Valid @RequestBody TeamUpdateDTO teamData, @PathVariable Long id) {
        return teamService.update(teamData, id);
    }

    @DeleteMapping("/teams/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        teamService.delete(id);
    }

    @GetMapping("/teams/{id}/players")
    @ResponseStatus(HttpStatus.OK)
    public List<PlayerDTO> players(@PathVariable Long id) {
        return teamService.getPlayers(id);
    }
}
