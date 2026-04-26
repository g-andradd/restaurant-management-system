# CLAUDE.md — Instruções permanentes para o Claude Code

## Fonte da verdade
Antes de gerar qualquer código, leia obrigatoriamente:
1. `/specs/product/` — o quê e por quê
2. `/specs/technical/` — como (arquitetura e convenções)
3. `/specs/modules/` — detalhes do módulo solicitado

## Regras de ouro
- NUNCA invente requisitos. Se a spec for ambígua, pare e pergunte.
- NUNCA misture código de módulos diferentes em um mesmo commit.
- Sempre siga as convenções de `/specs/technical/03-conventions.md`.
- Sempre gere os testes definidos no "Critério de pronto" do módulo.
- Se precisar de uma decisão técnica nova, crie um ADR em `/specs/technical/decisions/`.

## Contexto do projeto
Tech Challenge Fase 1 — FIAP Pós Arquitetura Java.
Sistema de gestão compartilhado para restaurantes.
Stack obrigatória: Spring Boot + banco relacional + Docker Compose.