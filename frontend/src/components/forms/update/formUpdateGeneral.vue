<template>
  <VCard class="pa-5 ma-5 overflow-auto" max-width="600" max-height="85vh">
    <VCardTitle>Editar {{ label }}</VCardTitle>
    <VDivider/>
    <VCardText>
      <VForm @submit.prevent="editItem">
        <!-- Campos para editar, por ejemplo: nombre -->
        <div v-for="(field, index) in fields" :key="index">
          <VTextField v-if="field.type === 'text'"
            v-model="formValues[field.key]"
            :label="field.label"
            rows="5"
          />
          <VDateInput v-else-if="field.type === 'date'"
            v-model="formValues[field.key]"
            :label="field.label"
            type="text"
            prepend-icon=""
            prepend-inner-icon="ri-calendar-2-line"
          />
          <VRadioGroup v-else-if="field.type === 'radio-group'"
            v-model="formValues[field.key]"
            class="d-flex"
            inline
          >
            <VRadio
             v-for="(option,index) in field.options"
             :key="index"
              :label="option.label"
              :value="option.value"
            />
          </VRadioGroup>
          <VAutocomplete v-else-if="field.type === 'select'"
            :key="`${field.key}-${componentKey}`"
            v-model="formValues[field.key]"
            :items="field.options"
            item-title="label"
            item-value="value"
            :custom-filter="customAutocompleteFilter"
            :label="field.label"
            :disabled="field.disabled"
            clearable
            class="mb-5"
            @update:model-value="handleFieldChange(field.key, $event)"
          />
          <VSelect v-else-if="field.type === 'multiple-select'"
            multiple
            v-model="formValues[field.key]"
            :items="field.options"
            item-title="label"
            item-value="value"
            :label="field.label"
            @update:model-value="handleFieldChange(field.key, $event)"
          />
          <VTextarea v-else-if="field.type === 'textarea'"
            v-model="formValues[field.key]"
            :label="field.label"
            rows="5"
            class="mb-5"
            @update:model-value="handleFieldChange(field.key, $event)"
          />

          <VCheckbox v-else-if="field.type === 'checkbox'"
            v-model="formValues[field.key]"
            :label="field.label"
            class="mb-5"
            :true-value="true"
            :false-value="false"
            @update:model-value="handleFieldChange(field.key, $event)"
          />
        </div>
        <VCardItem class="d-flex justify-end">
          <LoadingBtn icon="ri-save-2-line" text="Guardar" :loading="loading" color="primary"/>
        </VCardItem>
      </VForm>
    </VCardText>
  </VCard>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import API from "@/utils/api";
import { VAutocomplete, VSelect } from 'vuetify/components';
import { useFeedbackToast } from '@/composables/useFeedbackToast';
import { userAutocompleteFilter } from '@/utils/userOptions';

export default defineComponent({
  name: 'formEditGeneral',
  emits: ['itemEdited', 'loaded', 'fieldChanged'],
  setup() {
    const { showError, showSuccess } = useFeedbackToast()
    return { showError, showSuccess }
  },
  props: {
    type: {
      type: String,
    },
    index: {
      type: Number,
    },
    label: {
      type: String,
    },
    fields: {
      type: Array as () => Array<{ key: string; label: string; type?: string, options?: Array<{ label: string; value: string}>, disabled?: boolean }>,
      default: () => [],
    },
    initialData: {
      type: Object,
      default: () => ({}),
    },
    additionalData: {
      type: Object,
      default: () => ({}),
    },
  },
  data() {
    return {

      //inputValue: this.itemName, // valor inicial
      loading: false,
      formValues: this.initializeFormValues(),
      componentKey: 0,

    };
  },
  watch: {
    initialData: {
      handler(newVal) {
        this.formValues = { ...this.formValues, ...newVal };
      },
      deep: true,
    },
    additionalData: {
      handler(newVal) {
        this.formValues = { ...this.formValues, ...newVal };
      },
      deep: true,
    },
  },
  created() {
    this.$emit('loaded');
  },
  methods: {
    initializeFormValues() {
      const values: Record<string, any> = { ...this.initialData, ...this.additionalData };
      if (Array.isArray(this.fields)) {
        this.fields.forEach(field => {
          if (values[field.key] === undefined) {
            values[field.key] = (field.type === 'checkbox' || field.type === 'radio-group') ? false : null;
          }
        });
      }
      return values;
    },
    customAutocompleteFilter(value: string, query: string, item?: any) {
      return userAutocompleteFilter(value, query, item);
    },

    handleFieldChange(fieldKey: string | number, value: any) {
      this.formValues[fieldKey] = value;

      if (value === null) {
        this.componentKey++;
      }

      this.$emit('fieldChanged', fieldKey, value);
    },
    async editItem() {
      this.loading = true;
      const headers = {
        'API-VERSION': '1',
      }
      try {
        let response;
        if (this.type === 'period') {
          // Ejemplo hipotético para editar un periodo
          response = await API.put(API.ACADEMIC_PERIODS + this.index, {
            ...this.formValues,
          }, headers);
        } else if (this.type === 'seedbed') {
          // Ejemplo hipotético para editar un semillero
          response = await API.put(API.RESEARCH_SEEDBEDS+ this.index, {
            ...this.formValues,
          }, headers);
        } else if (this.type === 'group') {
          // Ejemplo hipotético para editar un grupo
          response = await API.put(API.INVESTIGATION_GROUPS + this.index, {
            ...this.formValues,
          }, headers);
        } else if (this.type === 'group_profile') {
          response = await API.put(API.INVESTIGATION_GRUOPS_PROFILES + this.index, {
            ...this.formValues,
          }, headers);
        } else if (this.type == 'seedbed_profile' || this.type == 'seedbed_coordinator' || this.type == 'seedbed_tutor') {
          response = await API.put(API.RESEARCH_SEEDBEDS_PROFILES + this.index, {
            ...this.formValues,
          }, headers);
        } else if (this.type == 'seedbed_member') {
          response = await API.put(API.RESEARCH_SEEDBEDS_MEMBERS + this.index, {
            ...this.formValues,
          }, headers);
        } else if (this.type == 'external_profile' || this.type == 'external_seedbed_profile') {
          response = await API.put(API.EXTERNAL_USER_PROFILES + this.index, {
            ...this.formValues,
          }, headers);
        } else if (this.type == 'student_profile') {
          response = await API.put(API.STUDENT_PROFILES + this.index, {
            ...this.formValues,
          }, headers);
        } else if (this.type == 'functionary_profile') {
          response = await API.put(API.FUNCTIONARY_PROFILES + this.index, {
            ...this.formValues,
          }, headers);
        }
        if (response && !response.error) {
          this.showSuccess('Elemento actualizado exitosamente');
          this.$emit('itemEdited', this.index, this.formValues.name);
        }
      } catch (error: any) {
        console.error("Error al editar", error);
        this.showError(error.response?.data);
      } finally {
        this.loading = false;
      }
    },
  },
});
</script>
