# Structure de packages backend recommandée (Spring Boot)

## 1) Principes

- **Architecture hexagonale / clean** par domaine fonctionnel.
- Séparation explicite des couches :
  - `domain` (métier pur)
  - `application` (cas d’usage)
  - `infrastructure` (implémentations techniques)
  - `exposition/rest` (API HTTP)
- Un module par sous-domaine métier : `identity`, `planning`, `reminder`, `calendar`.

---

## 2) Arborescence complète

```txt
com.todoapp.todo
├── TodoApplication
├── shared
│   ├── domain
│   │   ├── DomainEvent
│   │   └── DomainService
│   ├── application
│   │   ├── UseCase
│   │   └── TransactionManager
│   ├── infrastructure
│   │   └── PersistenceConfig
│   └── exposition
│       └── rest
│           └── GlobalRestExceptionHandler
│
├── identity
│   ├── domain
│   │   ├── User
│   │   ├── UserId
│   │   └── UserRepository
│   ├── application
│   │   ├── RegisterUserUseCase
│   │   └── GetUserUseCase
│   ├── infrastructure
│   │   ├── persistence
│   │   │   ├── UserJpaEntity
│   │   │   ├── SpringDataUserRepository
│   │   │   └── UserRepositoryAdapter
│   │   └── security
│   │       └── PasswordHasher
│   └── exposition
│       └── rest
│           ├── UserController
│           ├── UserRequest
│           └── UserResponse
│
├── planning
│   ├── domain
│   │   ├── Task
│   │   ├── TaskList
│   │   ├── RecurringTaskRule
│   │   ├── DailyTaskAssignment
│   │   ├── TaskRepository
│   │   └── TaskListRepository
│   ├── application
│   │   ├── CreateTaskUseCase
│   │   ├── UpdateTaskStatusUseCase
│   │   └── AssignTaskToDayUseCase
│   ├── infrastructure
│   │   └── persistence
│   │       ├── TaskJpaEntity
│   │       ├── TaskListJpaEntity
│   │       ├── RecurringTaskRuleJpaEntity
│   │       ├── DailyTaskAssignmentJpaEntity
│   │       ├── SpringDataTaskRepository
│   │       └── TaskRepositoryAdapter
│   └── exposition
│       └── rest
│           ├── TaskController
│           ├── TaskListController
│           ├── TaskRequest
│           └── TaskResponse
│
├── reminder
│   ├── domain
│   │   ├── Reminder
│   │   ├── ReminderRepository
│   │   └── ReminderScheduler
│   ├── application
│   │   ├── ScheduleReminderUseCase
│   │   └── TriggerReminderUseCase
│   ├── infrastructure
│   │   └── scheduler
│   │       ├── SpringReminderScheduler
│   │       └── ReminderJobRunner
│   └── exposition
│       └── rest
│           └── ReminderController
│
└── calendar
    ├── domain
    │   ├── CalendarLink
    │   ├── CalendarLinkRepository
    │   └── CalendarSyncGateway
    ├── application
    │   ├── LinkCalendarEventUseCase
    │   └── SyncCalendarEventUseCase
    ├── infrastructure
    │   └── provider
    │       ├── GoogleCalendarGateway
    │       └── MicrosoftCalendarGateway
    └── exposition
        └── rest
            └── CalendarController
```

---

## 3) Rôle de chaque package

### `shared.*`
- Contient les briques transverses communes à tous les domaines.
- `shared.domain` : abstractions métier globales (events, services métier de base).
- `shared.application` : contrats de cas d’usage (exécution, transaction).
- `shared.infrastructure` : config technique transversale.
- `shared.exposition.rest` : gestion uniforme des erreurs HTTP.

### `identity.*`
- Gestion des utilisateurs et de l’identité.
- `domain` : modèle et règles utilisateur.
- `application` : cas d’usage d’inscription/consultation.
- `infrastructure` : persistance JPA et sécurité (hash mot de passe).
- `exposition.rest` : endpoints `/api/v1/users`.

### `planning.*`
- Cœur fonctionnel des tâches (liste, tâche, récurrence, affectation journalière).
- `domain` : invariants métier des tâches.
- `application` : orchestration des actions métier.
- `infrastructure.persistence` : mapping DB/PostgreSQL.
- `exposition.rest` : endpoints `/api/v1/tasks` et `/api/v1/task-lists`.

### `reminder.*`
- Planification et déclenchement des rappels.
- `domain` : règle métier de rappel.
- `application` : cas d’usage de scheduling et déclenchement.
- `infrastructure.scheduler` : implémentation Spring Scheduler / workers.
- `exposition.rest` : API de pilotage des rappels.

### `calendar.*`
- Synchronisation avec agendas externes.
- `domain` : lien interne <-> externe et contrat de gateway.
- `application` : association/synchronisation d’événements.
- `infrastructure.provider` : implémentations Google/Microsoft.
- `exposition.rest` : endpoints de lien/sync calendrier.

---

## 4) Classes principales à générer

Les classes squelettes ont été générées avec responsabilité explicite (JavaDoc) dans les packages ci-dessus pour servir de base d’implémentation incrémentale.
