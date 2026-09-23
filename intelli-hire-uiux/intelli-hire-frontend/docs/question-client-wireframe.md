# Question Client Flow Wireframe

## 1. Scope

Tai lieu nay mo ta luong client-facing cho nguoi dung cuoi cua he thong, tach biet voi tai lieu quan tri/backoffice.

Client flow o day bao gom:

- chon topic va cau hinh interview practice
- bat dau interview session
- nhan cau hoi theo thoi gian thuc
- tra loi tung cau
- ket thuc phien
- xem lai session
- mo report
- nhan study article recommendation sau phien

Khong bao gom:

- CRUD topic/tag/difficulty/question
- quan tri rubric
- bulk import
- taxonomy management

## 2. Vai tro va muc tieu

### Primary user

- Candidate hoac learner dang luyen phong van ky thuat

### Muc tieu nghiep vu

1. Chon dung chu de muon luyen
2. Bat dau nhanh, it thao tac
3. Tra loi theo tung cau voi cam giac tap trung
4. Xem lai toan bo cau hoi va cau tra loi
5. Chuyen muot sang report va roadmap
6. Nhan tai lieu hoc tiep theo theo dung topic/difficulty

## 3. Journey tong the

```text
Practice Hub
  -> Interview Setup
    -> Session Lobby
      -> Live Interview Room
        -> Completion State
          -> Session Review
            -> Report Detail
              -> Recommended Study Articles
```

## 4. Wireframe chi tiet

### 4.1 Practice Hub

Man nay la entry point client cho module question/interview, khong nen dua user thang vao form trong.

```text
+--------------------------------------------------------------------------------------------------+
|  ai-dot  Practice Hub                                                  [New Practice Session]    |
|  Train with curated questions, then review report and roadmap                                   |
+--------------------------------------------------------------------------------------------------+

+--------------------------------------+ +---------------------------------------------------------+
| Quick Start                          | | Recent Sessions                                         |
| Topic                                | | Java Spring Boot     COMPLETED   12 Mar 14:20           |
| Difficulty                           | | React Fundamentals   ACTIVE      Resume                 |
| Question Count                       | | SQL Basics           COMPLETED   View Report            |
| [Start Now]                          | |                                                         |
+--------------------------------------+ +---------------------------------------------------------+

+--------------------------+ +--------------------------+ +----------------------------------------+
| Recommended Topics       | | Prep Before Practice     | | Last Performance Snapshot              |
| Spring Boot              | | 3 study articles         | | Avg score 72%                          |
| React                    | | by selected topic        | | Weak skill: SQL joins                  |
| SQL                      | | [Open articles]          | | [Continue learning]                    |
+--------------------------+ +--------------------------+ +----------------------------------------+
```

### 4.2 Interview Setup

Man nay thay cho form hien tai dang qua don gian. Setup screen phai bam taxonomy cua question-service, khong cho nhap text tu do neu muon dong bo du lieu.

```text
+--------------------------------------------------------------------------------------------------+
| <- Back   New Practice Session                                                 [Cancel][Start]  |
+--------------------------------------------------------------------------------------------------+

+--------------------------------------------------------+ +---------------------------------------+
| Topic Selection                                        | | Session Options                       |
| Topic tree / searchable select                         | | Difficulty                            |
| Child topic chips                                      | | Question count                        |
| Topic summary                                          | | Question type                         |
+--------------------------------------------------------+ | Tag filters (optional)                |
                                                           | Mode: Practice / Mock Interview       |
                                                           +---------------------------------------+

+--------------------------------------------------------+ +---------------------------------------+
| Preparation Preview                                    | | Session Rules                         |
| 2-3 study articles matching topic+difficulty           | | No answer reveal during session       |
| Estimated focus areas                                  | | One question at a time                |
| [Open all resources]                                   | | End early allowed                     |
+--------------------------------------------------------+ +---------------------------------------+
```

### 4.3 Session Lobby

Day la trang thai sau khi tao session nhung truoc khi cau hoi dau tien duoc stream xuong.

```text
+--------------------------------------------------------------------------------------------------+
| Session Ready                                                                                     |
+--------------------------------------------------------------------------------------------------+

+--------------------------------------------------------------------------------------------------+
| Topic: Java Spring Boot                                                                           |
| Difficulty: Mid                                                                                   |
| Questions: 5                                                                                      |
| Type: Technical                                                                                   |
| Estimated duration: 15-20 min                                                                     |
+--------------------------------------------------------------------------------------------------+

+----------------------------------------+ +-------------------------------------------------------+
| Preparation Reminder                    | | Controls                                              |
| Answer clearly and concretely           | | [Start Session]                                       |
| Keep examples practical                 | | [Back]                                                |
| You can end session early               | |                                                       |
+----------------------------------------+ +-------------------------------------------------------+
```

### 4.4 Live Interview Room

Day la man quan trong nhat. UI phai toi uu cho trang thai tap trung, khong nhieu, khong giong man form CRUD.

```text
+--------------------------------------------------------------------------------------------------+
| Topic: Java Spring Boot                Q2/5                       40% progress     [End Session] |
+--------------------------------------------------------------------------------------------------+

+--------------------------------------------------------------------------------------------------+
| Question                                                                                         |
| Explain dependency injection in Spring and why it improves maintainability.                      |
| Type: Technical    Difficulty: Mid                                                               |
+--------------------------------------------------------------------------------------------------+

+--------------------------------------------------------+ +---------------------------------------+
| Your Answer                                             | | Session Context                       |
| Large textarea / autosave draft                         | | Current topic                         |
| word count / char count                                 | | Difficulty                            |
| [Submit & Next]                                         | | Remaining questions                   |
+--------------------------------------------------------+ | Connection status                     |
                                                           +---------------------------------------+
```

### 4.5 Completion State

Sau khi gui cau cuoi hoac end session.

```text
+--------------------------------------------------------------------------------------------------+
| Practice Session Complete                                                                           |
+--------------------------------------------------------------------------------------------------+

+--------------------------------------+ +---------------------------------------------------------+
| Summary                              | | Next actions                                            |
| 5/5 questions answered               | | [Review Session]                                        |
| Session ended successfully           | | [Open Report]                                           |
| Report is being generated            | | [Study Recommended Articles]                            |
+--------------------------------------+ +---------------------------------------------------------+
```

### 4.6 Session Review

Man nay dong vai tro bridge giua question flow va report flow.

```text
+--------------------------------------------------------------------------------------------------+
| <- Back   Session Review                                                      [Open Report]      |
+--------------------------------------------------------------------------------------------------+

+--------------------------------------------------------------------------------------------------+
| Session Header                                                                                   |
| Topic • Difficulty • Status • Created time • Total questions                                    |
+--------------------------------------------------------------------------------------------------+

+--------------------------------------------------------------------------------------------------+
| Q1                                                                                               |
| Question text                                                                                    |
| ANSWER                                                                                           |
| User answer                                                                                      |
+--------------------------------------------------------------------------------------------------+

+--------------------------------------------------------------------------------------------------+
| Q2                                                                                               |
| Question text                                                                                    |
| ANSWER                                                                                           |
| User answer                                                                                      |
+--------------------------------------------------------------------------------------------------+
```

### 4.7 Report Gateway

Man report da co rieng, nhung trong client flow can define ro entry.

```text
Session Review
  -> Open Report
    -> Report Detail
      -> Improvement Roadmap
      -> Skill Scores
      -> Question Scores
      -> Continue Learning CTA
```

### 4.8 Study Recommendations

Sau report hoac ngay tu setup screen, user co the mo learning recommendations.

```text
+--------------------------------------------------------------------------------------------------+
| <- Back   Recommended Study Articles                                           [Start New Set]   |
+--------------------------------------------------------------------------------------------------+

+--------------------------------------+ +---------------------------------------------------------+
| Filters                              | | Article List / Cards                                    |
| Topic                                | | Spring Boot Roadmap                                     |
| Difficulty                           | | Source: engineering.example.com                         |
| Source                               | | Topic: Spring Boot • Difficulty: Mid                    |
| [Apply]                              | | Short description                                       |
+--------------------------------------+ | [Open Article]                                           |
                                         +---------------------------------------------------------+
```

## 5. API mapping theo man

### 5.1 Practice Hub

#### Required

- `GET /v1/interviews/sessions?page=0&size=5`
  - recent sessions
- `GET /v1/reports/stats/{userId}`
  - last performance snapshot
- `GET /v1/reports/progress/{userId}`
  - weak skill / progress insight

#### Optional

- `GET /api/v1/resumes/{resumeId}/skill-profile`
  - prefill recommended topics from resume skill profile

### 5.2 Interview Setup

#### Read APIs

- `GET /v1/topics/tree`
- `GET /v1/difficulties`
- `GET /v1/tags`
- `GET /v1/study-articles?topicId={topicId}&difficultyId={difficultyId}&page=0&size=3`

#### Create session

Hien frontend dang dung:

- `POST /v1/interviews/sessions`

Payload hien tai:

```json
{
  "topic": "Java Spring Boot",
  "difficulty": "mid",
  "questionCount": 5
}
```

Payload nay chua dong bo voi taxonomy cua question-service.

#### Payload de xuat de dong bo lau dai

```json
{
  "topic_id": "topic-uuid",
  "difficulty_id": "difficulty-uuid",
  "question_type": "TECHNICAL",
  "tag_ids": ["tag-1", "tag-2"],
  "question_count": 5,
  "mode": "PRACTICE"
}
```

### 5.3 Session Lobby / Live Room

#### WebSocket

- `SockJS /ws/interview`
- subscribe `/topic/interview/{sessionId}`
- publish `/app/interview/{sessionId}/start`
- publish `/app/interview/{sessionId}/answer`

#### REST

- `GET /v1/interviews/sessions/{sessionId}`
  - hydrate session detail neu reload page
- `PUT /v1/interviews/sessions/{sessionId}/end`
  - end session som

### 5.4 Completion State / Session Review

- `GET /v1/interviews/sessions/{sessionId}`

Neu session completed:

- `GET /v1/reports/session/{sessionId}`
  - lay report neu da generate xong

Neu report chua san sang, UI nen poll nhe:

- `GET /v1/reports/session/{sessionId}` moi 3-5 giay trong thoi gian ngan

### 5.5 Report Detail

- `GET /v1/reports/{reportId}`

Optional:

- `POST /v1/reports/{reportId}/regenerate`
  - neu co cho phep regenerate roadmap tu UI client

### 5.6 Study Recommendations

#### Neu FE duoc phep goi truc tiep Question Service

- `GET /v1/study-articles?topicId={topicId}&difficultyId={difficultyId}&page=0&size=10`

#### Neu giu dung boundary theo BRD

- Candidate-facing article reads nen di qua `Candidate Service`, khong goi browser truc tiep vao Question Service.

## 6. Integration gaps hien tai

### Gap 1: taxonomy mismatch

Question Service dung:

- `topic_id`
- `difficulty_id`
- `tag_ids`
- `question_type`
- `question_id: string`

Interview Service hien dung:

- `topic: string`
- `difficulty: enum`
- `questionId: number`

Diem nay dang lech model va se gay kho:

- khong trace duoc cau hoi canonical theo UUID
- khong dam bao topic client chon dung taxonomy da quan tri
- kho join report/question/rubric/study article theo cung ngu nghia

### Gap 2: setup chua ho tro filter day du

Client flow mong muon can:

- topic
- difficulty
- question type
- tags
- question count

Trong khi create session hien tai moi co:

- topic
- difficulty
- questionCount

### Gap 3: report readiness

Luong client sau khi ket thuc session can biet report da san sang hay chua. Hien co the reuse:

- `GET /v1/reports/session/{sessionId}`

Nhung neu muon UX ro hon, nen co them mot contract nhe kieu:

- `GET /v1/reports/session/{sessionId}/status`

### Gap 4: study article boundary

BRD cua question-service gia dinh article recommendation cho candidate di qua Candidate Service, khong phai browser goi truc tiep. Neu san pham muon FE hien tai goi thang thi can chot lai:

- policy auth
- scope du lieu public
- filter `isActive=true`

### Gap 5: websocket payload con it metadata

Payload question realtime hien chu yeu co:

- `questionId`
- `questionText`
- `questionOrder`
- `totalQuestions`

Neu muon room UX tot hon, nen can nhac them:

- `topicName`
- `difficultyName`
- `questionType`

## 7. API contracts de xuat

### 7.1 Interview create v2

```json
{
  "topic_id": "topic-uuid",
  "difficulty_id": "difficulty-uuid",
  "question_type": "TECHNICAL",
  "tag_ids": ["tag-1", "tag-2"],
  "question_count": 5,
  "mode": "PRACTICE",
  "user_id": "current-user-id"
}
```

### 7.2 Interview session detail v2

```json
{
  "id": "session-id",
  "topic_id": "topic-uuid",
  "topic_name": "Java Spring Boot",
  "difficulty_id": "difficulty-uuid",
  "difficulty_name": "Mid",
  "status": "COMPLETED",
  "questions": [
    {
      "question_id": "question-uuid",
      "question_text": "Explain dependency injection",
      "question_order": 1,
      "question_type": "TECHNICAL"
    }
  ],
  "answers": [
    {
      "question_id": "question-uuid",
      "answer_text": "..."
    }
  ]
}
```

### 7.3 Candidate-safe study article read

```json
{
  "topic_id": "topic-uuid",
  "difficulty_id": "difficulty-uuid",
  "is_active": true,
  "page": 0,
  "size": 10
}
```

## 8. Component tree de xuat

```text
features/question-client/
  pages/
    practice-hub/
    interview-setup/
    session-lobby/
    live-interview-room/
    session-review/
    study-recommendations/
  components/
    practice-hero/
    recent-session-list/
    recommended-topic-card/
    setup-topic-picker/
    preparation-preview-card/
    interview-progress-header/
    answer-editor-card/
    completion-summary-card/
```

## 9. Thu tu trien khai khuyen nghi

### Phase A

- Chot contract client giua Question Service va Interview Service
- Sua create session de dung taxonomy ID hoac co mapping server-side ro rang
- Thiet ke lai `Interview Setup`

### Phase B

- Toi uu `Live Interview Room`
- Them completion state + report polling
- Noi study article recommendation sau session

### Phase C

- Practice Hub co personalized recommendation tu report + resume skill profile
- Tach route client flow ro voi admin flow

## 10. Ket luan

Luong client cua question module thuc chat la mot `practice and evaluation journey`, khong phai man list cau hoi. Truc nghiep vu dung nen la:

- chon dung taxonomy
- lam interview theo session
- xem lai cau tra loi
- mo report
- hoc tiep bang study article dung topic/difficulty

Neu khong chot truoc integration contract voi `Interview Service`, phan admin va client se tiep tuc dung hai mo hinh du lieu khac nhau.
