package org.aibles.intellihirequestion.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "difficulty_levels")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DifficultyLevel extends BaseEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "weight", nullable = false)
    private Integer weight;

    @Column(name = "description", length = 255)
    private String description;
}