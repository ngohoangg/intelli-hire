package org.aibles.intellihirequestion.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.aibles.intellihirequestion.entity.enums.QuestionType;

@Converter(autoApply = false)
public class QuestionTypeConverter implements AttributeConverter<QuestionType, String> {

    @Override
    public String convertToDatabaseColumn(QuestionType attribute) {
        return attribute == null ? null : attribute.name().toLowerCase();
    }

    @Override
    public QuestionType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return QuestionType.valueOf(dbData.toUpperCase());
    }
}
