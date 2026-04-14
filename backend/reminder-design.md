# Système de rappels – V1 backend

## 1) Règle métier couverte

- L’utilisateur peut définir une temporalité sur :
  - une tâche (`/tasks/{taskId}/reminders`),
  - une affectation journalière (`/assignments/{assignmentId}/reminders`).
- Si une temporalité existe, le backend planifie un rappel et le déclenche quand il devient dû.

## 2) Modèle de données V1

- Entité : `ReminderEntity`
  - `task` (obligatoire)
  - `dailyAssignment` (optionnel)
  - `user` (obligatoire)
  - `type` (`DUE_SOON`, `DUE_NOW`, `OVERDUE`, `CUSTOM`)
  - `triggerMode` (`RELATIVE_DUE` / `ABSOLUTE_DATETIME`)
  - `minutesBeforeDue` (mode relatif)
  - `triggerAt` (instant calculé / fourni)
  - `channel` (`IN_APP`, `PUSH`, `EMAIL`)
  - `status` (`SCHEDULED`, `SENT`, `FAILED`, `CANCELED`)
  - `attemptCount`, `lastAttemptAt`, timestamps

## 3) Planification et détection

- V1 utilise un job backend simple (`@Scheduled`) déclenché toutes les 60s (configurable).
- Le job charge les rappels `SCHEDULED` avec `triggerAt <= now`, puis :
  1. marque `SENT`,
  2. incrémente `attemptCount`,
  3. renseigne `lastAttemptAt`.

## 4) Endpoints REST V1

- `POST /api/v1/tasks/{taskId}/reminders`
- `POST /api/v1/assignments/{assignmentId}/reminders`
- `GET /api/v1/reminders?userId=...`
- `DELETE /api/v1/reminders/{reminderId}`
- `POST /api/v1/reminders/dispatch` (trigger manuel pour ops/debug)

## 5) Limites de la V1

- Pas de vraie file de messages (traitement en polling DB).
- Pas de provider de notification réel (email/push) : statut envoyé simulé.
- Pas de retry exponentiel ni dead-letter queue.
- Pas de verrou distribué (si plusieurs instances backend).
- Pas de déduplication avancée multi-rappels.

## 6) Pistes d’amélioration

1. Remplacer le scheduler local par une queue + workers (RabbitMQ/Kafka).
2. Ajouter retry/backoff + DLQ + observabilité métier (métriques d’envoi).
3. Intégrer des canaux réels (FCM, APNs, SMTP provider) avec adaptateurs.
4. Ajouter idempotence et verrou distribué pour multi-instance.
5. Ajouter préférences utilisateur (silence window, fuseau avancé, digest).
