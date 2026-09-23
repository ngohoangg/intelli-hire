package org.aibles.intellihirequestion.specification;

import jakarta.persistence.criteria.Predicate;
import org.aibles.intellihirequestion.entity.StudyArticle;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

public final class StudyArticleSpecification {

    private StudyArticleSpecification() {
    }

    public static Specification<StudyArticle> withFilters(String topicId, String difficultyId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("isActive"), true));

            if (StringUtils.hasText(topicId)) {
                predicates.add(criteriaBuilder.equal(root.get("topicId"), topicId));
            }

            if (StringUtils.hasText(difficultyId)) {
                predicates.add(criteriaBuilder.equal(root.get("difficultyId"), difficultyId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
