<template>
  <section class="base-page" aria-live="polite">
    <header class="mb-4">
      <h1 class="text-h6"><slot name="title" /></h1>
      <p v-if="$slots.subtitle" class="text-body-2 text-medium-emphasis">
        <slot name="subtitle" />
      </p>
    </header>
    <slot />
  </section>
</template>

<style scoped>
.base-page {
  width: 100%;
}
</style>
