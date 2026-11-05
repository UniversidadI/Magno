<template>
  <formUpdateGeneral
    v-if="loaded"
    :type="type"
    :fields="fields"
    :index="index"
    :initialData="initialData"
    @itemEdited="handleItemEdited"
  />
</template>

<script lang="ts">
// ...existing code...
import { defineComponent } from 'vue';
import API from "@/utils/api";
import { buildUserOptions } from '@/utils/userOptions';

export default defineComponent({
  name: 'formUpdateGroup',
  emits: ['itemEdited', 'loaded'],
  data() {
    return {
      additionalData: {},
      loaded: false,
      options: [],
    };
  },
  async created() {
    await this.fetchFunctionaries()
    await this.fetchSeedbeds()
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
    // ...existing code...
    handleItemEdited() {
      this.$emit('itemEdited');
    },

    async fetchFunctionaries() {
      const headers = {
        'API-VERSION': '1',
      };
      try {
        const functionaries = await API.get(API.USERS_FUNCTIONARY, headers);
        console.log("Hola obtuve los funcionarios")
        console.log(functionaries);

        // Create the options list once
        const functionaryOptions = buildUserOptions(functionaries);

        // Update all matching fields, not just the first one
        this.fields.forEach(field => {
          if (field.key === 'coordinator_id' || field.key === 'tutor_id') {
            field.options = functionaryOptions;
          }
        });
      } catch (error) {
        console.error('Error fetching users:', error);
      }
    },

    async fetchSeedbeds() {
      const headers = {
        'API-VERSION': '1',
      };
      try {
        const seedbeds = await API.get(API.RESEARCH_SEEDBEDS, headers);
        console.log("Hola obtuve los semilleros")
        console.log(seedbeds);

        const groupField = this.fields.find(f => f.key === 'research_seedbed_id')
        if (groupField) {
          groupField.options = seedbeds.map((seedbed: any) => ({
            label: seedbed.name,
            value: seedbed.id,
          }));
        }
      } catch (error) {
        console.error('Error fetching users:', error);
      }
    },

    getPeriodId() {
      try {
        const periodId = this.$route.params.idPeriodo;
        if (periodId) {
          // Agregar el userId al objeto additionalData que se pasará al formulario
          this.additionalData = {
            ...this.additionalData,
            academic_period_id: periodId
          };
          console.log("ID de funcionario obtenido:", periodId);
        }
      } catch (error) {
        console.error('Error getting user ID from route:', error);
      }
    },

    getGroupId() {
      try {
        const groupId = this.$route.params.idGrupo;
        if (groupId) {
          // Agregar el userId al objeto additionalData que se pasará al formulario
          this.additionalData = {
            ...this.additionalData,
            investigation_group_profile_id: groupId
          };
          console.log("ID de grupo:", groupId);
        }
      } catch (error) {
        console.error('Error getting user ID from route:', error);
      }
    },
  },
});
</script>
