package com.example.demo.Repository;

import com.example.demo.Domain.Presentation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PresentationRepository extends JpaRepository<Presentation, Long> {
    List<Presentation> findByPresentationType(String presentationType);
}
