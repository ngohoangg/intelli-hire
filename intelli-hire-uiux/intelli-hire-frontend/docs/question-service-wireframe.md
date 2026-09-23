# Question Service UI/UX Wireframe

## Scope note

Tai lieu nay chi danh cho luong quan tri/backoffice.

Luong client-facing da duoc tach rieng tai:

- `docs/question-client-wireframe.md`

## 1. Audit nhanh

### Vấn đề UI hiện tại

- Header của `questions` đang là kiểu base CRUD (`eyebrow` + `h1`) nên lệch với header giàu nhận diện đang dùng ở `report` (`ai-dot`, count badge, back button, action button).
- Màn list hiện tại chỉ có 1 hàng filter đơn giản và 1 bảng; chưa có dashboard entry, chưa có stat cards, chưa có tab điều hướng các thực thể con của question-service.
- Form create/edit đang đi theo tư duy "question bank demo", chưa phải màn quản trị question-service đầy đủ.
- Detail hiện tại thiếu các khối quan trọng từ backend như rubric, random usage flow, related study articles.

### Vấn đề API mapping

- Frontend `questions` đang map sai domain:
  - frontend dùng `title`, `content`, `status`, `categoryId`, `answerTemplate`, `explanation`
  - backend thật dùng `topicId`, `difficultyId`, `questionText`, `questionType`, `tagIds`, `isActive`
- Frontend đang gọi `/api/v1/questions` và `/api/v1/question-categories`, trong khi backend thật expose:
  - `/v1/questions/search`
  - `/v1/topics`
  - `/v1/topics/tree`
  - `/v1/tags`
  - `/v1/difficulties`
  - `/v1/study-articles`
  - `/v1/questions/{id}/rubric`
- Frontend đang dùng `id: number`; backend dùng `id: string`.

## 2. Information Architecture đề xuất

Question service không nên chỉ là 1 list CRUD. Nên tổ chức thành 6 vùng chức năng:

1. Question Bank
2. Topics
3. Tags
4. Difficulty Levels
5. Study Articles
6. Tools

`Tools` gồm:

- Random Generator
- Bulk Import

Điểm chính: nếu màn landing chỉ có 4 ô thì thiếu mất 2 domain rất quan trọng là `Study Articles` và `Tools`, đồng thời cũng không phản ánh đúng scope API hiện tại.

## 3. Header chuẩn hóa theo module Report

Header của module `questions` nên dùng cùng ngôn ngữ với `report`:

- `page-container`
- `page-header`
- `page-title`
- `ai-dot`
- `count-badge`
- action chính ở góc phải

### Header chuẩn

- Dòng 1: icon dot + `Question Service`
- Dòng 2: subtitle ngắn: `Manage questions, taxonomy, rubric and learning resources`
- Góc phải:
  - `New Question`
  - `Open Random Lab`

## 4. Wireframe tổng thể

### 4.1 Landing Dashboard

```text
+--------------------------------------------------------------------------------------------------+
|  ai-dot  Question Service                                           [New Question] [Random Lab] |
|  Manage questions, taxonomy, rubric and learning resources                                     |
+--------------------------------------------------------------------------------------------------+

+----------------------+ +----------------------+ +----------------------+ +----------------------+
| Question Bank        | | Topics               | | Tags                 | | Difficulty Levels    |
| 1,248 questions      | | 18 topics            | | 42 tags              | | 5 levels             |
| Active / inactive    | | tree hierarchy       | | reusable labels      | | weighted scoring     |
| [Open]               | | [Manage]             | | [Manage]             | | [Manage]             |
+----------------------+ +----------------------+ +----------------------+ +----------------------+

+----------------------+ +----------------------+ +----------------------------------------------------+
| Study Articles       | | Random Generator     | | Recent Activity                                    |
| 156 resources        | | build mock interview | | - Question updated                                 |
| by topic/difficulty  | | test filters/cache   | | - Rubric added                                     |
| [Open]               | | [Open Lab]           | | - Topic created                                    |
+----------------------+ +----------------------+ +----------------------------------------------------+

+--------------------------------------------------------------------------------------------------+
| Quick Actions                                                                                   |
| [Bulk Import] [Create Topic] [Create Tag] [Create Difficulty] [Create Study Article]            |
+--------------------------------------------------------------------------------------------------+
```

### 4.2 Question Bank List

```text
+--------------------------------------------------------------------------------------------------+
| <- Back   Question Bank                                             [Bulk Import] [New Question] |
| Search, filter and curate the interview question pool                                           |
+--------------------------------------------------------------------------------------------------+

+-------------------------------+ +------------------------------------------------------------------+
| Filter Panel                  | | Result Toolbar                                                   |
| Keyword                       | | [All] [Active] [Inactive] [Technical] [Coding] [Behavioral]     |
| Topic tree                    | | Sort: [Latest v]  Total: 1248                                   |
| Difficulty                    | +------------------------------------------------------------------+
| Type                          | | Table / Cards                                                    |
| Tags multi-select             | |------------------------------------------------------------------|
| Status active/inactive        | | Question Text        Topic       Difficulty  Type   Tags Actions |
| [Reset] [Apply]               | | ...                                                              |
+-------------------------------+ | ...                                                              |
                                  | ...                                                              |
                                  +------------------------------------------------------------------+
                                  | Pagination                                                       |
                                  +------------------------------------------------------------------+
```

### 4.3 Question Detail

```text
+--------------------------------------------------------------------------------------------------+
| <- Back   Question Detail                                                       [Edit] [Archive] |
+--------------------------------------------------------------------------------------------------+

+--------------------------------------------------------+ +---------------------------------------+
| Main Question Card                                     | | Metadata Card                         |
| Question Text                                          | | Topic                                 |
| Type chip                                              | | Difficulty                            |
| Active badge                                           | | Created at                            |
| Tags                                                   | | Updated at                            |
+--------------------------------------------------------+ +---------------------------------------+

+--------------------------------------------------------+ +---------------------------------------+
| Rubric                                                 | | Related Study Articles                |
| Criteria                                               | | Article 1                             |
| Keywords                                               | | Article 2                             |
| Common mistakes                                        | | Article 3                             |
| Hints                                                  | | [View all by topic+difficulty]        |
| Example answer                                         | |                                       |
| Max score                                              | |                                       |
| [Create Rubric] / [Edit Rubric]                        | |                                       |
+--------------------------------------------------------+ +---------------------------------------+

+--------------------------------------------------------------------------------------------------+
| Usage Tools                                                                                      |
| [Generate random set with this topic] [Open topic questions] [Duplicate as new]                 |
+--------------------------------------------------------------------------------------------------+
```

### 4.4 Create / Edit Question

```text
+--------------------------------------------------------------------------------------------------+
| <- Back   New Question                                                            [Cancel][Save] |
+--------------------------------------------------------------------------------------------------+

+--------------------------------------------------------+ +---------------------------------------+
| Core Information                                       | | Classification                        |
| Question Text (large textarea)                         | | Topic                                 |
|                                                        | | Difficulty                            |
|                                                        | | Question Type                         |
|                                                        | | Active toggle                         |
+--------------------------------------------------------+ +---------------------------------------+

+--------------------------------------------------------+ +---------------------------------------+
| Tags                                                   | | Rubric Setup (optional after create)  |
| multi-select + quick create tag                        | | Criteria                              |
|                                                        | | Keywords                              |
|                                                        | | Example Answer                        |
|                                                        | | Mistakes / Hints / Max score          |
+--------------------------------------------------------+ +---------------------------------------+
```

### 4.5 Study Article Library

```text
+--------------------------------------------------------------------------------------------------+
| <- Back   Study Articles                                                [New Article]            |
+--------------------------------------------------------------------------------------------------+

+--------------------------------+ +-----------------------------------------------------------------+
| Topic                          | | Article cards / table                                            |
| Difficulty                     | | Title                                                            |
| Active                         | | Source                                                           |
| [Reset] [Apply]                | | Topic + Difficulty                                               |
+--------------------------------+ | URL                                                               |
                                   | Description                                                       |
                                   +-----------------------------------------------------------------+
```

## 5. Mapping API theo màn

### 5.1 Landing Dashboard

Gọi song song:

- `GET /v1/questions/search?page=0&size=1&isActive=true`
  - lấy `total_elements` để hiện tổng question active
- `GET /v1/questions/search?page=0&size=1&isActive=false`
  - lấy count inactive nếu cần tách card
- `GET /v1/topics`
- `GET /v1/tags`
- `GET /v1/difficulties`
- `GET /v1/study-articles?page=0&size=1`

Client tự tính:

- tổng topics = `topics.length`
- tổng tags = `tags.length`
- tổng difficulties = `difficulties.length`
- tổng study articles = `page.total_elements`

### 5.2 Question Bank List

Filter options:

- `GET /v1/topics/tree`
- `GET /v1/difficulties`
- `GET /v1/tags`

Search chính:

- `GET /v1/questions/search`

Query params nên dùng:

- `page`
- `size`
- `sort`
- `topicId`
- `difficultyId`
- `type`
- `tagIds`
- `keyword`
- `isActive`

Sort nên hỗ trợ theo backend:

- `createdAt,desc`
- `lastUpdatedAt,desc`
- `questionText,asc`
- `questionType,asc`

### 5.3 Question Detail

Gọi song song:

- `GET /v1/questions/{id}`
- `GET /v1/questions/{id}/rubric`
  - nếu `404 RUB_001` thì render empty state rubric

Block `Related Study Articles`:

- `GET /v1/study-articles?topicId={topicId}&difficultyId={difficultyId}&page=0&size=3`

CTA phụ:

- `GET /v1/topics/{topicId}/questions?page=0&size=10`
  - khi user muốn xem thêm question cùng topic

### 5.4 Create Question

Init form:

- `GET /v1/topics/tree`
- `GET /v1/difficulties`
- `GET /v1/tags`

Submit:

- `POST /v1/questions`

Payload:

```json
{
  "topic_id": "topic-id",
  "difficulty_id": "difficulty-id",
  "question_text": "Explain dependency injection",
  "question_type": "TECHNICAL",
  "tag_ids": ["tag-1", "tag-2"],
  "is_active": true
}
```

Nếu user nhập rubric ngay sau khi tạo:

- `POST /v1/questions/{id}/rubric`

### 5.5 Edit Question

Init:

- `GET /v1/questions/{id}`
- `GET /v1/questions/{id}/rubric`
- `GET /v1/topics/tree`
- `GET /v1/difficulties`
- `GET /v1/tags`

Submit:

- `PUT /v1/questions/{id}`
- rubric:
  - `PUT /v1/questions/{id}/rubric` nếu đã có
  - `POST /v1/questions/{id}/rubric` nếu chưa có

### 5.6 Random Generator Lab

Màn này nên là drawer hoặc modal lớn từ dashboard/detail.

Call:

- `GET /v1/questions/random`

Params:

- `count`
- `topicId`
- `difficultyId`
- `type`
- `tagIds`
- `excludeIds`

Use case:

- test pool theo topic
- tạo nhanh bộ câu hỏi thử nghiệm
- verify cache/filter backend

### 5.7 Bulk Import

Call:

- `POST /v1/questions/bulk`

UX:

- upload textarea/json editor/table mapping
- hiển thị result summary:
  - total
  - created
  - failed
  - per-row errors

### 5.8 Topics / Tags / Difficulties / Study Articles

Topics:

- `GET /v1/topics`
- `GET /v1/topics/tree`
- `POST /v1/topics`
- `PUT /v1/topics/{id}`
- `DELETE /v1/topics/{id}`
- `GET /v1/topics/{id}/questions`

Tags:

- `GET /v1/tags`
- `POST /v1/tags`
- `PUT /v1/tags/{id}`
- `DELETE /v1/tags/{id}`

Difficulties:

- `GET /v1/difficulties`
- `POST /v1/difficulties`
- `GET /v1/difficulties/{id}`
- `PUT /v1/difficulties/{id}`
- `DELETE /v1/difficulties/{id}`

Study articles:

- `GET /v1/study-articles`
- `GET /v1/study-articles/{id}`
- `POST /v1/study-articles`
- `PUT /v1/study-articles/{id}`
- `DELETE /v1/study-articles/{id}`

## 6. Component tree đề xuất

```text
features/questions/
  pages/
    question-dashboard/
    question-list/
    question-detail/
    question-form/
    question-random-lab/
    topic-management/
    tag-management/
    difficulty-management/
    study-article-list/
    study-article-form/
  components/
    question-header/
    question-stats-grid/
    question-filter-panel/
    question-table/
    rubric-card/
    related-articles-card/
    taxonomy-chip-list/
    bulk-import-panel/
```

## 7. Thứ tự triển khai khuyến nghị

### Phase 1

- Sửa model/service frontend cho đúng DTO backend
- Làm lại header/layout đồng bộ với `report`
- Refactor Question List theo filter panel + result table

### Phase 2

- Thêm Question Dashboard
- Thêm Rubric card ở Question Detail
- Thêm Related Study Articles

### Phase 3

- Thêm Topics / Tags / Difficulties / Study Articles management
- Thêm Random Lab
- Thêm Bulk Import

## 8. Kết luận

Nếu làm chuẩn theo API hiện tại, `question-service` phải là một module quản trị tri thức phỏng vấn, không phải chỉ là một CRUD list câu hỏi. Trục chính nên là:

- dashboard điều hướng
- question bank mạnh về filter/search
- detail có rubric + article liên quan
- taxonomy management đầy đủ
- tools cho random và bulk import

Đó là cấu trúc đồng bộ nhất với độ hoàn thiện của module `report`, đồng thời bám sát backend hiện có để tránh làm lại lần hai.

## 9. Cross-service note

Admin flow va client flow phai dung cung taxonomy nguon tu Question Service. Neu client flow van dung payload text tu do o Interview Service thi du lieu se tiep tuc lech giua:

- admin taxonomy
- interview session
- report
- study recommendation

Vi vay khi trien khai client flow can chot them contract voi:

- Interview Service
- Report Service
- Candidate Service hoac Question Service cho study article reads
