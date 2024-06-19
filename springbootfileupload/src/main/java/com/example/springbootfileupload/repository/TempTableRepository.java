package com.example.springbootfileupload.repository;

import com.example.springbootfileupload.entity.TempTableEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TempTableRepository extends JpaRepository<TempTableEntry, Long> {
    List<TempTableEntry> findByUploadedFileId(Long fileId);
}
