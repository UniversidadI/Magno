<template>
  <VCard class="pa-5 ma-5 overflow-auto" max-width="600" max-height="85vh">
    <VCardTitle>Agregar {{ label }}</VCardTitle>
    <VDivider/>
    <VCardText>
      <VForm @submit.prevent="CreateItem">
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
            :label="field.label"
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
            :disabled="field.disabled"
            :required="field.required"
            clearable
            :ref="`select_${field.key}`"
            class="mb-5"
            :label="field.label"
            @update:model-value="handleFieldChange(field.key, $event)"
          />
          <VAutocomplete v-else-if="field.type === 'multiple-select'"
            multiple
            v-model="formValues[field.key]"
            :items="field.options"
            item-title="label"
            item-value="value"
            :custom-filter="customAutocompleteFilter"
            :label="field.label"
            class="mb-5"
          />
          <VTextarea no-resize v-else-if="field.type === 'textarea'"
            v-model="formValues[field.key]"
            :label="field.label"
            rows="5"
            class="mb-5"
          />
          <VCheckbox v-else-if="field.type === 'checkbox'"
            v-model="formValues[field.key]"
            :label="field.label"
            class="mb-5"
            :true-value="true"
            :false-value="false"
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
import { VAutocomplete } from 'vuetify/components';
import { useFeedbackToast } from '@/composables/useFeedbackToast';
import { userAutocompleteFilter } from '@/utils/userOptions';

export default defineComponent({
  name: 'formEditGeneral',
  emits: ['itemCreated', 'fieldChanged'],
  setup() {
    const { showError, showSuccess } = useFeedbackToast()
    return { showError, showSuccess }
  },
  props: {
    type: {
      type: String,
    },
    label: {
      type: String,
    },
    fields: {
      type: Object,
      default: () => ({}),
    },
    additionalData: {
      type: Object,
      default: () => ({}),
    },
    index: {
      type: [String, Number],
      default: null,
      required: false,
    }
  },
  data() {
    return {
      loading: false,
      formValues: this.initializeFormValues(),
      componentKey: 0,
    };
  },
  methods: {
    initializeFormValues() {
      const values: Record<string, any> = { ...this.additionalData };
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

    handleFieldChange(fieldKey: string | number, value: null) {
      this.formValues[fieldKey] = value;

      if (value === null) {
        this.componentKey++;
      }

      this.$emit('fieldChanged', fieldKey, value);
    },

    clearField(fieldKey: string | number) {
      this.formValues[fieldKey] = null;
      this.componentKey++;
      this.$nextTick(() => {
        this.$forceUpdate();
      });
    },

    async CreateItem() {
      this.loading = true;
      const headers = {
        'API-VERSION': '1',
      }
      try {
        let response;
        if (this.type === 'period') {
          // Ejemplo hipotético para editar un periodo
          response = await API.post(API.ACADEMIC_PERIODS, {
            ...this.formValues,
          }, headers);
        } else if (this.type === 'group') {
          response = await API.post(API.INVESTIGATION_GROUPS, {
            ...this.formValues,
          }, headers);
        } else if (this.type === 'seedbed') {
          response = await API.post(API.RESEARCH_SEEDBEDS, {
          ...this.formValues,
          }, headers);
        } else if (this.type === 'user_integra') {
          response = await API.post(API.USERS_INTEGRA, {
           ...this.formValues,
          }, headers);
        } else if (this.type === 'user_external') {
          response = await API.post(API.USERS, {
            ...this.formValues,
          }, headers);
        } else if (this.type === 'role') {
          response = await API.post(API.ROLES, {
          ...this.formValues,
          }, headers);
        } else if (this.type === 'functionary_profile') {
          response = await API.post(API.FUNCTIONARY_PROFILES, {
         ...this.formValues,
          }, headers);
        } else if (this.type === 'student_profile') {
          response = await API.post(API.STUDENT_PROFILES, {
         ...this.formValues,
          }, headers);
        } else if (this.type === 'external_profile' || this.type === 'external_seedbed_profile') {
          response = await API.post(API.EXTERNAL_USER_PROFILES, {
            ...this.formValues,
          }, headers);
        } else if (this.type === 'group_profile') {
          response = await API.post(API.INVESTIGATION_GRUOPS_PROFILES, {
            ...this.formValues,
          }, headers);
        } else if (this.type === 'seedbed_profile') {
          response = await API.post(API.RESEARCH_SEEDBEDS_PROFILES, {
            ...this.formValues,
          }, headers);
        } else if (this.type === 'seedbed_member') {
          response = await API.post(API.RESEARCH_SEEDBEDS_MEMBERS, {
              ...this.formValues,
            }, headers);
        } else if (this.type === 'user_diri') {
          response = await API.post(`${API.USERS_DIRI}?diri-identification=${this.formValues.identification}`, undefined, headers);
        }
        if (!response.error) {
          this.showSuccess('Elemento creado exitosamente');
          this.$emit('itemCreated', this.formValues.name);
        }
      } catch (error: any) {
        console.error("Error al crear", error);
        this.showError(error.response?.data);
      } finally {
        this.loading = false;
      }
    },
  },
});
</script>
