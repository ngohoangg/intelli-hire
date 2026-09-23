package org.aibles.intellihirequestion.specification;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.aibles.intellihirequestion.entity.Question;
import org.aibles.intellihirequestion.entity.QuestionTag;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class QuestionSpecification {
    public static Specification<Question>
    withTopicId(String topicId) {
        if (topicId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("topicId"), topicId);
    }
    public static Specification<Question>
    withDifficultyId(String difficultyId) {
        if (difficultyId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("difficultyId"), difficultyId);
    }
    public static Specification<Question>
    withType(String type) {
        if (type == null) return null;
        return (root, query, cb) -> cb.equal(root.get("questionType"), type);
    }
    public static Specification<Question>
    withIsActive(Boolean isActive) {
        if (isActive==null) return null;
        return (root, query, cb) -> cb.equal(root.get("isActive"), isActive);
    }

    public static Specification<Question>
    withKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String escaped = keyword.toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return (root, query, cb) -> cb.like(cb.lower(root.get("questionText")), "%" + escaped + "%", '\\');
    }
    public static Specification<Question>
    withStatus(String status) {
        if (status == null || status.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Question>
    withSubmittedBy(String submittedBy) {
        if (submittedBy == null || submittedBy.isBlank()) return null;
        String escaped = submittedBy.toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return (root, query, cb) -> cb.like(cb.lower(root.get("submittedBy")), "%" + escaped + "%", '\\');
    }

    public static Specification<Question>
    withSubmittedById(Long submittedById) {
        if (submittedById == null) return null;
        return (root, query, cb) -> cb.equal(root.get("submittedById"), submittedById);
    }

    public static Specification<Question>
    withTagIds(List<String> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return null;
        return (root, query, cb) -> {
            Subquery<String> subquery =
                    query.subquery(String.class);
            Root<QuestionTag> qtRoot =
                    subquery.from(QuestionTag.class);
            subquery.select(qtRoot.get("questionId"))
                    .where(qtRoot.get("tagId").in(tagIds));
            return root.get("id").in(subquery);
        };
    }
}
