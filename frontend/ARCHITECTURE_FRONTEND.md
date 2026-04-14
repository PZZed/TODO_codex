# Architecture frontend détaillée (Vue 3 + TypeScript + Vuetify)

## 1) Arborescence recommandée

```txt
frontend/
  src/
    app/
      main.ts
      router/
    pages/                 # pages de navigation (route-level)
    components/
      base/                # composants UI génériques (BasePage, BaseCard...)
      common/              # composants transverses (topbar, empty state...)
      task/                # composants métier tâche
    composables/           # logique réutilisable (état async, breakpoints, a11y)
    stores/                # état global Pinia (task, app, ui)
    services/
      api/                 # clients API / modules API
      errors/              # normalisation erreurs frontend
    forms/                 # schémas / validateurs formulaires
    models/                # modèles TypeScript métier
    plugins/               # Vuetify, i18n, etc.
    styles/                # tokens, responsive, utilitaires
```

## 2) Séparation des responsabilités

- **Pages** : orchestration d’écran, appels store/composables, composition UI.
- **Composants** : affichage et interactions locales, pas d’appels API directs.
- **Composables** : logique réutilisable (async, responsive, accessibilité, erreurs formulaire).
- **Services** : couche d’accès backend + mapping erreurs techniques vers erreurs métier front.
- **Stores** : état partagé et cache applicatif (listes, tâches, statut UI global).
- **Modèles** : contrats de types unifiés entre stores/services/components.

## 3) Stratégie de gestion d’état

- **Pinia** pour l’état global long-vécu : session utilisateur, listes, tâches, préférences UI.
- **État local composant** pour formulaires et interactions ponctuelles.
- Principe :
  - store = source de vérité partagée,
  - composable = logique transversale,
  - page = orchestration.

## 4) Stratégie de gestion des formulaires

- Modèles `FormState` typés + validateurs par feature (`forms/*`).
- Validation synchrone côté client (champs requis, tailles, cohérence dates).
- Mapping des erreurs backend (`422`, `409`) en erreurs de champ via `useFormErrors`.

## 5) Stratégie de gestion des erreurs

- Normalisation des erreurs HTTP via `services/errors` (`AppError`, `toAppError`).
- UX standard :
  - erreurs champ (formulaires),
  - erreurs page (alert),
  - erreurs globales (snackbar/store `ui`).

## 6) Stratégie d’accessibilité

- Composants base avec labels explicites, focus visibles, navigation clavier.
- Messages dynamiques importants annoncés via région ARIA (`useA11yAnnouncer`).
- Couleurs/thèmes contrastés via tokens Vuetify.

## 7) Stratégie responsive mobile-first

- Layout conçu d’abord mobile : bottom navigation, spacing réduit, cards compactes.
- Breakpoints pilotés par `useBreakpoint` + classes utilitaires `styles/responsive.css`.
- Desktop = enrichissement progressif (grilles plus larges, colonnes secondaires).
