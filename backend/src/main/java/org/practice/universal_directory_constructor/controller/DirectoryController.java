package org.practice.universal_directory_constructor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.practice.universal_directory_constructor.dto.directoriesCountData.DirectoryAndCount;
import org.practice.universal_directory_constructor.dto.DirectoryDTOCreate;
import org.practice.universal_directory_constructor.dto.DirectoryDTO;
import org.practice.universal_directory_constructor.service.DirectoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directories")
public class DirectoryController {
    private final DirectoryService service;

    @GetMapping
    public List<DirectoryAndCount> findAll() {
        return service.findAll();
    }

    @PostMapping
    public DirectoryDTO save(@RequestBody @Valid DirectoryDTOCreate dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public DirectoryDTO update(@PathVariable Long id, @RequestBody @Valid DirectoryDTO dto) {
        return service.update(id, dto);
    }
}
