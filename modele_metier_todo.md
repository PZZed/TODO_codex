# Modèle métier complet – Application Todo

Ce document définit un **modèle métier de référence** prêt à être utilisé pour le développement (backend Spring Boot + PostgreSQL, frontend Vue).

---

## 1) Principes métier structurants

- Un **utilisateur** possède une ou plusieurs **listes de tâches**.
- Une **tâche** peut être :
  - simple (ponctuelle),
  - récurrente (génère des occurrences selon une règle).
- Une **affectation journalière** permet de planifier explicitement une tâche sur un jour donné (agenda personnel/quotidien).
- Un **rappel** est un mécanisme de notification associé à une tâche (ou occurrence).
- Un **lien agenda** synchronise un élément métier avec un provider externe (Google/Microsoft).

---

## 2) Entités métier

## 2.1 Utilisateur (`User`)

### Attributs
- `id: UUID`
- `email: String` (unique)
- `passwordHash: String` (nullable si SSO-only)
- `displayName: String`
- `timezone: String` (ex: `Europe/Paris`)
- `locale: String` (ex: `fr-FR`)
- `isActive: Boolean`
- `createdAt: Instant`
- `updatedAt: Instant`
- `lastLoginAt: Instant?`

### Rôle métier
- Porteur de compte applicatif.
- Propriétaire de listes, de tâches et de préférences de notification/fuseau.

### Relations
- 1 → N avec `TaskList` (owner).
- 1 → N avec `Task` (creator, éventuellement assignee selon stratégie).
- 1 → N avec `DailyTaskAssignment`.
- 1 → N avec `CalendarLink`.

### Contraintes métier
- `email` unique et vérifié avant usage complet.
- `timezone` obligatoire pour calcul de rappels et récurrences.
- Compte inactif = pas de création/modification métier.

---

## 2.2 Liste de tâches (`TaskList`)

### Attributs
- `id: UUID`
- `ownerUserId: UUID`
- `name: String` (1..120)
- `color: String?` (token/UI)
- `isArchived: Boolean`
- `position: Int` (ordre d’affichage)
- `createdAt: Instant`
- `updatedAt: Instant`

### Rôle métier
- Regrouper les tâches selon un contexte (perso, travail, courses, etc.).

### Relations
- N → 1 avec `User`.
- 1 → N avec `Task`.

### Contraintes métier
- Nom unique par utilisateur (au minimum sur listes actives).
- Une liste archivée n’accepte plus de nouvelles tâches.
- Une tâche appartient à une et une seule liste.

---

## 2.3 Tâche (`Task`)

### Attributs
- `id: UUID`
- `taskListId: UUID`
- `createdByUserId: UUID`
- `title: String` (1..255)
- `description: String?`
- `status: TaskStatus`
- `priority: TaskPriority`
- `dueAt: Instant?` (échéance globale)
- `startAt: Instant?`
- `completedAt: Instant?`
- `isAllDay: Boolean`
- `sourceType: TaskSourceType` (manual, imported, generated)
- `deletedAt: Instant?` (soft delete)
- `createdAt: Instant`
- `updatedAt: Instant`

### Rôle métier
- Représenter une action à effectuer, suivie par statut et échéance.

### Relations
- N → 1 avec `TaskList`.
- N → 1 avec `User` (créateur).
- 1 → 0..1 avec `RecurringTaskRule` (si récurrente).
- 1 → N avec `Reminder`.
- 1 → N avec `DailyTaskAssignment`.
- 1 → N avec `CalendarLink` (ou 1→1 selon stratégie).

### Contraintes métier
- `title` obligatoire.
- `completedAt` requis si `status = DONE`.
- `status = DONE` interdit si tâche supprimée/archivée.
- `dueAt >= startAt` si les deux existent.
- Une tâche simple ne possède pas de règle de récurrence active.

---

## 2.4 Tâche récurrente (`RecurringTaskRule`)

> Entité de règle de récurrence, attachée à une tâche “template”.

### Attributs
- `id: UUID`
- `taskId: UUID` (unique, 1:1 avec tâche template)
- `frequency: RecurrenceFrequency`
- `intervalValue: Int` (>=1, ex: toutes les 2 semaines)
- `daysOfWeek: Set<DayOfWeek>?` (hebdomadaire)
- `dayOfMonth: Int?` (mensuelle)
- `monthOfYear: Int?` (annuelle)
- `startDate: LocalDate`
- `endDate: LocalDate?`
- `maxOccurrences: Int?`
- `nextOccurrenceDate: LocalDate?`
- `timezone: String`
- `isActive: Boolean`
- `createdAt: Instant`
- `updatedAt: Instant`

### Rôle métier
- Définir la logique de génération des occurrences futures d’une tâche.

### Relations
- 1 → 1 avec `Task` (tâche template).
- 1 → N logique avec occurrences (implémentées via `DailyTaskAssignment` et/ou tâches générées).

### Contraintes métier
- Une seule règle active par tâche template.
- `startDate <= endDate` si `endDate` renseignée.
- `maxOccurrences > 0` si renseigné.
- Cohérence frequency/champs :
  - WEEKLY ⇒ `daysOfWeek` non vide.
  - MONTHLY ⇒ `dayOfMonth` requis.
  - YEARLY ⇒ `dayOfMonth` + `monthOfYear` requis.
- Fuseau obligatoire pour éviter les décalages DST.

---

## 2.5 Affectation d’une tâche à une journée (`DailyTaskAssignment`)

### Attributs
- `id: UUID`
- `taskId: UUID`
- `userId: UUID`
- `assignmentDate: LocalDate`
- `plannedStartTime: LocalTime?`
- `plannedEndTime: LocalTime?`
- `origin: AssignmentOrigin` (manual, recurrence, calendar_sync)
- `statusOnDay: DailyAssignmentStatus` (planned, done, skipped)
- `note: String?`
- `createdAt: Instant`
- `updatedAt: Instant`

### Rôle métier
- Matérialiser le plan quotidien d’exécution d’une tâche (notamment utile pour les récurrentes).

### Relations
- N → 1 avec `Task`.
- N → 1 avec `User`.
- 1 → N avec `Reminder` (rappels ciblant une journée précise, optionnel selon implémentation).

### Contraintes métier
- Unicité `(taskId, userId, assignmentDate)` pour éviter doublons.
- `plannedEndTime >= plannedStartTime` si les deux existent.
- `statusOnDay = DONE` seulement si tâche non supprimée.
- Si `origin = recurrence`, l’affectation doit être traçable à une règle active/historique.

---

## 2.6 Rappel (`Reminder`)

### Attributs
- `id: UUID`
- `taskId: UUID`
- `dailyAssignmentId: UUID?` (si rappel spécifique à un jour)
- `userId: UUID`
- `type: ReminderType`
- `triggerMode: ReminderTriggerMode` (relative_due, absolute_datetime)
- `minutesBeforeDue: Int?` (si relatif)
- `triggerAt: Instant?` (si absolu)
- `channel: ReminderChannel` (in_app, push, email)
- `status: ReminderStatus` (scheduled, sent, failed, canceled)
- `lastAttemptAt: Instant?`
- `attemptCount: Int`
- `createdAt: Instant`
- `updatedAt: Instant`

### Rôle métier
- Piloter l’envoi de notifications de rappel avant ou au moment prévu.

### Relations
- N → 1 avec `Task`.
- N → 0..1 avec `DailyTaskAssignment`.
- N → 1 avec `User`.

### Contraintes métier
- Exactement une stratégie de déclenchement :
  - relatif (`minutesBeforeDue`),
  - absolu (`triggerAt`).
- `attemptCount` incrémenté à chaque tentative d’envoi.
- Un rappel `sent` ou `canceled` n’est plus modifiable (hors audit/admin).

---

## 2.7 Lien avec agenda (`CalendarLink`)

### Attributs
- `id: UUID`
- `userId: UUID`
- `taskId: UUID?`
- `dailyAssignmentId: UUID?`
- `provider: CalendarProvider` (google, microsoft, apple)
- `calendarId: String`
- `externalEventId: String`
- `syncDirection: SyncDirection` (todo_to_calendar, bidirectional)
- `syncStatus: CalendarSyncStatus` (linked, pending, sync_error, unlinked)
- `lastSyncedAt: Instant?`
- `etag: String?`
- `createdAt: Instant`
- `updatedAt: Instant`

### Rôle métier
- Porter la correspondance entre un objet interne et un événement agenda externe.

### Relations
- N → 1 avec `User`.
- N → 0..1 avec `Task`.
- N → 0..1 avec `DailyTaskAssignment`.

### Contraintes métier
- Au moins une cible interne obligatoire (`taskId` XOR `dailyAssignmentId` recommandé).
- Unicité provider + externalEventId + userId.
- `syncStatus=sync_error` doit conserver un diagnostic en journal technique.

---

## 3) Différences métier : tâche simple vs tâche récurrente

## 3.1 Tâche simple
- Une occurrence métier unique.
- Échéance éventuellement ponctuelle (`dueAt`).
- Rappels calculés directement sur la tâche.
- Pas de génération automatique de journées futures.

## 3.2 Tâche récurrente
- Une tâche template + une règle (`RecurringTaskRule`).
- Génère des occurrences (souvent via `DailyTaskAssignment`, éventuellement snapshots de tâche).
- Peut produire plusieurs rappels par occurrence.
- Nécessite la gestion de fuseau, DST, fin de récurrence, et idempotence de génération.

## 3.3 Impacts techniques
- Moteur de planification requis pour calculer les prochaines occurrences.
- Besoin de mécanisme anti-duplication lors des recalculs.
- Historique des occurrences recommandé pour audit/UX.

---

## 4) Enums nécessaires

## 4.1 Statut de tâche (`TaskStatus`)
- `TODO`
- `IN_PROGRESS`
- `BLOCKED`
- `DONE`
- `CANCELED`

## 4.2 Priorité (`TaskPriority`)
- `LOW`
- `MEDIUM`
- `HIGH`
- `URGENT`

## 4.3 Fréquence de récurrence (`RecurrenceFrequency`)
- `DAILY`
- `WEEKLY`
- `MONTHLY`
- `YEARLY`

## 4.4 Type de rappel (`ReminderType`)
- `DUE_SOON`
- `DUE_NOW`
- `OVERDUE`
- `CUSTOM`

## 4.5 Mode de déclenchement rappel (`ReminderTriggerMode`)
- `RELATIVE_DUE`
- `ABSOLUTE_DATETIME`

## 4.6 Canal de rappel (`ReminderChannel`)
- `IN_APP`
- `PUSH`
- `EMAIL`

## 4.7 Statut de rappel (`ReminderStatus`)
- `SCHEDULED`
- `SENT`
- `FAILED`
- `CANCELED`

## 4.8 Origine d’affectation journalière (`AssignmentOrigin`)
- `MANUAL`
- `RECURRENCE`
- `CALENDAR_SYNC`

## 4.9 Statut journalier (`DailyAssignmentStatus`)
- `PLANNED`
- `DONE`
- `SKIPPED`

## 4.10 Provider agenda (`CalendarProvider`)
- `GOOGLE`
- `MICROSOFT`
- `APPLE`

## 4.11 Sens de synchronisation (`SyncDirection`)
- `TODO_TO_CALENDAR`
- `BIDIRECTIONAL`

## 4.12 Statut de synchro agenda (`CalendarSyncStatus`)
- `PENDING`
- `LINKED`
- `SYNC_ERROR`
- `UNLINKED`

## 4.13 Source de tâche (`TaskSourceType`)
- `MANUAL`
- `IMPORTED`
- `GENERATED`

---

## 5) Schéma relationnel métier (texte)

```txt
User 1---N TaskList
User 1---N Task (createdBy)
TaskList 1---N Task

Task 1---0..1 RecurringTaskRule
Task 1---N DailyTaskAssignment
Task 1---N Reminder
Task 1---N CalendarLink

User 1---N DailyTaskAssignment
User 1---N Reminder
User 1---N CalendarLink

DailyTaskAssignment 1---N Reminder (optionnel)
DailyTaskAssignment 1---N CalendarLink (optionnel)
```

---

## 6) Règles transverses recommandées

- Tous les timestamps persistés en UTC.
- Toutes les dates de planification calculées en timezone utilisateur.
- Soft delete sur `Task` (et éventuellement `TaskList`).
- Idempotence sur génération récurrente + sync agenda.
- Audit minimal recommandé : création, changement statut, annulation rappel, erreur sync.

---

## 7) Base de développement (contrats)

Pour démarrer l’implémentation proprement :
- Créer les `enum` backend dès le départ (Java) + miroirs TS côté frontend.
- Créer des validateurs métier dédiés (`RecurringRuleValidator`, `ReminderValidator`).
- Poser des contraintes SQL (`unique`, `check`) cohérentes avec les règles ci-dessus.
- Exposer les objets via DTO API explicitement versionnés (`/api/v1`).

Ce modèle constitue un **socle stable** pour la conception des migrations DB, des APIs et des écrans.
