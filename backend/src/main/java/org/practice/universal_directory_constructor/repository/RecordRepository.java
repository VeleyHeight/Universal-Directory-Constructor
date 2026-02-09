package org.practice.universal_directory_constructor.repository;

import org.practice.universal_directory_constructor.dto.directoriesCountData.CountsForDirectory;
import org.practice.universal_directory_constructor.dto.recordCheckReference.RepositoryResultDTO;
import org.practice.universal_directory_constructor.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long>, JpaSpecificationExecutor<Record>{
    @Query("select r.id as id, r.directory.id as dirId from Record r where r.id in :ids")
    List<RepositoryResultDTO> findIdAndDirIdByRecordIds(@Param("ids") Set<Long> ids);
    @Query("""
    select r.directory.id, count(r)
    from Record r
    where r.directory.id in :ids
    group by r.directory.id
""")
    List<CountsForDirectory> findCountForDirectory(@Param("ids") Set<Long> ids);
}
