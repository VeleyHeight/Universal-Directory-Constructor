<script setup lang="ts">
withDefaults(
    defineProps<{
      type?: 'button' | 'submit' | 'reset'
      variant?: 'primary' | 'secondary' | 'danger'
      disabled?: boolean
    }>(),
    { type: 'button', variant: 'primary', disabled: false }
)
</script>

<template>
  <button
      :type="type"
      :disabled="disabled"
      :class="['app-button', `app-button--${variant}`]"
  >
    <slot />
  </button>
</template>

<style scoped>
.app-button {
  border: none;
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-sm);
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  transition: background 0.2s;
  font-size: 13px;
}

.app-button--primary {
  background-color: var(--primary);
  color: #fff;
}

.app-button--danger {
  background-color: var(--danger);
  color: #fff;
}

.app-button--secondary {
  background: var(--btn-secondary);
  color: var(--text);
}
.app-button--secondary:hover {
  background: var(--btn-secondary-hover);
}

.app-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
