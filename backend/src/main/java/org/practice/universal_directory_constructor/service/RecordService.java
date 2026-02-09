package org.practice.universal_directory_constructor.service;

import org.practice.universal_directory_constructor.dto.RecordDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

import java.util.List;

public interface RecordService {
    PagedModel<RecordDTO> findAllPagination(Long id, Pageable pageable, String search);
    List<RecordDTO> findAllForDirectory(Long id, String search);
    RecordDTO save(Long id, RecordDTO record);
    void delete(Long id);
}
