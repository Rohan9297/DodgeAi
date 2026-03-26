# DodgeAi


# Dodge AI - Order to Cash Context Graph

Context graph system with an LLM-powered query interface for Order-to-Cash data (Sales Orders -> Deliveries -> Billing -> Payments/Journal Entries).

## Live Demo
- Frontend (Vercel): https://dodge-jtn0i742v-rohankolekar66-3542s-projects.vercel.app
- Backend (Render): https://dodgeai-backend-xufw.onrender.com

## Repository
- GitHub: https://github.com/Rohan9297/DodgeAi

## Problem Scope
This project unifies fragmented business data into a graph and enables natural-language exploration.

It supports:
- Graph construction from provided dataset
- Graph visualization for entity/relationship exploration
- Conversational query interface
- Natural language -> SQL translation -> data-backed answers
- Guardrails for unrelated prompts

## Architecture Decisions
- Frontend: React + Vite + Cytoscape (`dodge-ai-frontend`)
- Backend: Spring Boot + JPA + H2 + custom graph service (`backend`)
- LLM Integration: Gemini API for:
  - SQL generation from user question
  - Natural-language answer formatting from SQL results
  - Fallback relevance classification

### Why this architecture
- Spring Boot allows fast API development and clean service layering.
- H2 provides simple in-memory analytics for assignment scale and quick startup.
- Cytoscape is optimized for interactive graph rendering and relationship exploration.
- Splitting graph APIs and query APIs keeps responsibilities clear.

## Data + Graph Modeling
Graph is built from dataset entities such as:
- `Customer` / `BusinessPartner`
- `SalesOrder` / `SalesOrderItem`
- `Delivery`
- `Billing`
- `Payment`
- `Product`

Relationships modeled include core Order-to-Cash flow links:
- Sales Order -> Delivery
- Sales Order -> Billing
- Billing -> Journal Entry / Payment references
- Sales Order Item -> Product

## Conversational Query Strategy
Pipeline:
1. Guardrail check (`isRelevantQuestion`)
2. SQL generation from question using schema-aware prompt
3. SQL execution on H2
4. Natural-language response generation from query output

### Prompting approach
- Explicit schema and relationships are provided to LLM.
- SQL output is constrained (`LIMIT`, formatting rules).
- Response generation is grounded on raw query result text.

## Guardrails
Guardrails are implemented in backend service logic:
- Fast keyword-based domain relevance checks
- Hard rejection for clearly unrelated topics
- Fallback LLM relevance check for ambiguous prompts

Example rejection message:
`This system is designed to answer questions related to the provided dataset only.`

## Functional Coverage vs Task
- Graph construction: Implemented
- Graph visualization: Implemented (pan/zoom, relationship visibility, node selection)
- Node metadata inspection: Implemented via node selection context in UI/chat
- Conversational querying: Implemented
- Natural language -> structured query translation: Implemented
- Data-backed answers: Implemented
- Guardrails: Implemented

Note:
- Current implementation preloads and renders the graph; it does not use lazy node-by-node expansion.

## Example Queries Supported
- Which products are associated with the highest number of billing documents?
- Trace the full flow of a given billing document (when ID/reference is provided).
- Identify sales orders that have broken or incomplete flows.

## Local Setup
### Prerequisites
- Java 17+
- Node.js 18+
- npm

### Backend
```bash
cd backend
./mvnw spring-boot:run
```

Required env vars:
- `GEMINI_API_KEY`
- `GEMINI_API_URL` 

### Frontend
bash
cd dodge-ai-frontend
npm install
npm run dev


Frontend env:
- `VITE_API_BASE_URL` (e.g. `http://localhost:8080`)

## Deployment
### Backend (Render)
- Service type: Web Service (Docker)
- Root directory: `backend`
- Dockerfile path: `Dockerfile`
- Env vars:
  - `GEMINI_API_KEY`
  - `GEMINI_API_URL`

### Frontend (Vercel)
- Root directory: `dodge-ai-frontend`
- Framework: Vite
- Env var:
  - `VITE_API_BASE_URL=https://<render-backend-url>`

## Security Notes
- API keys are not committed to source code.
- `application.properties` uses env var interpolation for secrets.
- If any key was exposed during development, rotate/revoke it.

## AI Coding Session Logs
Add your links here before submission:
- Codex/ChatGPT session log: `ADD_LINK_HERE`
- Claude logs: https://claude.ai/share/26dfdff8-078e-459a-9036-b3261a852323

## Future Improvements
- Explicit node expand/collapse interaction
- Highlight graph nodes referenced in chat answers
- Streaming responses in chat
- Query result table alongside natural-language explanation
