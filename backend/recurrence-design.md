# Gestion des tâches récurrentes – modèle et distinction occurrence

## Distinction claire des modèles

- **Tâche récurrente** (`TaskEntity` + `RecurringTaskRuleEntity`)
  - Représente le **modèle source** (template).
  - Porte la définition métier de la récurrence : fréquence, intervalle, jours de semaine, date début/fin.
  - N’est pas une occurrence “journalière” en elle-même.

- **Occurrence assignée à une journée** (`DailyTaskAssignmentEntity`)
  - Représente une **instance visible/exécutable** sur un jour donné.
  - Sert à afficher/planifier dans la vue quotidienne (horaires, note, statut du jour).
  - Peut venir d’une tâche simple (assignation manuelle) ou d’une tâche récurrente.

## Règles métier implémentées

- Fréquences supportées : `DAILY`, `WEEKLY`, `MONTHLY`.
- Hebdomadaire : obligation de fournir `daysOfWeek`.
- Mensuelle : `dayOfMonth` optionnel (sinon jour de `startDate`).
- `endDate` optionnelle mais si présente : `endDate >= startDate`.
- `intervalValue >= 1`.
- Une tâche ne peut avoir qu’une règle active de récurrence.

## Endpoints REST ajoutés

- `POST /api/v1/tasks/{taskId}/recurrence` : créer une règle.
- `PATCH /api/v1/tasks/{taskId}/recurrence` : modifier.
- `GET /api/v1/tasks/{taskId}/recurrence` : consulter.
- `DELETE /api/v1/tasks/{taskId}/recurrence` : désactiver.
- `GET /api/v1/recurrences/day?userId=&date=` : occurrences visibles d’un jour.
- `GET /api/v1/recurrences/range?userId=&from=&to=` : occurrences visibles sur période.

## Logique d’occurrences visibles

1. Charger les règles actives de l’utilisateur.
2. Intersecter la période demandée avec `[startDate, endDate?]`.
3. Évaluer chaque jour de la fenêtre avec la règle de matching :
   - quotidienne : modulo en jours,
   - hebdomadaire : modulo en semaines + `daysOfWeek`,
   - mensuelle : modulo en mois + jour ciblé.
4. Produire des `TaskOccurrenceResponse` triées par date.

## Exceptions métier

- `BusinessConflictException` si règle invalide (range incohérent, weekly sans jours, etc.).
- `ResourceNotFoundException` si tâche/règle introuvable.

