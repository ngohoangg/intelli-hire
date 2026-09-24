package org.aibles.intellihireresume.service.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeParsePromptBuilderTest {

    private static final String CATALOG = "Java, Python, SQL";

    @Test
    void build_ShouldIncludeConfiguredOutputLanguage_WhenBuildingPrompt() {
        String prompt = ResumeParsePromptBuilder.build(CATALOG, "Vietnamese");

        assertThat(prompt).contains("in Vietnamese");
    }

    @Test
    void build_ShouldIncludeDifferentConfiguredOutputLanguage_WhenLanguageChanges() {
        String prompt = ResumeParsePromptBuilder.build(CATALOG, "English");

        assertThat(prompt).contains("in English");
        assertThat(prompt).doesNotContain("in Vietnamese");
    }

    @Test
    void build_ShouldIncludeSkillCatalog_WhenBuildingPrompt() {
        String prompt = ResumeParsePromptBuilder.build(CATALOG, "Vietnamese");

        assertThat(prompt).contains("[" + CATALOG + "]");
    }

    @Test
    void build_ShouldPreserveAllRequiredJsonKeys_WhenBuildingPrompt() {
        String prompt = ResumeParsePromptBuilder.build(CATALOG, "Vietnamese");

        assertThat(prompt)
                .contains("\"contact\"")
                .contains("\"fullName\"")
                .contains("\"email\"")
                .contains("\"phone\"")
                .contains("\"linkedinUrl\"")
                .contains("\"location\"")
                .contains("\"summary\"")
                .contains("\"experiences\"")
                .contains("\"company\"")
                .contains("\"title\"")
                .contains("\"startYear\"")
                .contains("\"startMonth\"")
                .contains("\"endYear\"")
                .contains("\"endMonth\"")
                .contains("\"isCurrent\"")
                .contains("\"description\"")
                .contains("\"educations\"")
                .contains("\"school\"")
                .contains("\"degree\"")
                .contains("\"field\"")
                .contains("\"skills\"")
                .contains("\"name\"")
                .contains("\"confidenceScore\"")
                .contains("\"yearsExperience\"")
                .contains("\"isPrimary\"")
                .contains("\"evidenceText\"");
    }

    @Test
    void build_ShouldReturnValidJson_WhenExtractingJsonStructure() {
        String prompt = ResumeParsePromptBuilder.build(CATALOG, "Vietnamese");

        assertThat(prompt).contains("Return ONLY valid JSON");
    }
}
