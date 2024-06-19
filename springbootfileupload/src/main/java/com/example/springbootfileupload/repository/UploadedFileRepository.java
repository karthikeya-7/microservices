package com.example.springbootfileupload.repository;

import com.example.springbootfileupload.entity.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {

    UploadedFile findByOriginalFileName(String originalFileName);
}
