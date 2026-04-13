# Architecture complète – Application Todo List (mobile-first)

## 1) Vision d’ensemble

Objectif : construire une application de gestion de tâches robuste et évolutive, optimisée mobile, avec rappels et synchronisation agenda.

**Stack imposée**
- Frontend : Vue 3 + TypeScript + Vuetify
- Backend : Java 22 + Spring Boot
- Base de données : PostgreSQL
- Capacités spécifiques : notifications de rappel + intégration agenda

**Style d’architecture recommandé**
- **Frontend** : architecture modulaire par feature (Feature-Sliced + couches transverses).
- **Backend** : **modular monolith** au départ (DDD léger + Clean Architecture), prêt à extraire des services plus tard.
- **Communication** : API REST JSON + Webhooks agenda + scheduler interne.

---

## 2) Architecture frontend (Vue 3 + TS + Vuetify)

## 2.1 Principes
- Mobile-first réel : composants denses, navigation bas écran, interactions tactiles.
- Séparation stricte : UI / état client / accès API / règles métier front.
- Feature-based pour éviter un dossier `components/` géant non maintenable.

## 2.2 Structure recommandée

```txt
frontend/
  src/
    app/
      main.ts
      router/
        index.ts
        guards.ts
      providers/
        vuetify.ts
        i18n.ts
    shared/
      ui/                     # composants UI réutilisables (buttons, cards, dialogs)
      composables/            # hooks transverses (useDebounce, useViewport)
      utils/                  # helpers purs
      types/                  # types globaux
      constants/
    entities/
      task/
        model/
          task.types.ts
          task.schema.ts
      project/
      user/
      reminder/
      calendar/
    features/
      auth/
        api/
        model/
        ui/
      task-create/
      task-edit/
      task-filter/
      task-status-change/
      reminder-config/
      calendar-connect/
    widgets/
      task-list/
      task-board/
      dashboard-summary/
      notification-center/
    pages/
      auth/
        LoginPage.vue
        RegisterPage.vue
      tasks/
        TaskListPage.vue
        TaskDetailPage.vue
      projects/
      settings/
    processes/
      session-refresh/
      deep-link-handler/
    api/
      httpClient.ts           # Axios instance, interceptors, retry policy
      endpoints/
    stores/
      auth.store.ts           # Pinia
      task.store.ts
      notification.store.ts
    styles/
      tokens.scss
      globals.scss
```

## 2.3 État et cache
- **Pinia** pour état applicatif (session, notifications, préférences UI).
- **TanStack Query for Vue** (ou équivalent) pour cache serveur, invalidation, synchronisation API.
- Pattern recommandé :
  - `query` pour lecture (liste tâches, détails).
  - `mutation` pour création/édition/changement statut.
  - invalidation ciblée (`tasks:list`, `tasks:detail:{id}`).

## 2.4 Navigation mobile-first
- Router avec guards :
  - `requiresAuth`
  - `requiresWorkspace`
- Navigation principale mobile : bottom navigation (Tâches / Agenda / Notifications / Profil).
- Actions rapides : FAB (ajout tâche) + drawer contextuel.

## 2.5 Responsabilités frontend
- Validation formulaire de premier niveau (UX).
- Gestion optimiste sur certaines actions (ex : checkbox “done”).
- Affichage localisé des dates / fuseaux.
- Fallback offline léger (lecture cache + file d’attente mutation si nécessaire en phase 2).

---

## 3) Architecture backend (Java 22 + Spring Boot)

## 3.1 Choix architectural
- **Modular Monolith** avec modules métier explicites.
- Chaque module suit un découpage :
  - `domain` (règles métier pures)
  - `application` (use cases)
  - `infrastructure` (JPA, clients externes)
  - `api` (controllers REST)

Permet d’éviter la complexité microservices trop tôt, tout en préparant une extraction future (notifications/agenda).

## 3.2 Structure backend recommandée

```txt
backend/
  src/main/java/com/acme/todo/
    TodoApplication.java

    common/
      config/                # sécurité, jackson, openapi, scheduling
      exception/             # erreurs techniques/fonctionnelles
      security/              # JWT, filters, user principal
      util/

    modules/
      auth/
        domain/
        application/
        infrastructure/
        api/

      workspace/
        domain/
        application/
        infrastructure/
        api/

      project/
        domain/
        application/
        infrastructure/
        api/

      task/
        domain/
          model/
          service/
          event/
        application/
          usecase/
          dto/
          mapper/
        infrastructure/
          persistence/
          messaging/
        api/
          rest/

      reminder/
        domain/
        application/
        infrastructure/
        api/

      notification/
        domain/
        application/
        infrastructure/
          push/
          email/
          inapp/
        api/

      calendar/
        domain/
        application/
        infrastructure/
          google/
          microsoft/
        api/
```

## 3.3 Couches et responsabilités

### Domain
- Entités métier (`Task`, `ReminderRule`, `CalendarLink`, etc.).
- Value Objects (`TaskStatus`, `Priority`, `DueDate`, `Timezone`).
- Services métier purs (sans Spring ni JPA).
- Règles invariantes (ex : impossible de planifier un rappel après échéance + tolérance).

### Application
- Orchestration des cas d’usage (`CreateTaskUseCase`, `ScheduleReminderUseCase`, `SyncCalendarEventUseCase`).
- Transactions métier.
- Publication d’événements applicatifs.
- DTO de commande/réponse.

### Infrastructure
- Implémentation des repositories (Spring Data JPA).
- Adaptateurs externes (Google Calendar API, Microsoft Graph).
- Scheduler/queue adapter pour rappels.
- Providers de notifications (FCM/WebPush/email).

### API
- REST controllers.
- Validation d’entrée (`jakarta.validation`).
- Mapping erreurs métier → HTTP.
- OpenAPI docs.

---

## 4) Structure des modules métier

## 4.1 Module `task`
- CRUD tâche.
- Changement de statut.
- Attribution / priorité / échéance.
- Publication d’événements :
  - `TaskCreated`
  - `TaskUpdated`
  - `TaskDueDateChanged`
  - `TaskCompleted`

## 4.2 Module `reminder`
- Gestion des règles de rappel :
  - relative à l’échéance (ex : -1j, -1h)
  - absolue (date/heure spécifique)
- Calcul des prochaines occurrences.
- Gestion timezone utilisateur.
- Création de jobs planifiés.

## 4.3 Module `notification`
- Canaux : in-app (MVP), push mobile/web (phase 2), email (option).
- Déduplication (éviter spam multi-rappels).
- Tracking état envoi (`PENDING`, `SENT`, `FAILED`).

## 4.4 Module `calendar`
- Connexion OAuth2 agenda externe (Google, Microsoft).
- Synchronisation bidirectionnelle maîtrisée :
  - création / update événement agenda depuis tâche
  - import optionnel d’événements bloquants
- Gestion webhook + stratégie de resynchronisation périodique.

---

## 5) Base de données PostgreSQL

## 5.1 Tables principales
- `users`
- `workspaces`
- `workspace_members`
- `projects`
- `tasks`
- `task_comments`
- `reminder_rules`
- `reminder_jobs`
- `notifications`
- `calendar_accounts`
- `calendar_events`
- `outbox_events`

## 5.2 Choix techniques DB
- UUID v7 recommandé pour PK (ordre temporel + distribution).
- Index critiques :
  - `tasks(project_id, status, due_at)`
  - `tasks(assignee_id, status)`
  - `reminder_jobs(next_trigger_at, status)`
  - `notifications(user_id, is_read, created_at desc)`
- Soft delete sur tâches/projets.
- `created_at`, `updated_at` en UTC.

---

## 6) Rappels et notifications : design concret

## 6.1 Scheduler
- Utiliser **Spring Scheduler** pour MVP (polling `reminder_jobs` toutes les 30-60 sec).
- Pour montée en charge : migrer vers queue (RabbitMQ/Kafka + worker dédié).

## 6.2 Flux reminder
1. Création/modification tâche avec échéance.
2. `ReminderRule` génère un ou plusieurs `reminder_jobs`.
3. Scheduler détecte jobs “due”.
4. Appel service `notification`.
5. Marquage job `SENT` ou `FAILED` + retry exponentiel.

## 6.3 Politique de retry
- `max_attempts = 5`
- backoff : 30s, 2m, 10m, 30m, 2h
- dead-letter logique en DB pour investigation.

---

## 7) Intégration agenda

## 7.1 Stratégie
- MVP : **unidirectionnelle** (todo → agenda) pour réduire ambiguïtés.
- Phase 2 : bidirectionnelle partielle avec règles anti-conflit.

## 7.2 Gestion OAuth2
- Stocker tokens chiffrés (at rest).
- Rafraîchir access token automatiquement.
- Révocation depuis paramètres utilisateur.

## 7.3 Mapping métier
- Une tâche peut référencer zéro ou un événement agenda externe.
- `calendar_events` garde : provider, external_event_id, sync_status, last_sync_at.
- En cas d’échec sync : statut `SYNC_ERROR` + bannière UI.

---

## 8) Choix techniques recommandés

## 8.1 Frontend
- Vue 3 + Composition API + `<script setup>`.
- TypeScript strict (`strict: true`).
- Vuetify 3 avec design tokens custom (densité mobile).
- Axios + interceptors + gestion uniforme d’erreurs.
- Vitest + Vue Testing Library + Playwright (E2E mobile viewport).

## 8.2 Backend
- Spring Boot 3.x, Java 22.
- Spring Web, Spring Security, Spring Data JPA, Validation.
- Flyway pour migrations SQL versionnées.
- MapStruct pour mapping DTO <-> domain.
- Testcontainers pour tests d’intégration PostgreSQL.

## 8.3 Observabilité
- Logs JSON (correlationId).
- Micrometer + Prometheus + Grafana.
- Traces OpenTelemetry (API et jobs reminders).
- Alertes : taux d’échec reminder, latence API p95, erreurs sync agenda.

---

## 9) Patterns à utiliser

1. **Clean Architecture / Hexagonal (adaptée)**
   - Domaine indépendant des frameworks.

2. **CQRS léger**
   - Séparer handlers de lecture/écriture dans `application` si besoin de perf.

3. **Outbox Pattern**
   - Fiabiliser publication d’événements (notifications/sync agenda) après transaction DB.

4. **Saga simple (orchestration locale)**
   - Pour enchaîner création tâche + sync agenda + rappel sans incohérence silencieuse.

5. **Specification Pattern**
   - Filtrage dynamique des tâches (statut, tags, assignee, due date).

6. **Retry + Circuit Breaker**
   - Résilience appels providers agenda/notification.

7. **Idempotency Key**
   - Endpoints sensibles (création invitation, sync event).

---

## 10) Schéma textuel des flux

```txt
[Mobile Web App (Vue3/Vuetify)]
        |
        | HTTPS REST (JWT)
        v
[Spring Boot API]
   |        |                |
   |        |                +--> [Calendar Adapter]
   |        |                       | OAuth2 / API calls
   |        |                       v
   |        |                 [Google/Microsoft Calendar]
   |        |
   |        +--> [Reminder Scheduler/Worker]
   |                   |
   |                   v
   |             [Notification Service]
   |                   |
   |                   +--> In-App notifications (DB)
   |                   +--> Push/Email provider (option)
   |
   +--> [PostgreSQL]
          |- tasks/projects/users
          |- reminder_jobs/reminder_rules
          |- calendar_accounts/calendar_events
          |- notifications
          |- outbox_events
```

**Flux principal création tâche + rappel + agenda**
1. Front envoie `POST /tasks` avec `dueAt`, `reminderRules`, `syncToCalendar=true`.
2. Backend crée tâche + règles rappel en transaction.
3. Backend écrit événement en `outbox_events`.
4. Processor outbox déclenche :
   - création jobs de rappel
   - création/mise à jour événement agenda.
5. Scheduler exécute jobs de rappel à échéance.
6. Notification créée (in-app) et poussée canal secondaire si activé.
7. Front lit notifications / état sync agenda via API.

---

## 11) Plan de mise en œuvre réaliste

## Phase 1 (MVP)
- Auth + workspaces + projets + tâches + vue mobile.
- Reminder in-app via scheduler DB.
- Intégration agenda unidirectionnelle (Google d’abord).
- Observabilité minimale + tests intégration.

## Phase 2
- Push notifications.
- Microsoft Calendar.
- Sync bidirectionnelle partielle.
- Optimisations perf (cache requêtes, index additionnels).

## Phase 3
- Offline-first partiel.
- Extraction service reminders si charge élevée.
- Reporting avancé.

---

## 12) Décisions à verrouiller avant build

- Niveau de synchronisation agenda (uni vs bi-directionnelle).
- Canaux de notification MVP exacts (in-app only vs email).
- Politique de conflit calendrier (source of truth).
- Volumétrie cible (utilisateurs actifs/jour, jobs reminders/minute).
- Contraintes sécurité/compliance (chiffrement, audit, retention).
