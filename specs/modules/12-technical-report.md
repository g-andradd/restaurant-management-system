# M12 — Technical Report (Final Deliverable)

## Goal
Produce the technical report PDF that is the OFFICIAL deliverable
required by the challenge. Everything else in the repository
supports this PDF.

## Scope
- `report/technical-report.md` — source in markdown.
- `report/technical-report.pdf` — generated final.
- `report/diagrams/` — ER diagram and architecture diagram (PNG).
- Screenshots of Swagger UI and Postman runner stored in
  `report/screenshots/`.

## Required sections (per PDF page 5-6)
1. **Cover** — project name, author (Mauricio Borges Florencio),
   course (FIAP Pós ADJ), phase, date.
2. **Architecture description** — synthesizes
   `specs/technical/01-architecture.md` plus a diagram showing
   layers (domain / application / infrastructure) and the request
   flow Controller → UseCase → Port → Adapter.
3. **Entity & relationship modeling** — ER diagram for the `users`
   table, with column types and constraints. Reference the V1
   migration.
4. **Endpoints catalogue** — for each of the seven endpoints:
   verb, path, brief description, request example, success response,
   error responses (with ProblemDetail body).
5. **Swagger documentation** — 2-3 screenshots of `/swagger-ui.html`
   showing the operations list and at least one expanded endpoint
   with examples.
6. **Postman collection** — overview of folders, screenshots of the
   collection runner output (all green), explanation of the
   environment variables.
7. **Database structure** — table definitions, indexes, and how
   Flyway manages migrations.
8. **Docker Compose run guide** — step-by-step from clone to running
   API: prerequisites, env vars table, commands, expected logs,
   troubleshooting (port in use, postgres healthcheck).
9. **Appendix** — links to: GitHub repo, Swagger URL pattern, ADRs.

## Diagrams to produce
- **Architecture diagram**: hexagonal layers with arrows showing
  dependency direction. Tool: draw.io / Mermaid / PlantUML, exported
  as PNG.
- **ER diagram**: a single `users` table with all columns and types.
  Even with one table, a diagram is required by the PDF.
- **Sequence diagram (optional but valuable)**: registration flow
  HTTP → Controller → Service → Port → Adapter → DB.

## Production rules
- Markdown is the source; PDF is generated (Pandoc, Typora, or
  similar). Both are committed.
- Screenshots use realistic Brazilian data, not lorem ipsum.
- No password fields visible anywhere in screenshots.
- All examples in the report match exactly what the running app
  returns — copy from real responses, do not invent.

## Definition of done
- [ ] All nine sections present.
- [ ] Architecture and ER diagrams included.
- [ ] At least 2 Swagger screenshots and 1 Postman runner screenshot.
- [ ] PDF generated and committed under `report/`.
- [ ] Step-by-step from `git clone` to working API verified by a
  friend or by yourself on a clean machine.
- [ ] Commit: `docs(M12): technical report`.