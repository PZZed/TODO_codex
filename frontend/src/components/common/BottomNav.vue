<template>
  <v-bottom-navigation grow aria-label="Navigation principale">
    <v-btn to="/" value="dashboard" aria-label="Aller au tableau de bord">Dashboard</v-btn>
    <v-btn to="/lists" value="lists" aria-label="Aller aux listes">Listes</v-btn>
    <v-btn to="/day" value="day" aria-label="Aller à ma journée">Jour</v-btn>
    <v-btn to="/recurrences" value="rec" aria-label="Aller aux récurrences">Récurrences</v-btn>
    <v-btn to="/settings/calendar" value="settings" aria-label="Aller aux réglages agenda">Agenda</v-btn>
  </v-bottom-navigation>
</template>
