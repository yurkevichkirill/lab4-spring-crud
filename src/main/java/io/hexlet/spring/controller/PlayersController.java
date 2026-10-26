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

import io.hexlet.spring.dto.PlayerCreateDTO;
import io.hexlet.spring.dto.PlayerDTO;
import io.hexlet.spring.dto.PlayerUpdateDTO;
import io.hexlet.spring.service.PlayerService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class PlayersController {

    @Autowired
    private PlayerService playerService;

    @GetMapping("/players")
    @ResponseStatus(HttpStatus.OK)
    public List<PlayerDTO> index() {
        return playerService.getAll();
    }

    @PostMapping("/players")
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerDTO create(@Valid @RequestBody PlayerCreateDTO playerData) {
        return playerService.create(playerData);
    }

    @GetMapping("/players/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PlayerDTO show(@PathVariable Long id) {
        return playerService.findById(id);
    }

    @PutMapping("/players/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PlayerDTO update(@Valid @RequestBody PlayerUpdateDTO playerData, @PathVariable Long id) {
        return playerService.update(playerData, id);
    }

    @DeleteMapping("/players/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        playerService.delete(id);
    }
}
