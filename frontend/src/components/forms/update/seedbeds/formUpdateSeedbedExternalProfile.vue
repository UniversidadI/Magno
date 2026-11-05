<template>
  <formUpdateGeneral
    v-if="loaded"
    :type="type"
    :fields="processedFields"
    :index="index"
    :initialData="processedInitialData"
    :additionalData="additionalData"
    @itemEdited="handleItemEdited"
    @fieldChanged="handleFieldChange"
  />
</template>

<script lang="ts">
// ...existing code...
import { defineComponent } from 'vue';
import API from "@/utils/api";
import { buildUserOptions } from '@/utils/userOptions';

export default defineComponent({
  name: 'formUpdateSeedbedExternalProfile',
  emits: ['itemEdited', 'loaded'],
  data() {
    return {
      additionalData: {} as Record<string, any>,
      loaded: false,
      options: [],
      processedFields: [] as any[],
    };
  },
  computed: {
    processedInitialData() {
      // Create a copy of initialData
      const data = { ...this.initialData };

      // Flatten user data to match form field keys (user_id)
      if (data.user && !data.user_id) {
        data.user_id = data.user.id;
      }

      // Flatten seedbed profile identification if needed
      if (data.research_seedbed_profile && !data.research_seedbed_profile_id) {
        data.research_seedbed_profile_id = data.research_seedbed_profile.id;
      }

      // Extract investigation group profile ID for fetching dependent fields
      if (data.research_seedbed_profile && data.research_seedbed_profile.investigation_group_profile_id && !data.investigation_group_profile_id) {
        data.investigation_group_profile_id = data.research_seedbed_profile.investigation_group_profile_id;
      }

      // Extract academic period ID if needed
      if (data.academic_period && !data.academic_period_id) {
        data.academic_period_id = data.academic_period.id;
      }

      return data;
    }
  },
  async created() {
    this.processedFields = JSON.parse(JSON.stringify(this.fields));
    this.getRouteParams();

    await this.fetchPeriods();
    await this.fetchCountry()
    await this.fetchExternalType()
    await this.fetchExternalUsers()

    // Use processed data for initial dependent fetches
    const periodId = this.processedInitialData.academic_period_id || this.additionalData.academic_period_id;
    if (periodId) {
      await this.handlePeriodChange(periodId);
    }

    const groupId = this.processedInitialData.investigation_group_profile_id || this.additionalData.investigation_group_profile_id;
    if (groupId) {
      await this.fetchSeedbedsByGroup(groupId)
    }

    this.$emit('loaded')

    this.loaded = true
  },
  props:{
    fields:{
      type: Array as () => Array<{ key: string; label: string; type?: string; options?: Array<{ label: string; value: string }> }>,
      default: () => [],
    },
    type:{
      type: String,
    },
    index: {
      type: Number,
    },
    initialData: {
      type: Object,
      default: () => ({}),
    },
  },
  methods: {
    handleItemEdited() {
      this.$emit('itemEdited');
    },

    getRouteParams() {
      try {
        if (this.$route.params.idExternal) this.additionalData.user_id = this.$route.params.idExternal;
        if (this.$route.params.idPeriodo) this.additionalData.academic_period_id = this.$route.params.idPeriodo;
        if (this.$route.params.idGrupo) this.additionalData.investigation_group_profile_id = this.$route.params.idGrupo;
        if (this.$route.params.idSemillero) this.additionalData.research_seedbed_profile_id = this.$route.params.idSemillero;
      } catch (error) {
        console.error('Error getting params from route:', error);
      }
    },

    async handleFieldChange(fieldKey: string, value: any) {
      if (fieldKey === 'academic_period_id') {
        await this.handlePeriodChange(value);
      } else if (fieldKey === 'investigation_group_profile_id') {
        await this.handleGroupChange(value);
      }
    },

    async handlePeriodChange(periodId: string) {
      if (!periodId) {
        this.resetDependentFields(['investigation_group_profile_id', 'research_seedbed_profile_id']);
        return;
      }

      try {
        this.enableField('investigation_group_profile_id');
        await this.fetchGroupsByPeriod(periodId);
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
        this.enableField('research_seedbed_profile_id');
        await this.fetchSeedbedsByGroup(groupId);
      } catch (error) {
        console.error('Error loading seedbeds:', error);
      }
    },

    resetDependentFields(fieldKeys: string[]) {
      fieldKeys.forEach(key => {
        const field = this.processedFields.find(f => f.key === key);
        if (field) {
          field.disabled = true;
          field.options = [];
          this.additionalData[key] = null;
          // Note: formUpdateGeneral handles the value reset via prop watch
        }
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
            periodField.options = [{ label: 'No hay periodos disponibles', value: null, disabled: true }];
          }
        }
      } catch (error) {
        console.error('Error fetching periods:', error);
      }
    },

    async fetchGroupsByPeriod(periodId: string) {
      const headers = { 'API-VERSION': '1' };
      const groupField = this.processedFields.find(f => f.key === 'investigation_group_profile_id');
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
            groupField.options = [{ label: 'No hay grupos disponibles', value: null, disabled: true }];
          }
        }
      } catch (error) {
        console.error('Error fetching groups:', error);
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
        console.error('Error fetching external users:', error);
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
        this.$forceUpdate();
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
              label: 'No hay países disponibles',
              value: null,
              disabled: true
            }];
          }
        }
      } catch (error) {
        console.error('Error fetching countries:', error);
        if (countryField) {
          countryField.options = [{
            label: 'Error al cargar los países',
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
            typeField.options = [{ label: 'No hay tipos de usuario disponibles', value: null, disabled: true }];
          }
        }
      } catch (error) {
        console.error('Error fetching external types:', error);
        if (typeField) {
          typeField.options = [{ label: 'Error al cargar tipos de usuario', value: null, disabled: true }];
        }
      }
    },
  },
});
</script>
