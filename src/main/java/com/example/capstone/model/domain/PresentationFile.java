package com.example.demo.Domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@Entity
public class PresentationFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fileId;

    @Column(nullable = false)
    private Long userId;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] fileData;
}
