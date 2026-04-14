# Intégration agenda V1 – approche recommandée

## Approche V1 recommandée : export ICS

Pour une V1 **simple, robuste et réaliste**, la meilleure option est l’export ICS :
- pas d’OAuth complexe ni de quotas provider,
- compatible avec Google/Outlook/Apple Calendar (abonnement à URL ICS),
- faible coût de maintenance au démarrage.

## Modèle de données nécessaire

- `CalendarIntegrationEntity`
  - `user` : propriétaire
  - `exportToken` : token secret d’accès au flux ICS
  - `enabled` : activation/désactivation
  - timestamps

Ce token permet un endpoint public sécurisé par URL opaque.

## Endpoints REST V1

- `POST /api/v1/calendar/integrations/ics?userId=...`
  - active (ou crée) l’intégration ICS utilisateur.
- `POST /api/v1/calendar/integrations/ics/{userId}/rotate-token`
  - rotation de token en cas de fuite.
- `GET /api/v1/calendar/ics/{token}?from=YYYY-MM-DD&to=YYYY-MM-DD`
  - export ICS des tâches planifiées (tâches + affectations).

## Logique backend implémentée

1. Activation intégration : création d’un token long aléatoire.
2. Export ICS :
   - résolution user via token,
   - récupération tâches planifiées sur la période,
   - récupération affectations sur la période,
   - génération de `VEVENT` + renvoi `text/calendar`.
3. Rotation token : invalide l’ancien lien ICS.

## Limites V1

- Pas de synchro bidirectionnelle.
- Pas de push instantané vers provider (refresh selon client agenda).
- Gestion timezone simplifiée (UTC dans ce socle).
- Pas de gestion fine des suppressions/mises à jour incrémentales (pas de sequence tracking avancé).

## Évolutions possibles

1. Ajouter Google Calendar API (OAuth2) pour sync unidirectionnelle push.
2. Ajouter table de mapping `internal_task_id <-> external_event_id`.
3. Gérer webhooks provider pour synchro bidirectionnelle.
4. Ajouter gestion fine timezone utilisateur + VTIMEZONE ICS.
5. Introduire un worker async pour génération lourde et caching d’exports.
