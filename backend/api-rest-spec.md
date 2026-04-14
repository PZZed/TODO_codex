# API REST complète – Todo Application (v1)

Base URL : `/api/v1`
Format : `application/json`
Auth : `Authorization: Bearer <JWT>` (sauf endpoints d’auth)

---

## 1) Conventions transverses

## 1.1 Format d’erreur standard

```json
{
  "timestamp": "2026-04-13T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation error",
  "details": ["title: must not be blank"],
  "path": "/api/v1/tasks"
}
```

## 1.2 Codes transverses
- `200 OK` : lecture / update réussie
- `201 Created` : création réussie
- `204 No Content` : suppression réussie
- `400 Bad Request` : validation/payload invalide
- `401 Unauthorized` : non authentifié
- `403 Forbidden` : interdit par droits
- `404 Not Found` : ressource introuvable
- `409 Conflict` : conflit métier (doublon / état invalide)
- `422 Unprocessable Entity` : règle métier violée
- `500 Internal Server Error` : erreur technique

## 1.3 Validations communes
- UUID valides sur tous les IDs.
- Dates/horaires au format ISO-8601.
- `timezone` au format IANA (ex: `Europe/Paris`).
- Pagination : `page >= 0`, `size in [1..200]`.

---

## 2) Listes de tâches

## 2.1 Créer une liste
- **HTTP** : `POST /task-lists`
- **Entrée**
```json
{
  "name": "Travail",
  "color": "#4F46E5",
  "position": 1
}
```
- **Sortie (201)**
```json
{
  "id": "uuid",
  "name": "Travail",
  "color": "#4F46E5",
  "position": 1,
  "archived": false,
  "createdAt": "2026-04-13T12:00:00Z",
  "updatedAt": "2026-04-13T12:00:00Z"
}
```
- **Validations** : `name` requis (1..120), `position >= 0`.
- **Erreurs métier** :
  - `409` nom déjà utilisé pour cet utilisateur.

## 2.2 Lister les listes
- **HTTP** : `GET /task-lists?archived=false`
- **Sortie (200)**
```json
[
  {
    "id": "uuid",
    "name": "Travail",
    "color": "#4F46E5",
    "position": 1,
    "archived": false,
    "createdAt": "...",
    "updatedAt": "..."
  }
]
```
- **Erreurs métier** : aucune spécifique.

## 2.3 Consulter une liste
- **HTTP** : `GET /task-lists/{taskListId}`
- **Sortie (200)** : même structure que ci-dessus.
- **Erreurs métier** : `404` liste inexistante.

## 2.4 Modifier une liste
- **HTTP** : `PATCH /task-lists/{taskListId}`
- **Entrée**
```json
{
  "name": "Travail 2026",
  "color": "#1D4ED8",
  "position": 2,
  "archived": false
}
```
- **Sortie (200)** : objet liste mis à jour.
- **Validations** : champs optionnels mais valides si présents.
- **Erreurs métier** : `409` nom déjà utilisé.

## 2.5 Supprimer une liste (soft delete)
- **HTTP** : `DELETE /task-lists/{taskListId}`
- **Sortie** : `204 No Content`
- **Erreurs métier** :
  - `404` liste inexistante,
  - `409` liste contient des tâches actives si politique blocage.

---

## 3) Tâches (CRUD)

## 3.1 Créer une tâche
- **HTTP** : `POST /tasks`
- **Entrée**
```json
{
  "taskListId": "uuid",
  "title": "Préparer la démo",
  "description": "Slides + répétition",
  "status": "TODO",
  "priority": "HIGH",
  "startAt": "2026-04-14T08:00:00Z",
  "dueAt": "2026-04-15T16:00:00Z",
  "allDay": false
}
```
- **Sortie (201)**
```json
{
  "id": "uuid",
  "taskListId": "uuid",
  "title": "Préparer la démo",
  "description": "Slides + répétition",
  "status": "TODO",
  "priority": "HIGH",
  "startAt": "2026-04-14T08:00:00Z",
  "dueAt": "2026-04-15T16:00:00Z",
  "completedAt": null,
  "allDay": false,
  "isRecurring": false,
  "createdAt": "...",
  "updatedAt": "..."
}
```
- **Validations** :
  - `title` requis (1..255)
  - `dueAt >= startAt` si présents
  - `status` dans enum
- **Erreurs métier** :
  - `404` liste introuvable,
  - `422` dates incohérentes.

## 3.2 Consulter une tâche
- **HTTP** : `GET /tasks/{taskId}`
- **Sortie (200)** : payload tâche.
- **Erreurs métier** : `404` tâche inexistante.

## 3.3 Lister les tâches
- **HTTP** : `GET /tasks?taskListId=&status=&priority=&from=&to=&page=0&size=20`
- **Sortie (200)**
```json
{
  "content": [
    {
      "id": "uuid",
      "taskListId": "uuid",
      "title": "Préparer la démo",
      "status": "IN_PROGRESS",
      "priority": "HIGH",
      "dueAt": "..."
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```
- **Validations** : `size <= 200`, période cohérente `from <= to`.

## 3.4 Modifier une tâche
- **HTTP** : `PATCH /tasks/{taskId}`
- **Entrée**
```json
{
  "title": "Préparer la démo client",
  "description": "Version finale",
  "status": "DONE",
  "priority": "URGENT",
  "dueAt": "2026-04-15T16:00:00Z"
}
```
- **Sortie (200)** : tâche mise à jour.
- **Validations** : idem création + transitions de statut autorisées.
- **Erreurs métier** :
  - `404` tâche introuvable,
  - `409` transition de statut interdite,
  - `422` `DONE` sans `completedAt` (si imposé côté service).

## 3.5 Supprimer une tâche (soft delete)
- **HTTP** : `DELETE /tasks/{taskId}`
- **Sortie** : `204 No Content`
- **Erreurs métier** : `404` tâche inexistante.

---

## 4) Tâches récurrentes

## 4.1 Créer/attacher une règle de récurrence
- **HTTP** : `POST /tasks/{taskId}/recurrence`
- **Entrée**
```json
{
  "frequency": "WEEKLY",
  "intervalValue": 1,
  "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"],
  "startDate": "2026-04-14",
  "endDate": "2026-12-31",
  "timezone": "Europe/Paris"
}
```
- **Sortie (201)**
```json
{
  "id": "uuid",
  "taskId": "uuid",
  "frequency": "WEEKLY",
  "intervalValue": 1,
  "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"],
  "startDate": "2026-04-14",
  "endDate": "2026-12-31",
  "timezone": "Europe/Paris",
  "active": true,
  "nextOccurrenceDate": "2026-04-15"
}
```
- **Validations** :
  - `intervalValue >= 1`
  - WEEKLY => `daysOfWeek` non vide
  - MONTHLY => `dayOfMonth` requis
  - YEARLY => `monthOfYear` + `dayOfMonth`
- **Erreurs métier** :
  - `404` tâche introuvable,
  - `409` règle déjà active,
  - `422` règle incohérente.

## 4.2 Lire la récurrence d’une tâche
- **HTTP** : `GET /tasks/{taskId}/recurrence`
- **Sortie (200)** : payload règle.
- **Erreurs métier** : `404` tâche ou règle introuvable.

## 4.3 Modifier la récurrence
- **HTTP** : `PATCH /tasks/{taskId}/recurrence`
- **Entrée** : même forme que création (partielle autorisée).
- **Sortie (200)** : règle mise à jour.
- **Erreurs métier** : `404`, `422`, `409`.

## 4.4 Désactiver la récurrence
- **HTTP** : `DELETE /tasks/{taskId}/recurrence`
- **Sortie** : `204 No Content`
- **Erreurs métier** : `404` si aucune règle.

---

## 5) Affectation d’une tâche à une journée

## 5.1 Affecter
- **HTTP** : `POST /tasks/{taskId}/assignments`
- **Entrée**
```json
{
  "assignmentDate": "2026-04-15",
  "plannedStartTime": "09:00:00",
  "plannedEndTime": "10:00:00",
  "origin": "MANUAL",
  "note": "Priorité du matin"
}
```
- **Sortie (201)**
```json
{
  "id": "uuid",
  "taskId": "uuid",
  "assignmentDate": "2026-04-15",
  "plannedStartTime": "09:00:00",
  "plannedEndTime": "10:00:00",
  "origin": "MANUAL",
  "statusOnDay": "PLANNED",
  "note": "Priorité du matin"
}
```
- **Validations** : `plannedEndTime >= plannedStartTime`.
- **Erreurs métier** :
  - `404` tâche introuvable,
  - `409` doublon `(taskId, assignmentDate)`.

## 5.2 Modifier une affectation
- **HTTP** : `PATCH /assignments/{assignmentId}`
- **Entrée** (exemple)
```json
{
  "statusOnDay": "DONE",
  "note": "Terminé avant 10h"
}
```
- **Sortie (200)** : affectation mise à jour.
- **Erreurs métier** : `404`, `409` état non compatible.

## 5.3 Supprimer une affectation
- **HTTP** : `DELETE /assignments/{assignmentId}`
- **Sortie** : `204 No Content`
- **Erreurs métier** : `404`.

---

## 6) Récupérer les tâches du jour

## 6.1 Tâches du jour (vue quotidienne)
- **HTTP** : `GET /tasks/today?date=2026-04-15&timezone=Europe/Paris`
- **Sortie (200)**
```json
{
  "date": "2026-04-15",
  "timezone": "Europe/Paris",
  "tasks": [
    {
      "taskId": "uuid",
      "title": "Préparer la démo",
      "status": "IN_PROGRESS",
      "assignment": {
        "assignmentId": "uuid",
        "plannedStartTime": "09:00:00",
        "plannedEndTime": "10:00:00",
        "statusOnDay": "PLANNED"
      }
    }
  ]
}
```
- **Validations** : `date` ISO local date, timezone IANA.
- **Erreurs métier** : `422` timezone invalide.

---

## 7) Récupérer les tâches d’une période

## 7.1 Vue période
- **HTTP** : `GET /tasks/range?from=2026-04-01&to=2026-04-30&taskListId=&status=&priority=`
- **Sortie (200)**
```json
{
  "from": "2026-04-01",
  "to": "2026-04-30",
  "tasks": [
    {
      "taskId": "uuid",
      "title": "Préparer la démo",
      "dueAt": "2026-04-15T16:00:00Z",
      "status": "TODO"
    }
  ]
}
```
- **Validations** : `from <= to`, intervalle max configurable (ex: 366 jours).
- **Erreurs métier** : `422` période trop large/invalide.

---

## 8) Rappels

## 8.1 Créer un rappel
- **HTTP** : `POST /tasks/{taskId}/reminders`
- **Entrée**
```json
{
  "type": "DUE_SOON",
  "triggerMode": "RELATIVE_DUE",
  "minutesBeforeDue": 60,
  "channel": "IN_APP"
}
```
ou
```json
{
  "type": "CUSTOM",
  "triggerMode": "ABSOLUTE_DATETIME",
  "triggerAt": "2026-04-15T08:00:00Z",
  "channel": "PUSH"
}
```
- **Sortie (201)**
```json
{
  "id": "uuid",
  "taskId": "uuid",
  "type": "DUE_SOON",
  "triggerMode": "RELATIVE_DUE",
  "minutesBeforeDue": 60,
  "triggerAt": null,
  "channel": "IN_APP",
  "status": "SCHEDULED",
  "attemptCount": 0
}
```
- **Validations** :
  - mode relatif => `minutesBeforeDue` requis
  - mode absolu => `triggerAt` requis
  - exclusivité `minutesBeforeDue` / `triggerAt`
- **Erreurs métier** :
  - `404` tâche introuvable,
  - `422` rappel après échéance,
  - `409` duplication de rappel identique.

## 8.2 Lister les rappels d’une tâche
- **HTTP** : `GET /tasks/{taskId}/reminders`
- **Sortie (200)** : liste de rappels.

## 8.3 Modifier un rappel
- **HTTP** : `PATCH /reminders/{reminderId}`
- **Entrée** : mêmes champs que création (partiel).
- **Sortie (200)** : rappel mis à jour.
- **Erreurs métier** :
  - `404` rappel introuvable,
  - `409` rappel déjà `SENT`/`CANCELED` non modifiable.

## 8.4 Supprimer/annuler un rappel
- **HTTP** : `DELETE /reminders/{reminderId}`
- **Sortie** : `204 No Content`
- **Erreurs métier** : `404`.

## 8.5 Forcer l’exécution d’un rappel (admin/debug)
- **HTTP** : `POST /reminders/{reminderId}/trigger`
- **Sortie (202)**
```json
{
  "reminderId": "uuid",
  "status": "QUEUED"
}
```
- **Erreurs métier** : `409` état incompatible.

---

## 9) Synchronisation agenda

## 9.1 Créer un lien agenda pour une tâche
- **HTTP** : `POST /tasks/{taskId}/calendar-links`
- **Entrée**
```json
{
  "provider": "GOOGLE",
  "calendarId": "primary",
  "syncDirection": "TODO_TO_CALENDAR"
}
```
- **Sortie (201)**
```json
{
  "id": "uuid",
  "taskId": "uuid",
  "provider": "GOOGLE",
  "calendarId": "primary",
  "externalEventId": "evt_123",
  "syncDirection": "TODO_TO_CALENDAR",
  "syncStatus": "LINKED",
  "lastSyncedAt": "2026-04-13T12:00:00Z"
}
```
- **Erreurs métier** :
  - `404` tâche introuvable,
  - `409` déjà liée à ce provider/calendar.

## 9.2 Lister les liens agenda
- **HTTP** : `GET /calendar-links?provider=&syncStatus=&taskId=`
- **Sortie (200)** : liste des liens.

## 9.3 Lancer une synchronisation d’un lien
- **HTTP** : `POST /calendar-links/{calendarLinkId}/sync`
- **Sortie (202)**
```json
{
  "calendarLinkId": "uuid",
  "status": "SYNC_REQUESTED"
}
```
- **Erreurs métier** :
  - `404` lien introuvable,
  - `409` sync déjà en cours,
  - `422` provider non connecté.

## 9.4 Lancer une synchronisation globale utilisateur
- **HTTP** : `POST /calendar-links/sync-all`
- **Entrée**
```json
{
  "provider": "GOOGLE"
}
```
- **Sortie (202)**
```json
{
  "status": "SYNC_ALL_REQUESTED",
  "provider": "GOOGLE"
}
```
- **Erreurs métier** : `422` provider non lié.

## 9.5 Délier un événement agenda
- **HTTP** : `DELETE /calendar-links/{calendarLinkId}`
- **Sortie** : `204 No Content`
- **Erreurs métier** :
  - `404` lien introuvable,
  - `409` suppression impossible si sync en cours.

---

## 10) Enums API (à partager backend/frontend)

- `TaskStatus`: `TODO`, `IN_PROGRESS`, `BLOCKED`, `DONE`, `CANCELED`
- `TaskPriority`: `LOW`, `MEDIUM`, `HIGH`, `URGENT`
- `RecurrenceFrequency`: `DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY`
- `ReminderType`: `DUE_SOON`, `DUE_NOW`, `OVERDUE`, `CUSTOM`
- `ReminderTriggerMode`: `RELATIVE_DUE`, `ABSOLUTE_DATETIME`
- `ReminderChannel`: `IN_APP`, `PUSH`, `EMAIL`
- `ReminderStatus`: `SCHEDULED`, `SENT`, `FAILED`, `CANCELED`
- `AssignmentOrigin`: `MANUAL`, `RECURRENCE`, `CALENDAR_SYNC`
- `DailyAssignmentStatus`: `PLANNED`, `DONE`, `SKIPPED`
- `CalendarProvider`: `GOOGLE`, `MICROSOFT`, `APPLE`
- `SyncDirection`: `TODO_TO_CALENDAR`, `BIDIRECTIONAL`
- `CalendarSyncStatus`: `PENDING`, `LINKED`, `SYNC_ERROR`, `UNLINKED`

---

## 11) Mapping direct vers implémentation backend

- `TaskListController` : endpoints section 2.
- `TaskController` : sections 3, 4, 5, 6, 7.
- `ReminderController` : section 8.
- `CalendarController` : section 9.

Recommandation technique : implémenter les payloads en DTO `*Request` / `*Response` et centraliser les validations métier en couche `service/application`.
