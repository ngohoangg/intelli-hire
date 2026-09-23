# IntelliHire

Monorepo for the IntelliHire interview platform. Each service lives in its own folder.

| Folder | Stack | Port |
|---|---|---|
| `intelli-hire-auth` | Spring Boot 3.4, Java 21 | `SERVER_PORT` |
| `intelli-hire-question` | Spring Boot 3.2, Java 17 | 8080 |
| `intelli-hire-resume` | Spring Boot 3.2, Java 17 | 8080 |
| `intelli-hire-interviews` | Spring Boot 3.2, Java 17 | 8083 |
| `intelli-hire-grading` | Spring Boot 3.2, Java 17 | 8084 |
| `intelli-hire-report` | Spring Boot 3.2, Java 17 | 8085 |
| `intelli-hire-uiux` | Angular 19 | 4200 |
| `intelli-hire-eureka-service`, `intelli-hire-gateway` | placeholder | – |

## Team workflow

- Work only inside the service folder(s) you own.
- One branch per task (`feature/<service>-<short-name>`), open a PR into `main`.
- Never commit `.env` files. Copy `.env.sample` and fill in your own values.
