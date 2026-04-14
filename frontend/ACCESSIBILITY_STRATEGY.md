# Stratégie d’accessibilité frontend (Vue 3 + Vuetify)

## Objectifs
- Atteindre une base solide WCAG 2.2 niveau AA sur les parcours principaux.
- Garantir une navigation clavier complète sur mobile + desktop.
- Rendre les formulaires compréhensibles et robustes pour lecteurs d’écran.

## Règles WCAG prioritaires
1. **Perceivable**
   - Contrastes AA (texte normal >= 4.5:1, gros texte >= 3:1).
   - Information non portée uniquement par la couleur.
2. **Operable**
   - Tous les éléments interactifs focusables au clavier.
   - Focus visible et ordre de tabulation logique.
   - Lien d’évitement vers le contenu principal.
3. **Understandable**
   - Libellés explicites, aides et erreurs compréhensibles.
   - Messages d’action clairs (créé, supprimé, terminé).
4. **Robust**
   - Landmarks (`banner`, `main`, `navigation`) et labels ARIA cohérents.
   - Composants Vuetify configurés avec `aria-label` lorsque nécessaire.

## Conventions techniques appliquées
- **Landmarks globaux** dans le layout (`banner`, `main`, `navigation`) + skip-link.
- **Région live** globale (`aria-live="polite"`) pilotée par un store `a11y` pour annoncer les actions.
- **Formulaires**: champs requis explicites, erreurs associées au champ, autofocus sur le premier champ utile.
- **Navigation clavier**: boutons natifs/Vuetify plutôt que div cliquables.
- **Focus management**: `main` focusable via skip-link, dialogs avec premier champ autofocus.
- **Feedback SR**: annonces lors des actions CRUD / marquage terminé.

## Checklist PR (obligatoire)
- [ ] Chaque vue a un titre de page (`h1`) unique.
- [ ] Chaque action importante déclenche un feedback visible + SR.
- [ ] Tous les champs ont un label clair.
- [ ] Les erreurs de validation sont lisibles, non ambiguës.
- [ ] Les boutons icônes ont un `aria-label` explicite.
- [ ] Le parcours clavier est testable sans souris.
