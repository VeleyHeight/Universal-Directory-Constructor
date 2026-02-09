package org.practice.universal_directory_constructor.repository;

import org.practice.universal_directory_constructor.entity.Directory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DirectoryRepository extends JpaRepository<Directory, Long> {
    @Query(value = """
        select d.id
        from Directory d
        where d.id in :ids
    """)
    List<Long> findExistingByIds(@Param("ids") Set<Long> ids);
}
