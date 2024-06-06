package com.example.demo.Repository;

import com.example.demo.Domain.PresentationFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PresentationFileRepository extends JpaRepository<PresentationFile, Long> {
    List<PresentationFile> findByUserId(Long userId);
}
