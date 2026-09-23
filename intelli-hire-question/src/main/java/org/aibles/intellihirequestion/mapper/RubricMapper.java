package org.aibles.intellihirequestion.mapper;

import org.aibles.intellihirequestion.dto.RubricRequest;
import org.aibles.intellihirequestion.dto.RubricResponse;
import org.aibles.intellihirequestion.entity.Rubric;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class RubricMapper {

    public Rubric toEntity(String questionId, RubricRequest request) {
        return Rubric.builder()
                .questionId(questionId)
                .criteria(request.getCriteria())
                .keywords(listToJsonMap("keywords", request.getKeywords()))
                .exampleAnswer(request.getExampleAnswer())
                .commonMistakes(listToJsonMap("common_mistakes", request.getCommonMistakes()))
                .hints(listToJsonMap("hints", request.getHints()))
                .maxScore(request.getMaxScore())
                .isActive(true)
                .build();
    }

    public void updateEntity(Rubric rubric, RubricRequest request) {
        rubric.setCriteria(request.getCriteria());
        rubric.setKeywords(listToJsonMap("keywords", request.getKeywords()));
        rubric.setExampleAnswer(request.getExampleAnswer());
        rubric.setCommonMistakes(listToJsonMap("common_mistakes", request.getCommonMistakes()));
        rubric.setHints(listToJsonMap("hints", request.getHints()));
        rubric.setMaxScore(request.getMaxScore());
    }

    public RubricResponse toResponse(Rubric rubric) {
        return RubricResponse.builder()
                .id(rubric.getId())
                .questionId(rubric.getQuestionId())
                .criteria(rubric.getCriteria())
                .keywords(jsonMapToList(rubric.getKeywords(), "keywords"))
                .exampleAnswer(rubric.getExampleAnswer())
                .commonMistakes(jsonMapToList(rubric.getCommonMistakes(), "common_mistakes"))
                .hints(jsonMapToList(rubric.getHints(), "hints"))
                .maxScore(rubric.getMaxScore())
                .isActive(rubric.getIsActive())
                .createdAt(rubric.getCreatedAt())
                .lastUpdatedAt(rubric.getLastUpdatedAt())
                .build();
    }

    private Map<String, Object> listToJsonMap(String key, List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return Map.of(key, list);
    }

    @SuppressWarnings("unchecked")
    private List<String> jsonMapToList(Map<String, Object> map, String expectedKey) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyList();
        }
        Object value = map.get(expectedKey);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return Collections.emptyList();
    }
}
