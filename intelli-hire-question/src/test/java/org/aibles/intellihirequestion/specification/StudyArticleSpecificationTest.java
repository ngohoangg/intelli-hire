package org.aibles.intellihirequestion.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.aibles.intellihirequestion.entity.StudyArticle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("StudyArticleSpecification")
class StudyArticleSpecificationTest {

    @Test
    @DisplayName("Should always apply active filter when no optional filters are provided")
    void shouldApplyActiveFilter_whenNoOptionalFiltersProvided() {
        executeSpecification(null, null);
    }

    @Test
    @DisplayName("Should apply topic filter when topicId is provided")
    void shouldApplyTopicFilter_whenTopicIdProvided() {
        TestCriteriaContext context = executeSpecification("topic-1", null);

        verify(context.criteriaBuilder).equal(context.topicPath, "topic-1");
        verify(context.criteriaBuilder, never()).equal(context.difficultyPath, "difficulty-1");
    }

    @Test
    @DisplayName("Should apply difficulty filter when difficultyId is provided")
    void shouldApplyDifficultyFilter_whenDifficultyIdProvided() {
        TestCriteriaContext context = executeSpecification(null, "difficulty-1");

        verify(context.criteriaBuilder).equal(context.difficultyPath, "difficulty-1");
        verify(context.criteriaBuilder, never()).equal(context.topicPath, "topic-1");
    }

    private TestCriteriaContext executeSpecification(String topicId, String difficultyId) {
        Root<StudyArticle> root = mock(Root.class);
        @SuppressWarnings("unchecked")
        CriteriaQuery<StudyArticle> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<Object> activePath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> topicPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> difficultyPath = mock(Path.class);
        Predicate combinedPredicate = mock(Predicate.class);

        when(root.get("isActive")).thenReturn(activePath);
        when(root.get("topicId")).thenReturn(topicPath);
        when(root.get("difficultyId")).thenReturn(difficultyPath);
        when(criteriaBuilder.equal(activePath, true)).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.equal(topicPath, topicId)).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.equal(difficultyPath, difficultyId)).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(combinedPredicate);

        Specification<StudyArticle> specification = StudyArticleSpecification.withFilters(topicId, difficultyId);
        specification.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).equal(activePath, true);

        return new TestCriteriaContext(criteriaBuilder, topicPath, difficultyPath);
    }

    private record TestCriteriaContext(CriteriaBuilder criteriaBuilder, Path<Object> topicPath, Path<Object> difficultyPath) {
    }
}
