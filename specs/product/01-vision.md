# Product Vision

## Context
A group of local restaurants decided to build a shared management
system instead of each one buying its own. Individual systems were
expensive, and the quality of each system was influencing customers'
restaurant choice — which makes no sense. With a single shared
system, customers go back to choosing restaurants by the food.

## Problem
Restaurants need to manage their operations, and customers need to
browse information, leave reviews and place online orders — all of
this in a single, robust system, delivered incrementally in phases.

## Scope of this delivery (Phase 1)
Backend for **user management** only. Nothing else.
Out of scope: menu, orders, reviews, payment. Those come in later
phases.

## Users
- **Restaurant Owner**: registers, manages own data, logs in.
- **Customer**: registers, manages own data, logs in.
- **Admin**: full access to all operations.

All user types share the same mandatory fields; what changes is the
role, which will gate permissions in later phases.

## Assumptions
- Backend-only in this phase (no frontend).
- No external integrations (no payment gateway, email, etc.).
- Simple authentication via dedicated login/password endpoint
  (Spring Security / JWT is optional and out of scope for Phase 1).

## Non-goals
- No complex administrative profiles beyond the Admin role.
- No password recovery via email.
- No fine-grained authorization rules.