package org.aibles.intellihirequestion.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "topics")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Topic extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "parent_id")
    private String parentId;

    @Column(name = "\"order\"")
    private Integer order = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}