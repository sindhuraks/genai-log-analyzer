## LogGuardianAI - Overview
System logs are cumbersome to understand due to the large volume and lack of formatting making it difficult to find critical insights and anomalies (warnings or fatal errors that can cause a system to crash) present. To make the lives of Site Reliability Engineers (SRE) easy and prevent the same kind of issues occurring in the future, an AI-powered log analysis tool named LogGuardianAI has been built that ingests system logs, detects anomalies, explains the error and the root cause and recommends actions to be taken (using LLM APIs : Claude, OpenAI, Llama with temperature = 0.2). The responses are evaluated using LLM as a judge metric that evaluates the responses on its correctness, completenes, clarity , answer relevance, short reasoning and returns a score between 0-1. Also, provides a short insight on when the anomlay was seen first and whether its a newly detected one or recurrent.

- Goal: improve reliability, reduce downtime, and accelerate incident response
through GenAI-powered insights

## Tech Stack
### Dataset
- LogHub
### Database
- PostgreSQL
### LLM APIs
- claude-haiku-3, claude-sonnet-4-5
- gpt-4o-mini
- llama-chatqa-8b
### Backend
- Spring Boot
### Frontend
- React
- Nextjs