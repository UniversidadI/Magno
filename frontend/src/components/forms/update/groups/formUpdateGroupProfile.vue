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
  emits: ['itemEdited'],
  data() {
    return {
      additionalData: {},
      loaded: false,
      options: [],
    };
  },
  async created() {
    await this.fetchFunctionaries()
    await this.fetchGroups()
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

        const functionaryField = this.fields.find(f => f.key === 'coordinator_id')
        if (functionaryField) {
          functionaryField.options = buildUserOptions(functionaries);
        }
      } catch (error) {
        console.error('Error fetching users:', error);
      }
    },
    async fetchGroups() {
      const headers = {
        'API-VERSION': '1',
      };
      try {
        const groups = await API.get(API.INVESTIGATION_GROUPS, headers);
        console.log("Hola obtuve los grupos")
        console.log(groups);

        const groupField = this.fields.find(f => f.key === 'investigation_group_id')
        if (groupField) {
          groupField.options = groups.map((group: any) => ({
            label: group.name,
            value: group.id,
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
  },
});
</script>
