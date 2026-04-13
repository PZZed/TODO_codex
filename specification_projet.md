# Spécification fonctionnelle et technique

## 1) Cadrage et hypothèses

> **Contexte** : le besoin détaillé (métier, périmètre, utilisateurs, contraintes) n’a pas été fourni dans ce dépôt.
> Cette spécification est donc proposée comme **base structurée** à valider, avec un angle “application métier de gestion de tâches/projets” (cohérent avec le nom du repository `TODO_codex`).

### 1.1 Objectif produit
Créer une application web (et API) permettant à des utilisateurs individuels et des équipes de :
- créer, organiser et prioriser des tâches ;
- suivre l’avancement ;
- collaborer (assignation, commentaires, notifications) ;
- piloter via des vues synthétiques (tableau de bord, filtres, recherche).

### 1.2 Périmètre initial (MVP)
- Authentification et gestion de compte.
- Gestion de projets/espaces de travail.
- CRUD des tâches.
- Statuts, priorités, dates d’échéance.
- Assignation des tâches.
- Commentaires simples.
- Notifications in-app.
- Recherche et filtres.

### 1.3 Hors périmètre (phase ultérieure)
- Facturation/abonnement avancé.
- Automatisations complexes (workflows no-code).
- IA générative ou prédiction de charge.
- Intégrations tierces poussées (ERP, SSO enterprise avancé).

---

## 2) Fonctionnalités principales

1. **Gestion des comptes et accès**
   - Inscription, connexion, réinitialisation mot de passe.
   - Gestion du profil utilisateur.
   - Rôles et permissions (admin, manager, membre, lecteur).

2. **Gestion des espaces/projets**
   - Création d’un espace de travail.
   - Création/modification/archivage de projets.
   - Invitation de membres par email.

3. **Gestion des tâches**
   - Création, édition, suppression logique (archivage).
   - Attributs : titre, description, statut, priorité, échéance, assigné, étiquettes.
   - Sous-tâches (option MVP+).

4. **Suivi de l’avancement**
   - Vues Liste / Kanban.
   - Filtres multi-critères (statut, priorité, membre, dates).
   - Historique des changements clés (audit léger).

5. **Collaboration**
   - Commentaires sur les tâches.
   - Mentions utilisateurs.
   - Notifications in-app sur événements importants.

6. **Recherche et reporting de base**
   - Recherche plein texte (titre/description).
   - Indicateurs simples : tâches en retard, tâches terminées cette semaine, charge par membre.

---

## 3) Règles de gestion

### 3.1 Règles d’accès
- Un utilisateur doit être authentifié pour accéder aux données.
- Un utilisateur ne peut voir que les projets de ses espaces autorisés.
- Seuls les rôles `admin` et `manager` peuvent modifier la configuration d’un projet.

### 3.2 Règles sur les tâches
- Une tâche appartient à un seul projet.
- Une tâche a un statut parmi : `todo`, `in_progress`, `blocked`, `done`.
- Une tâche `done` peut être réouverte en `in_progress`.
- La date d’échéance doit être supérieure ou égale à la date de création.
- Une tâche ne peut pas être supprimée définitivement en MVP (soft delete uniquement).

### 3.3 Règles d’assignation
- Une tâche peut être non assignée ou assignée à un unique membre.
- L’assignation est autorisée seulement si l’utilisateur est membre du projet.

### 3.4 Règles de commentaires et notifications
- Chaque commentaire est horodaté et rattaché à un auteur.
- Une notification est créée lors de :
  - assignation à un utilisateur ;
  - mention dans un commentaire ;
  - tâche en retard (batch quotidien).

### 3.5 Règles de traçabilité
- Les événements critiques (création tâche, changement statut, assignation) sont historisés.
- Les horodatages sont stockés en UTC.

---

## 4) Entités métier

1. **Utilisateur**
   - id, nom, email, mot_de_passe_hash, statut_compte, date_creation.

2. **Espace de travail (Workspace)**
   - id, nom, propriétaire_id, date_creation, statut.

3. **Membre d’espace**
   - id, workspace_id, user_id, rôle, date_invitation, date_acceptation.

4. **Projet**
   - id, workspace_id, nom, description, date_creation, date_archivage.

5. **Tâche**
   - id, project_id, titre, description, statut, priorité, échéance, assignee_id, créateur_id, created_at, updated_at, deleted_at.

6. **Commentaire**
   - id, task_id, auteur_id, contenu, created_at, updated_at.

7. **Notification**
   - id, user_id, type, payload_json, lu, created_at.

8. **Historique d’événement (AuditEvent)**
   - id, entité_type, entité_id, action, acteur_id, metadata_json, created_at.

---

## 5) Cas d’usage principaux

### UC1 – Créer un compte et rejoindre un espace
- **Acteurs** : utilisateur invité.
- **Préconditions** : invitation envoyée.
- **Scénario nominal** :
  1. L’utilisateur ouvre le lien d’invitation.
  2. Il crée son compte.
  3. Il confirme son email.
  4. Il rejoint l’espace avec le rôle prévu.
- **Postconditions** : utilisateur actif dans l’espace.

### UC2 – Créer et assigner une tâche
- **Acteurs** : manager/membre.
- **Préconditions** : accès au projet.
- **Scénario nominal** :
  1. L’utilisateur crée une tâche avec titre et priorité.
  2. Il assigne la tâche à un membre.
  3. Le système notifie l’assigné.
- **Postconditions** : tâche visible dans la vue du membre assigné.

### UC3 – Mettre à jour le statut d’une tâche
- **Acteurs** : membre assigné.
- **Préconditions** : tâche existante.
- **Scénario nominal** : passage `todo` → `in_progress` → `done`.
- **Postconditions** : historique mis à jour, indicateurs recalculés.

### UC4 – Rechercher des tâches en retard
- **Acteurs** : manager.
- **Préconditions** : tâches avec échéance.
- **Scénario nominal** : filtre “échéance < aujourd’hui ET statut != done”.
- **Postconditions** : liste exploitable pour priorisation.

### UC5 – Collaborer via commentaires/mentions
- **Acteurs** : membres projet.
- **Préconditions** : tâche existante.
- **Scénario nominal** : ajout commentaire avec @mention.
- **Postconditions** : notification envoyée à l’utilisateur mentionné.

---

## 6) Écrans principaux

1. **Écran de connexion / inscription**
   - Email, mot de passe, SSO optionnel, reset mot de passe.

2. **Tableau de bord global**
   - Synthèse des tâches par statut, échéances proches, tâches en retard.

3. **Liste des projets**
   - Création projet, archivage, recherche projet.

4. **Écran projet – vue Liste**
   - Table des tâches, tri/filtres, actions rapides.

5. **Écran projet – vue Kanban**
   - Colonnes par statut, glisser-déposer (optionnel MVP).

6. **Détail tâche**
   - Informations complètes, commentaires, historique, assignation.

7. **Écran administration espace**
   - Gestion membres, rôles, invitations.

8. **Centre de notifications**
   - Liste des notifications, marquage lu/non lu.

---

## 7) API nécessaires (proposition REST)

### 7.1 Authentification
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/reset-password`

### 7.2 Utilisateurs / profils
- `GET /api/v1/me`
- `PATCH /api/v1/me`

### 7.3 Workspaces et membres
- `POST /api/v1/workspaces`
- `GET /api/v1/workspaces`
- `GET /api/v1/workspaces/{id}`
- `POST /api/v1/workspaces/{id}/invitations`
- `GET /api/v1/workspaces/{id}/members`
- `PATCH /api/v1/workspaces/{id}/members/{userId}`

### 7.4 Projets
- `POST /api/v1/projects`
- `GET /api/v1/projects?workspaceId=...`
- `GET /api/v1/projects/{id}`
- `PATCH /api/v1/projects/{id}`
- `DELETE /api/v1/projects/{id}` (archivage)

### 7.5 Tâches
- `POST /api/v1/tasks`
- `GET /api/v1/tasks?projectId=...&status=...&assigneeId=...`
- `GET /api/v1/tasks/{id}`
- `PATCH /api/v1/tasks/{id}`
- `DELETE /api/v1/tasks/{id}` (soft delete)
- `POST /api/v1/tasks/{id}/comments`
- `GET /api/v1/tasks/{id}/comments`
- `GET /api/v1/tasks/{id}/audit-events`

### 7.6 Notifications
- `GET /api/v1/notifications`
- `PATCH /api/v1/notifications/{id}` (lu/non lu)
- `POST /api/v1/notifications/mark-all-read`

### 7.7 Exigences API transverses
- Auth JWT (access + refresh token).
- Pagination (`page`, `limit`) sur les listes.
- Tri (`sortBy`, `sortOrder`).
- Format d’erreur standardisé (code, message, details).
- Idempotency key pour endpoints sensibles (ex : invitations).

---

## 8) Points d’attention techniques

1. **Architecture**
   - Backend modulaire (ex : Domain/Application/Infrastructure).
   - Frontend SPA (React/Vue) avec gestion d’état claire.

2. **Base de données**
   - PostgreSQL recommandé.
   - Index sur `tasks(project_id, status, due_date, assignee_id)`.
   - Stratégie de migration versionnée.

3. **Sécurité**
   - Hash mot de passe fort (Argon2 ou bcrypt cost élevé).
   - Contrôle d’accès systématique côté API.
   - Protection brute force login, rotation des tokens.

4. **Performance**
   - Pagination obligatoire.
   - Cache sur agrégats dashboard.
   - Éviter N+1 (préchargement relations).

5. **Observabilité**
   - Logs structurés (JSON).
   - Traces sur endpoints critiques.
   - Métriques : latence p95, taux erreur, volume requêtes.

6. **Qualité / Delivery**
   - CI : lint + tests unitaires + tests d’intégration API.
   - Convention de version d’API (`/v1`).
   - Stratégie de rollback DB/app.

7. **Conformité et données**
   - Gestion RGPD : consentement, suppression/anonymisation.
   - Journalisation limitée des données sensibles.

---

## 9) Risques et zones ambiguës à clarifier

1. **Périmètre fonctionnel réel**
   - L’application cible-t-elle uniquement la gestion de tâches ou un scope plus large (planning, time tracking, documents) ?

2. **Modèle de rôles**
   - Granularité exacte des permissions attendues (par projet, par action, par champ) ?

3. **Workflow métier**
   - Statuts imposés vs personnalisables par projet ?
   - Transitions autorisées (workflow strict ou libre) ?

4. **Multi-tenant et isolation**
   - Exigence stricte d’isolation par workspace (juridique/sécurité) ?

5. **Exigences non fonctionnelles**
   - SLA/volumétrie cible (nb utilisateurs, nb tâches, concurrence) ?
   - Objectifs de performance (temps de réponse max) ?

6. **Intégrations externes**
   - Email provider, SSO, calendrier, messagerie (Slack/Teams) : nécessaires en MVP ?

7. **Notifications**
   - In-app seulement ou email/push également ?
   - Fréquence et regroupement (digest vs temps réel) ?

8. **Conformité légale**
   - Besoin SOC2/ISO27001, rétention légale, localisation des données ?

9. **UX produit**
   - Priorité mobile vs desktop ?
   - Accessibilité visée (WCAG 2.1 AA ?) ?

10. **Stratégie de migration**
    - Existence d’un legacy à reprendre ? import de données attendu ?

---

## 10) Plan de clarification recommandé (atelier de cadrage)

1. Valider le périmètre MVP (features must-have vs nice-to-have).
2. Valider les rôles et matrices de permissions.
3. Valider le workflow de tâche (statuts + transitions).
4. Définir les KPI produit et SLO techniques.
5. Prioriser les intégrations externes.
6. Produire backlog initial (épics → user stories → critères d’acceptation).

---

## 11) Critères d’acceptation globaux (MVP)

- Un utilisateur invité peut rejoindre un espace et accéder aux projets autorisés.
- Un membre peut créer, assigner et suivre une tâche jusqu’à `done`.
- Un manager peut filtrer les tâches en retard et par membre.
- Les actions clés génèrent un historique exploitable.
- Les endpoints API principaux sont couverts par tests d’intégration.
- Les temps de réponse API sur endpoints critiques respectent la cible (à définir en cadrage).
