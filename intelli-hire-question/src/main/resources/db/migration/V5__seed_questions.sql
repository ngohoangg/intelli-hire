-- =============================================
-- Seed: Difficulty Levels
-- =============================================
INSERT INTO difficulty_levels (id, name, weight, description, created_at, created_by)
VALUES
  ('d1000000-0000-0000-0000-000000000001', 'EASY',   1, 'Foundational concepts, suitable for entry-level candidates',  CURRENT_TIMESTAMP, 'system'),
  ('d1000000-0000-0000-0000-000000000002', 'MEDIUM', 2, 'Intermediate concepts requiring solid practical experience',   CURRENT_TIMESTAMP, 'system'),
  ('d1000000-0000-0000-0000-000000000003', 'HARD',   3, 'Advanced topics requiring deep expertise and design thinking', CURRENT_TIMESTAMP, 'system')
ON CONFLICT (name) DO NOTHING;

-- =============================================
-- Seed: Topics
-- =============================================
INSERT INTO topics (id, name, description, is_active, created_at, created_by)
VALUES
  ('t1000000-0000-0000-0000-000000000001', 'Java',             'Core Java language and JVM internals',          true, CURRENT_TIMESTAMP, 'system'),
  ('t1000000-0000-0000-0000-000000000002', 'Spring Boot',      'Spring Boot framework and ecosystem',           true, CURRENT_TIMESTAMP, 'system'),
  ('t1000000-0000-0000-0000-000000000003', 'Data Structures',  'Arrays, lists, trees, graphs, and algorithms',  true, CURRENT_TIMESTAMP, 'system'),
  ('t1000000-0000-0000-0000-000000000004', 'Databases',        'SQL, relational databases, and query tuning',   true, CURRENT_TIMESTAMP, 'system'),
  ('t1000000-0000-0000-0000-000000000005', 'System Design',    'Scalable architecture and distributed systems', true, CURRENT_TIMESTAMP, 'system'),
  ('t1000000-0000-0000-0000-000000000006', 'JavaScript',       'JavaScript language fundamentals and ES6+',     true, CURRENT_TIMESTAMP, 'system'),
  ('t1000000-0000-0000-0000-000000000007', 'React',            'React library, hooks, and state management',    true, CURRENT_TIMESTAMP, 'system'),
  ('t1000000-0000-0000-0000-000000000008', 'DevOps',           'Docker, CI/CD pipelines, and cloud deployment', true, CURRENT_TIMESTAMP, 'system'),
  ('t1000000-0000-0000-0000-000000000009', 'Behavioral',       'Soft skills, teamwork, and situational questions', true, CURRENT_TIMESTAMP, 'system')
ON CONFLICT DO NOTHING;

-- =============================================
-- Seed: Questions
-- =============================================

-- ---- Java / EASY ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000001', 't1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000001',
   'What is the difference between JDK, JRE, and JVM?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000002', 't1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000001',
   'Explain the four pillars of Object-Oriented Programming with examples.', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000003', 't1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000001',
   'What is the difference between == and .equals() in Java?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000004', 't1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000001',
   'What are checked and unchecked exceptions in Java? Give examples of each.', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000005', 't1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000001',
   'What is the difference between an interface and an abstract class in Java?', 'technical', true, CURRENT_TIMESTAMP, 'system');

-- ---- Java / MEDIUM ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000006', 't1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000002',
   'Explain how the Java memory model works. What is the difference between the heap and the stack?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000007', 't1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000002',
   'What is the Java Collections Framework? Compare ArrayList, LinkedList, and HashMap.', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000008', 't1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000002',
   'Explain generics in Java. Why are they used and what is type erasure?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000009', 't1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000002',
   'What are functional interfaces and lambdas in Java 8? Give a practical example.', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000010', 't1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000002',
   'What is the difference between synchronized, volatile, and atomic variables in Java concurrency?', 'technical', true, CURRENT_TIMESTAMP, 'system');

-- ---- Java / HARD ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000011', 't1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000003',
   'Describe how the Java garbage collector works. What are the different GC algorithms and when would you tune them?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000012', 't1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000003',
   'Explain the happens-before relationship in Java''s memory model and how it affects concurrent programming.', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000013', 't1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000003',
   'How does the CompletableFuture API work? Implement a non-blocking pipeline that fetches data from two services and merges the results.', 'coding', true, CURRENT_TIMESTAMP, 'system');

-- ---- Spring Boot / EASY ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000014', 't1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000001',
   'What is Spring Boot and how does it differ from the Spring Framework?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000015', 't1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000001',
   'What is dependency injection? Explain constructor injection vs field injection.', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000016', 't1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000001',
   'What is the purpose of @SpringBootApplication? What annotations does it combine?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000017', 't1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000001',
   'What is Spring Data JPA and how does it simplify database access?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000018', 't1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000001',
   'How do you handle exceptions globally in a Spring Boot REST API?', 'technical', true, CURRENT_TIMESTAMP, 'system');

-- ---- Spring Boot / MEDIUM ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000019', 't1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000002',
   'Explain Spring Security''s filter chain. How would you implement JWT-based authentication?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000020', 't1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000002',
   'What is the difference between @Transactional propagation levels REQUIRED, REQUIRES_NEW, and NESTED?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000021', 't1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000002',
   'How does Spring Boot auto-configuration work? How would you create a custom auto-configuration?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000022', 't1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000002',
   'What is Spring Cache abstraction? How would you use @Cacheable and @CacheEvict with Redis?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000023', 't1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000002',
   'Explain the N+1 query problem in JPA and describe two ways to solve it.', 'technical', true, CURRENT_TIMESTAMP, 'system');

-- ---- Spring Boot / HARD ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000024', 't1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000003',
   'Design a distributed rate limiter in Spring Boot using Redis. What edge cases need to be handled?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000025', 't1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000003',
   'How would you implement the Saga pattern for distributed transactions across microservices in a Spring Boot system?', 'technical', true, CURRENT_TIMESTAMP, 'system');

-- ---- Data Structures / EASY ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000026', 't1000000-0000-0000-0000-000000000003', 'd1000000-0000-0000-0000-000000000001',
   'What is the difference between an array and a linked list? When would you prefer one over the other?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000027', 't1000000-0000-0000-0000-000000000003', 'd1000000-0000-0000-0000-000000000001',
   'Explain the concept of Big-O notation. What is the time complexity of binary search?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000028', 't1000000-0000-0000-0000-000000000003', 'd1000000-0000-0000-0000-000000000001',
   'What is a stack and a queue? Describe a real-world use case for each.', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000029', 't1000000-0000-0000-0000-000000000003', 'd1000000-0000-0000-0000-000000000001',
   'Reverse a singly linked list. What is the time and space complexity of your solution?', 'coding', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000030', 't1000000-0000-0000-0000-000000000003', 'd1000000-0000-0000-0000-000000000001',
   'How does a hash table work? What is a hash collision and how can it be resolved?', 'technical', true, CURRENT_TIMESTAMP, 'system');

-- ---- Data Structures / MEDIUM ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000031', 't1000000-0000-0000-0000-000000000003', 'd1000000-0000-0000-0000-000000000002',
   'Implement a function to check whether a binary tree is a valid binary search tree.', 'coding', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000032', 't1000000-0000-0000-0000-000000000003', 'd1000000-0000-0000-0000-000000000002',
   'Given an array of integers, find two numbers that add up to a target sum. Optimize for O(n) time complexity.', 'coding', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000033', 't1000000-0000-0000-0000-000000000003', 'd1000000-0000-0000-0000-000000000002',
   'Explain depth-first search and breadth-first search. When would you use each?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000034', 't1000000-0000-0000-0000-000000000003', 'd1000000-0000-0000-0000-000000000002',
   'What is dynamic programming? Solve the coin change problem using it.', 'coding', true, CURRENT_TIMESTAMP, 'system');

-- ---- Data Structures / HARD ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000035', 't1000000-0000-0000-0000-000000000003', 'd1000000-0000-0000-0000-000000000003',
   'Implement an LRU cache with O(1) get and put operations. Explain your data structure choices.', 'coding', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000036', 't1000000-0000-0000-0000-000000000003', 'd1000000-0000-0000-0000-000000000003',
   'Find the median of a data stream in real time. What data structures would you use?', 'coding', true, CURRENT_TIMESTAMP, 'system');

-- ---- Databases / EASY ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000037', 't1000000-0000-0000-0000-000000000004', 'd1000000-0000-0000-0000-000000000001',
   'What is the difference between INNER JOIN, LEFT JOIN, and FULL OUTER JOIN?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000038', 't1000000-0000-0000-0000-000000000004', 'd1000000-0000-0000-0000-000000000001',
   'What are database indexes? How do they improve query performance?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000039', 't1000000-0000-0000-0000-000000000004', 'd1000000-0000-0000-0000-000000000001',
   'What are the ACID properties in database transactions? Explain each one.', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000040', 't1000000-0000-0000-0000-000000000004', 'd1000000-0000-0000-0000-000000000001',
   'What is normalization? Describe the first three normal forms with examples.', 'technical', true, CURRENT_TIMESTAMP, 'system');

-- ---- Databases / MEDIUM ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000041', 't1000000-0000-0000-0000-000000000004', 'd1000000-0000-0000-0000-000000000002',
   'What is query execution plan? How would you use EXPLAIN ANALYZE to diagnose a slow query?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000042', 't1000000-0000-0000-0000-000000000004', 'd1000000-0000-0000-0000-000000000002',
   'What is database sharding? What are the trade-offs between horizontal and vertical scaling?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000043', 't1000000-0000-0000-0000-000000000004', 'd1000000-0000-0000-0000-000000000002',
   'Explain optimistic vs pessimistic locking. When would you use each in a concurrent application?', 'technical', true, CURRENT_TIMESTAMP, 'system');

-- ---- System Design / MEDIUM ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000044', 't1000000-0000-0000-0000-000000000005', 'd1000000-0000-0000-0000-000000000002',
   'Design a URL shortener service like bit.ly. What components would you need and how would you handle high traffic?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000045', 't1000000-0000-0000-0000-000000000005', 'd1000000-0000-0000-0000-000000000002',
   'What is the CAP theorem? Give an example of a system that chooses CP and one that chooses AP.', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000046', 't1000000-0000-0000-0000-000000000005', 'd1000000-0000-0000-0000-000000000002',
   'Explain the differences between REST, GraphQL, and gRPC. When would you choose each?', 'technical', true, CURRENT_TIMESTAMP, 'system');

-- ---- System Design / HARD ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000047', 't1000000-0000-0000-0000-000000000005', 'd1000000-0000-0000-0000-000000000003',
   'Design a notification system that can send millions of push/email/SMS messages per day. Walk through the architecture.', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000048', 't1000000-0000-0000-0000-000000000005', 'd1000000-0000-0000-0000-000000000003',
   'How would you design a globally distributed job scheduling system that guarantees at-least-once execution?', 'technical', true, CURRENT_TIMESTAMP, 'system');

-- ---- JavaScript / EASY ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000049', 't1000000-0000-0000-0000-000000000006', 'd1000000-0000-0000-0000-000000000001',
   'What is the difference between var, let, and const in JavaScript?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000050', 't1000000-0000-0000-0000-000000000006', 'd1000000-0000-0000-0000-000000000001',
   'Explain JavaScript''s event loop and what makes it single-threaded.', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000051', 't1000000-0000-0000-0000-000000000006', 'd1000000-0000-0000-0000-000000000001',
   'What is the difference between == and === in JavaScript?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000052', 't1000000-0000-0000-0000-000000000006', 'd1000000-0000-0000-0000-000000000001',
   'What is a closure in JavaScript? Give a practical example.', 'technical', true, CURRENT_TIMESTAMP, 'system');

-- ---- JavaScript / MEDIUM ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000053', 't1000000-0000-0000-0000-000000000006', 'd1000000-0000-0000-0000-000000000002',
   'Explain Promises and async/await in JavaScript. How do they differ from callbacks?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000054', 't1000000-0000-0000-0000-000000000006', 'd1000000-0000-0000-0000-000000000002',
   'What is the prototype chain in JavaScript? How does inheritance work compared to class-based languages?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000055', 't1000000-0000-0000-0000-000000000006', 'd1000000-0000-0000-0000-000000000002',
   'What are JavaScript generators? Show an example of lazy evaluation using a generator.', 'coding', true, CURRENT_TIMESTAMP, 'system');

-- ---- React / EASY ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000056', 't1000000-0000-0000-0000-000000000007', 'd1000000-0000-0000-0000-000000000001',
   'What is the Virtual DOM and how does React use it to improve performance?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000057', 't1000000-0000-0000-0000-000000000007', 'd1000000-0000-0000-0000-000000000001',
   'What is the difference between state and props in React?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000058', 't1000000-0000-0000-0000-000000000007', 'd1000000-0000-0000-0000-000000000001',
   'Explain the useState and useEffect hooks with examples.', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000059', 't1000000-0000-0000-0000-000000000007', 'd1000000-0000-0000-0000-000000000001',
   'What is the component lifecycle in React? What are the equivalent hooks for each lifecycle method?', 'technical', true, CURRENT_TIMESTAMP, 'system');

-- ---- React / MEDIUM ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000060', 't1000000-0000-0000-0000-000000000007', 'd1000000-0000-0000-0000-000000000002',
   'What is React Context API? When would you use it instead of a state management library like Redux?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000061', 't1000000-0000-0000-0000-000000000007', 'd1000000-0000-0000-0000-000000000002',
   'Explain React performance optimization techniques: useMemo, useCallback, and React.memo.', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000062', 't1000000-0000-0000-0000-000000000007', 'd1000000-0000-0000-0000-000000000002',
   'What is code splitting in React? How would you implement lazy loading with React.lazy and Suspense?', 'technical', true, CURRENT_TIMESTAMP, 'system');

-- ---- DevOps / EASY ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000063', 't1000000-0000-0000-0000-000000000008', 'd1000000-0000-0000-0000-000000000001',
   'What is Docker? What problem does containerization solve?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000064', 't1000000-0000-0000-0000-000000000008', 'd1000000-0000-0000-0000-000000000001',
   'What is the difference between a Docker image and a Docker container?', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000065', 't1000000-0000-0000-0000-000000000008', 'd1000000-0000-0000-0000-000000000001',
   'What is CI/CD? Describe the stages of a typical CI/CD pipeline.', 'technical', true, CURRENT_TIMESTAMP, 'system');

-- ---- DevOps / MEDIUM ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000066', 't1000000-0000-0000-0000-000000000008', 'd1000000-0000-0000-0000-000000000002',
   'What is Kubernetes? Explain the difference between a Pod, Deployment, and Service.', 'technical', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000067', 't1000000-0000-0000-0000-000000000008', 'd1000000-0000-0000-0000-000000000002',
   'Explain blue-green deployment vs canary deployment. What are the trade-offs?', 'technical', true, CURRENT_TIMESTAMP, 'system');

-- ---- Behavioral ----
INSERT INTO questions (id, topic_id, difficulty_id, question_text, question_type, is_active, created_at, created_by)
VALUES
  ('q1000000-0000-0000-0000-000000000068', 't1000000-0000-0000-0000-000000000009', 'd1000000-0000-0000-0000-000000000001',
   'Tell me about a time you had to debug a difficult production issue. How did you approach it?', 'behavioral', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000069', 't1000000-0000-0000-0000-000000000009', 'd1000000-0000-0000-0000-000000000001',
   'Describe a situation where you had to work with a difficult teammate. How did you handle it?', 'behavioral', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000070', 't1000000-0000-0000-0000-000000000009', 'd1000000-0000-0000-0000-000000000001',
   'Tell me about a project you are most proud of. What was your contribution and what did you learn?', 'behavioral', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000071', 't1000000-0000-0000-0000-000000000009', 'd1000000-0000-0000-0000-000000000001',
   'Describe a time you disagreed with a technical decision. How did you handle it?', 'behavioral', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000072', 't1000000-0000-0000-0000-000000000009', 'd1000000-0000-0000-0000-000000000002',
   'Tell me about a time you had to learn a new technology quickly under deadline. What was your approach?', 'behavioral', true, CURRENT_TIMESTAMP, 'system'),
  ('q1000000-0000-0000-0000-000000000073', 't1000000-0000-0000-0000-000000000009', 'd1000000-0000-0000-0000-000000000002',
   'How do you prioritize tasks when everything seems urgent? Give a concrete example.', 'behavioral', true, CURRENT_TIMESTAMP, 'system')
ON CONFLICT DO NOTHING;
