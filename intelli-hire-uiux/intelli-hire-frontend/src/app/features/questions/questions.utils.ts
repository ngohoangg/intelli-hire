import {
  QuestionResponse,
  QuestionType,
  TagResponse,
  TopicResponse,
  TopicTreeResponse
} from '../../core/models/question.model';

export interface TopicOption {
  id: string;
  name: string;
  label: string;
  depth: number;
}

export interface ActionCard {
  title: string;
  value: string;
  subtitle: string;
  detail: string;
  cta: string;
  route: string;
}

export const QUESTION_TYPE_OPTIONS = [
  QuestionType.TECHNICAL,
  QuestionType.CODING,
  QuestionType.BEHAVIORAL
] as const;

export const QUESTION_SORT_OPTIONS = [
  { label: 'Latest', value: 'createdAt,desc' },
  { label: 'Recently Updated', value: 'lastUpdatedAt,desc' },
  { label: 'Question Text A-Z', value: 'questionText,asc' },
  { label: 'Question Type', value: 'questionType,asc' }
] as const;

/**
 * Returns a translation KEY for the question type — not display text. Callers
 * must pipe the result through `| translate` in the template, or
 * `TranslateService.instant()` when building a string outside a template.
 */
export function formatQuestionType(type: QuestionType | string | null | undefined): string {
  if (!type) {
    return 'questions.unknownType';
  }

  return `questions.questionTypes.${type}`;
}

export function formatBooleanLabel(value: boolean, positive = 'Active', negative = 'Inactive'): string {
  return value ? positive : negative;
}

export function formatDateTime(value: string | null | undefined, notAvailableLabel = 'N/A'): string {
  if (!value) {
    return notAvailableLabel;
  }

  return new Date(value).toLocaleString('vi-VN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

export function formatTagList(tags: TagResponse[]): string {
  return tags.length ? tags.map(tag => tag.name).join(', ') : 'No tags';
}

export function flattenTopicTree(
  nodes: TopicTreeResponse[],
  depth = 0
): TopicOption[] {
  return nodes.flatMap(node => {
    const prefix = depth > 0 ? `${'  '.repeat(depth)}└ ` : '';
    const current: TopicOption = {
      id: node.id,
      name: node.name,
      label: `${prefix}${node.name}`,
      depth
    };

    return [current, ...flattenTopicTree(node.children ?? [], depth + 1)];
  });
}

export function buildTopicLookup(topics: TopicResponse[]): Map<string, TopicResponse> {
  return new Map(topics.map(topic => [topic.id, topic]));
}

export function createQuestionDuplicationDraft(question: QuestionResponse) {
  return {
    topic_id: question.topic_id,
    difficulty_id: question.difficulty_id,
    question_text: question.question_text,
    question_type: question.question_type,
    tag_ids: question.tags.map(tag => tag.id),
    is_active: question.is_active
  };
}

export function parseListInput(raw: string): string[] {
  return raw
    .split('\n')
    .map(item => item.trim())
    .filter(Boolean);
}

export function getTopicName(topicId: string | null | undefined, topics: TopicOption[]): string {
  return topics.find(topic => topic.id === topicId)?.name ?? 'Unassigned';
}

export function buildBulkImportSample(): string {
  return JSON.stringify(
    {
      questions: [
        {
          topic_id: 'topic-uuid',
          difficulty_id: 'difficulty-uuid',
          question_text: 'Explain dependency injection in Spring Boot.',
          question_type: 'TECHNICAL',
          tag_ids: ['tag-uuid-1', 'tag-uuid-2'],
          is_active: true
        }
      ]
    },
    null,
    2
  );
}
