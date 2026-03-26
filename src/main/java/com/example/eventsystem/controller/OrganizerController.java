package com.example.eventsystem.controller;

import com.example.eventsystem.model.dto.OrganizerRequestDto;
import com.example.eventsystem.model.dto.OrganizerResponseDto;
import com.example.eventsystem.service.OrganizerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizers")
@RequiredArgsConstructor
public class OrganizerController {

    private final OrganizerService organizerService;

    @PostMapping
    public ResponseEntity<OrganizerResponseDto> create(@Valid @RequestBody OrganizerRequestDto requestDto) {
        return new ResponseEntity<>(organizerService.create(requestDto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrganizerResponseDto>> getAll() {
        return ResponseEntity.ok(organizerService.getAllOrganizers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizerResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(organizerService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrganizerResponseDto>> search(@RequestParam String name) {
        return ResponseEntity.ok(organizerService.searchByName(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizerResponseDto> update(@PathVariable Long id,
                                                       @Valid @RequestBody OrganizerRequestDto requestDto) {
        return ResponseEntity.ok(organizerService.update(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        organizerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
