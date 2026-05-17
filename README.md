# APAP Backend

APAP(Abnormal Pattern Alarmer Platform) backend base project.

## Stacks

This repository contains two backend implementations. Use the one that fits your team's choice.

### Python (FastAPI) — `app/`

- FastAPI
- SQLAlchemy
- SQLite for local development
- JWT auth

**Run:**

```powershell
python -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --reload
```

API docs: http://127.0.0.1:8000/docs · http://127.0.0.1:8000/redoc

**Test:**

```powershell
pytest
```

### Java (Spring Boot) — `backend/`

- Spring Boot 3
- JPA / Hibernate
- JWT auth

**Run:**

```powershell
cd backend
./mvnw spring-boot:run
```

## Project Structure

```text
app/                  # FastAPI implementation
  main.py
  core/
  db/
  models/
  routers/
  schemas/
  services/
backend/              # Spring Boot implementation
  src/main/java/com/apap/backend/
  src/main/resources/application.yml
```

## MVP Scope

- User auth
- Scenario CRUD
- Video source metadata
- AI analysis job request/callback
- Detection event storage
- Alert list/read state
- Dashboard summary
