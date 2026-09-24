package org.aibles.intellihireresume.service.ai;

/**
 * Single source of truth for the CV/resume-parsing system prompt shared by every
 * {@link org.aibles.intellihireresume.service.ai.AiProvider} implementation
 * (OpenAI, Claude, Gemini, Grok, OpenRouter). Previously each provider carried its
 * own copy of this text — extracted here so the parsing contract (JSON shape,
 * catalog constraint, output-language instruction) only needs to be maintained
 * in one place.
 */
public final class ResumeParsePromptBuilder {

    private ResumeParsePromptBuilder() {
    }

    /**
     * Builds the system prompt instructing the model how to parse a CV into
     * structured JSON.
     *
     * @param catalogStr     comma-joined skill catalog the model must match skill
     *                       names against exactly
     * @param outputLanguage human language the prose fields (summary, experience/
     *                       education description, skill evidenceText) must be
     *                       written in, e.g. "Vietnamese" or "English"
     */
    public static String build(String catalogStr, String outputLanguage) {
        return """
            You are a professional CV/resume parser. Extract structured information from the resume text.

            Rules:
            1. Return ONLY valid JSON. No explanation, no markdown, no code fences.
            2. For skills, ONLY use names from this exact catalog (case-sensitive):
               [%s]
            3. If a field cannot be determined, use null.
            4. confidenceScore: 0.0 = skill mentioned once, 1.0 = primary skill with clear years of evidence.
            5. isPrimary = true for skills central to the candidate's career.
            6. Write all human-readable prose text values — summary, experience description, education description, and skill evidenceText — in %s. Keep JSON keys, skill "name" values (must match the catalog exactly), dates, numbers, and booleans exactly as specified. Do not translate proper nouns extracted from the CV (company names, job titles, school names) — keep them exactly as written in the original text.

            Required JSON structure:
            {
              "contact": {"fullName": "...", "email": "...", "phone": "...", "linkedinUrl": "...", "location": "..."},
              "summary": "...",
              "experiences": [{"company": "...", "title": "...", "startYear": 2020, "startMonth": 3, "endYear": 2023, "endMonth": 6, "isCurrent": false, "description": "..."}],
              "educations": [{"school": "...", "degree": "...", "field": "...", "startYear": 2016, "endYear": 2020, "description": "..."}],
              "skills": [{"name": "...", "confidenceScore": 0.9, "yearsExperience": 3.0, "isPrimary": true, "evidenceText": "..."}]
            }
            """.formatted(catalogStr, outputLanguage);
    }
}
