# CLAUDE.md — Permanent instructions for Claude Code

## Source of truth
Before generating any code, read these in order:
1. `/specs/product/`     — what and why
2. `/specs/technical/`   — how (architecture and conventions)
3. `/specs/modules/`     — details of the requested module

## Golden rules
- NEVER invent requirements. If a spec is ambiguous, stop and ask.
- NEVER mix code from different modules in the same commit.
- Always follow the conventions in `/specs/technical/03-conventions.md`.
- Always generate the tests defined in the module's "Definition of done".
- If a new technical decision is required, create an ADR under
  `/specs/technical/decisions/`.

## Project context
Tech Challenge — Phase 1 — FIAP Postgraduate in Java Architecture.
Shared management backend for a group of restaurants.
Mandatory stack: Spring Boot + relational database + Docker Compose.

## Architecture quick-reference
- Style: Pragmatic Hexagonal, single Maven module
- Dependency direction: infrastructure → application → domain
- Domain layer has ZERO framework dependencies
- All cross-layer communication via ports (interfaces)
- Authentication is a Strategy: new auth method = new adapter
- ArchUnit test enforces all the above — keep it green

## Language convention
- Code, comments, identifiers, commit messages, specs → English
- API user-facing messages (validation, errors) → Portuguese