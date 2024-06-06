package com.example.demo.Domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name="presentation")
public class Presentation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long presentation_id;

    @Column(name = "presentation_type", nullable = false)
    private String presentationType;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "presentation_id")
    @OrderBy("id ASC")
    private List<Slide> slides = new ArrayList<>();
}
