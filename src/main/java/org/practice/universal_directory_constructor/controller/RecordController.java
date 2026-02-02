package org.practice.universal_directory_constructor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.practice.universal_directory_constructor.dto.RecordDTO;
import org.practice.universal_directory_constructor.service.RecordService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records")
public class RecordController {
    private final RecordService recordService;

    @GetMapping("/{id}")
    public PagedModel<RecordDTO> findAllPagination(
            @PathVariable Long id,
            Pageable pageable,
            @RequestParam(required = false) String search
    ) {
        return recordService.findAllPagination(id, pageable, search);
    }

    @GetMapping("/{id}/all")
    public List<RecordDTO> findAllForDirectory(
            @PathVariable Long id,
            @RequestParam(required = false) String search
    ) {
        return recordService.findAllForDirectory(id, search);
    }

    @PostMapping("/{id}")
    public RecordDTO save(@PathVariable Long id, @RequestBody @Valid RecordDTO record) {
        return recordService.save(id, record);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        recordService.delete(id);
    }
}
