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
  { label: 'Mới nhất', value: 'createdAt,desc' },
  { label: 'Cập nhật gần đây', value: 'lastUpdatedAt,desc' },
  { label: 'Nội dung câu hỏi A-Z', value: 'questionText,asc' },
  { label: 'Loại câu hỏi', value: 'questionType,asc' }
] as const;

export function formatQuestionType(type: QuestionType | string | null | undefined): string {
  if (!type) {
    return 'Không xác định';
  }

  const labels: Record<string, string> = {
    TECHNICAL: 'Kỹ thuật',
    CODING: 'Lập trình',
    BEHAVIORAL: 'Hành vi'
  };
  return (
    labels[type] ??
    type
      .toLowerCase()
      .split('_')
      .map(part => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ')
  );
}

export function formatBooleanLabel(value: boolean, positive = 'Hoạt động', negative = 'Ngừng hoạt động'): string {
  return value ? positive : negative;
}

export function formatDateTime(value: string | null | undefined): string {
  if (!value) {
    return 'Không có';
  }

  return new Date(value).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

export function formatTagList(tags: TagResponse[]): string {
  return tags.length ? tags.map(tag => tag.name).join(', ') : 'Chưa có thẻ';
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
  return topics.find(topic => topic.id === topicId)?.name ?? 'Chưa gán';
}

export function buildBulkImportSample(): string {
  return JSON.stringify(
    {
      questions: [
        {
          topic_id: 'topic-uuid',
          difficulty_id: 'difficulty-uuid',
          question_text: 'Giải thích dependency injection trong Spring Boot.',
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
