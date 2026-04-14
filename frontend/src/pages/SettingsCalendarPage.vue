<template>
  <section>
    <h1 class="text-h6 mb-4">Paramètres agenda</h1>
    <v-card class="pa-4 mb-4">
      <v-btn color="primary" block @click="enable">Activer export ICS</v-btn>
      <v-btn class="mt-2" block variant="outlined" @click="rotate">Régénérer le token</v-btn>
    </v-card>

    <v-alert v-if="icsUrl" type="success" variant="tonal">
      URL ICS : {{ icsUrl }}
    </v-alert>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { calendarService } from '../services/api/calendarService'

const userId = 'demo-user-id'
const icsUrl = ref('')

async function enable() {
  const data = await calendarService.enableIcs(userId)
  icsUrl.value = data.publicIcsUrl
}

async function rotate() {
  const data = await calendarService.rotateIcsToken(userId)
  icsUrl.value = data.publicIcsUrl
}
</script>
