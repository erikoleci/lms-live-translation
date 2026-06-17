<template>
  <v-btn-toggle
    :model-value="modelValue"
    mandatory
    color="primary"
    variant="outlined"
    rounded="lg"
    divided
    @update:model-value="emit('update:modelValue', $event)"
  >
    <v-btn
      v-for="lang in LANGUAGES"
      :key="lang.value"
      :value="lang.value"
      class="px-3 px-sm-5"
    >
      <span style="font-size:20px">{{ lang.flag }}</span>
      <span class="ml-2 text-body-2 font-weight-medium d-none d-sm-inline">{{ lang.label }}</span>
    </v-btn>
  </v-btn-toggle>
</template>

<script setup>
defineProps({ modelValue: String })
const emit = defineEmits(['update:modelValue'])

const LANGUAGES = [
  { value: 'IT', label: 'Italian', flag: '🇮🇹' },
  { value: 'EN', label: 'English', flag: '🇬🇧' },
  { value: 'SQ', label: 'Albanian', flag: '🇦🇱' },
]
</script>
