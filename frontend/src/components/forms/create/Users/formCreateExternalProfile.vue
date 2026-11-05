<template>
  <formCreateGeneral
    v-if="loaded"
    :type="type"
    :fields="processedFields"
    :name="name"
    :additionalData="additionalData"
    @itemCreated="handleItemCreated"
    @fieldChanged="handleFieldChange"
    ref="formComponent"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import API from '@/utils/api';
import { buildUserOptions } from '@/utils/userOptions';
import formCreateGeneral from '@/components/forms/create/formCreateGeneral.vue';

export default defineComponent({
  name: 'formCreateExternalProfile',
  components: {
    formCreateGeneral
  },
  emits: ['loaded', 'itemCreated', 'fieldChanged'],
  props: {
    name: { type: String },
    fields: {
      type: Array as () => Array<{
        key: string;
        label: string;
        type?: string;
        options?: Array<{ label: string; value: string }>;
        dependsOn?: string;
        disabled?: boolean;
        required?: boolean;
      }>,
      default: () => [],
    },
    type: { type: String },
    additionalData : {
      type: Object,
      default: () => ({}),
    },
  },
  data() {
    return {
      loaded: false,
      additionalData: {},
      formData: {} as Record<string, any>,
      processedFields: [] as any[],
    };
  },
  async created() {
    this.processedFields = [...this.fields];
    await this.fetchPeriods();
    this.setUserId();
    await this.fetchCountry();
    await this.fetchExternalType();
    await this.fetchExternalUsers();
    this.loaded = true;
    this.$emit('loaded');
  },

  beforeUnmount() {
    // Reset form to initial state when closing
    this.resetFormToInitialState();
  },

  methods: {
    handleItemCreated() {
      this.$emit('itemCreated');
    },

    async handleFieldChange(fieldKey: string, value: any) {
      this.formData[fieldKey] = value;

      if (fieldKey === 'academic_period_id') {
        await this.handlePeriodChange(value);
      } else if (fieldKey === 'research_group_profile_id') {
        await this.handleGroupChange(value);
      }
    },

    async handlePeriodChange(periodId: string) {
      if (!periodId) {
        this.resetDependentFields(['research_group_profile_id', 'research_seedbed_profile_id']);
        return;
      }

      try {
        // Clear the selected values for dependent fields FIRST
        this.clearFieldValues(['research_group_profile_id', 'research_seedbed_profile_id']);

        // Enable the group field
        this.enableField('research_group_profile_id');

        // Fetch groups for the selected period
        await this.fetchGroupsByPeriod(periodId);

        // Reset dependent fields (seedbeds)
        this.resetDependentFields(['research_seedbed_profile_id']);

      } catch (error) {
        console.error('Error loading groups:', error);
      }
    },

    async handleGroupChange(groupId: string) {
      if (!groupId) {
        this.resetDependentFields(['research_seedbed_profile_id']);
        return;
      }

      try {
        this.clearFieldValues(['research_seedbed_profile_id']);
        this.enableField('research_seedbed_profile_id');
        await this.fetchSeedbedsByGroup(groupId);
      } catch (error) {
        console.error('Error loading seedbeds:', error);
      }
    },

    enableField(fieldKey: string) {
      const field = this.processedFields.find(f => f.key === fieldKey);
      if (field) {
        field.disabled = false;
        this.$forceUpdate();
      }
    },

    disableField(fieldKey: string) {
      const field = this.processedFields.find(f => f.key === fieldKey);
      if (field) {
        field.disabled = true;
        this.formData[fieldKey] = null;
        this.$forceUpdate();
      }
    },

    resetDependentFields(fieldKeys: string[]) {
      fieldKeys.forEach(key => {
        const field = this.processedFields.find(f => f.key === key);
        if (field) {
          field.disabled = true;
          field.options = [];
          this.formData[key] = null;


        }
      });
    },

    clearFieldValues(fieldKeys: string[]) {
      fieldKeys.forEach(key => {
        this.formData[key] = null;
      });

      this.$nextTick(() => {
        fieldKeys.forEach(key => {
          this.$emit('fieldChanged', key, null);
          // Update the child component's formValues
          if (this.$refs.formComponent) {
            (this.$refs.formComponent as any).formValues[key] = null;
          }
        });
      });
    },

    async fetchPeriods() {
      const headers = { 'API-VERSION': '1' };
      try {
        const periods = await API.get(API.VISIBLE_ACADEMIC_PERIODS, headers);
        const periodField = this.processedFields.find(f => f.key === 'academic_period_id');
        if (periodField) {
          if (periods && periods.length > 0) {
            periodField.options = periods.map((period: any) => ({
              label: period.name,
              value: period.id,
            }));
          } else {
            periodField.options = [{
              label: 'No hay períodos académicos disponibles',
              value: null,
              disabled: true
            }];
          }
        }
      } catch (error) {
        console.error('Error fetching periods:', error);
        const periodField = this.processedFields.find(f => f.key === 'academic_period_id');
        if (periodField) {
          periodField.options = [{
            label: 'Error al cargar períodos académicos',
            value: null,
            disabled: true
          }];
        }
      }
    },

    async fetchGroupsByPeriod(periodId: string) {
      const headers = { 'API-VERSION': '1' };

      const groupField = this.processedFields.find(f => f.key === 'research_group_profile_id');
      if (groupField) {
        groupField.options = [];
      }

      try {
        const groups = await API.get(`${API.INVESTIGATION_GROUPS_PROFILES_BY_ACADEMIC_PERIOD}${periodId}`, headers);

        if (groupField) {
          if (groups && groups.length > 0) {
            groupField.options = groups.map((group: any) => ({
              label: group.investigation_group?.name || group.name,
              value: group.id,
            }));
          } else {
            groupField.options = [{
              label: 'No hay grupos disponibles para este período',
              value: null,
              disabled: true
            }];
          }
        }
      } catch (error) {
        console.error('Error fetching groups:', error);
        if (groupField) {
          groupField.options = [{
            label: 'No hay grupos para este período',
            value: null,
            disabled: true
          }];
        }
      }
    },

    async fetchExternalUsers() {
      const headers = { 'API-VERSION': '1' };

      const userField = this.processedFields.find(f => f.key === 'user_id');
      if (userField) {
        userField.options = [];
      }

      try {
        const users = await API.get(API.USERS_EXTERNAL, headers);

        if (userField) {
          if (users && users.length > 0) {
            userField.options = buildUserOptions(users);
            // Enable country and type fields when external users are available
            this.enableField('country');
            this.enableField('type_of_external_user');
          } else {
            userField.options = [{
              label: 'No hay aliados externos disponibles para este semillero',
              value: null,
              disabled: true
            }];
            // Disable country and type fields when no external users are available
            this.disableField('country');
            this.disableField('type_of_external_user');
          }
        }
      } catch (error) {
        console.error('Error fetching groups:', error);
        if (userField) {
          userField.options = [{
            label: 'No hay aliados externos disponibles para este semillero',
            value: null,
            disabled: true
          }];
        }
        // Disable country and type fields when there's an error
        this.disableField('country');
        this.disableField('type_of_external_user');
      }
    },

    async fetchSeedbedsByGroup(groupProfileId: string) {
      const headers = { 'API-VERSION': '1' };

      const seedbedField = this.processedFields.find(f => f.key === 'research_seedbed_profile_id');
      if (seedbedField) {
        seedbedField.options = [];
      }

      try {
        const seedbeds = await API.get(`${API.RESEARCH_SEEDBEDS_PROFILES_BY_INVESTIGATION_GROUP_PROFILE}${groupProfileId}`, headers);

        if (seedbedField) {
          if (seedbeds && seedbeds.length > 0) {
            seedbedField.options = seedbeds.map((seedbed: any) => ({
              label: seedbed.research_seedbed?.name || seedbed.name,
              value: seedbed.id,
            }));
          } else {
            seedbedField.options = [{
              label: 'No hay semilleros disponibles para este grupo',
              value: null,
              disabled: true
            }];
          }
        }
      } catch (error) {
        console.error('Error fetching seedbeds:', error);
        if (seedbedField) {
          seedbedField.options = [{
            label: 'Error al cargar semilleros',
            value: null,
            disabled: true
          }];
        }
      }
    },

    async fetchCountry() {
      const headers = { 'API-VERSION': '1' };

      const countryField = this.processedFields.find(f => f.key === 'country');
      if (countryField) {
        countryField.options = [];
      }

      try {
        const countries = await API.get(API.COUNTRIES, headers);

        if (countryField) {
          if (countries && countries.length > 0) {
            countryField.options = countries.map((country: any) => ({
              label: country,
              value: country,
            }));
          } else {
            countryField.options = [{
              label: 'No hay paises disponibles para este grupo',
              value: null,
              disabled: true
            }];
          }
        }
      } catch (error) {
        console.error('Error fetching seedbeds:', error);
        if (countryField) {
          countryField.options = [{
            label: 'Error al cargar los paises',
            value: null,
            disabled: true
          }];
        }
      }
    },

    async fetchExternalType() {
      const headers = { 'API-VERSION': '1' };
      const typeField = this.processedFields.find(f => f.key === 'type_of_external_user');
      if (typeField) typeField.options = [];
      try {
        const externalType = await API.get(API.EXTERNAL_USER_TYPES, headers);
        if (typeField) {
          if (externalType && externalType.length > 0) {
            const toUpperSnake = (str: string) => String(str)
              .trim()
              .replace(/[^A-Za-z0-9]+/g, '_')
              .replace(/^_|_$/g, '')
              .toUpperCase();
            typeField.options = externalType.map((type: any) => ({
              label: String(type),          // keep original text
              value: toUpperSnake(type),    // transformed value
            }));
          } else {
            typeField.options = [{ label: 'No hay semilleros disponibles para este grupo', value: null, disabled: true }];
          }
        }
      } catch (error) {
        console.error('Error fetching seedbeds:', error);
        if (typeField) {
          typeField.options = [{ label: 'Error al cargar semilleros', value: null, disabled: true }];
        }
      }
    },

    setUserId() {
      try {
        const userId = this.$route.params.idExternal;
        console.log('usedId: ' + userId);

        const academicPeriodId = this.$route.params.idPeriodo;
        console.log('PeriodId: ' + academicPeriodId);

        const researchSeedbedProfileId = this.$route.params.idSemillero;
        console.log('researchSeedbedProfileId: ' + researchSeedbedProfileId);


        if (userId) {
          this.additionalData = {
            ...this.additionalData,
            user_id: userId
          };
        }

        if (academicPeriodId) {
          this.additionalData = {
            ...this.additionalData,
            academic_period_id: academicPeriodId
          };
        }

        if (researchSeedbedProfileId) {
          this.additionalData = {
            ...this.additionalData,
            research_seedbed_profile_id: researchSeedbedProfileId
          };
        }

      } catch (error) {
        console.error('Error getting user ID from route:', error);
      }
    },

    resetFormToInitialState() {
      this.formData = {};

      const fieldsToDisable = ['research_group_profile_id', 'research_seedbed_profile_id'];

      fieldsToDisable.forEach(fieldKey => {
        const field = this.processedFields.find(f => f.key === fieldKey);
        if (field) {
          field.disabled = true;
          field.options = [];
        }
      });
    },
  },
});
</script>
