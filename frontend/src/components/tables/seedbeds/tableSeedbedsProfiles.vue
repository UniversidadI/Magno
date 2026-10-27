<template>

  <VCard flat class="pa-5 my-3">
    <VCardTitle class="d-flex align-center justify-end">
      <VTextField
        v-model="search"
        density="compact"
        label="Search"
        prepend-inner-icon="ri-search-line"
        variant="outlined"
        hide-details
        single-line
      ></VTextField>
      <QuickControl
        toCreate
        toRefresh
        type="seedbed_profile"
        @itemCreated="handleItemRefresh"
        @refresh="handleItemRefresh"
      />
    </VCardTitle>
    <VDataTable
      :items="items"
      :search="search"
      :headers="headers"
    >
      <template v-slot:item.research_seedbed.line_of_research="{item}">
        <VChip>
          {{item.research_seedbed.line_of_research}}
        </VChip>
      </template>
      <template v-slot:item.tutor.user.full_name="{item}">
        <span :class="item.tutor ? '' : 'opacity-40'">
        {{ item.tutor?.user?.full_name || 'Sin tutor' }}
        </span>
      </template>

      <template v-slot:item.was_active="{item}">
        <VChip :color="item.was_active ? 'green' : ''" >
          {{ SeedbedStatus(item.was_active) }}
        </VChip>
      </template>

      <template v-slot:item.link="{item}">
        <QuickActions
          toEdit
          toDelete
          :toView="item.id + '/detalles'"
          type="seedbed_profile"
          :name="item.research_seedbed.name"
          :index="item.id"
          :initialData="setInitialData(item)"
          @itemDeleted="handleItemRefresh"
          @itemEdited="handleItemRefresh"
        ></QuickActions>
      </template>
    </VDataTable>
  </VCard>
</template>

<script lang="ts">
import { defineComponent } from "vue"

//utils
import API from "@/utils/api";
import QuickControl from "@/components/operators/quickControl.vue";
import Formatter from "@/utils/formatter";
//import Formatter from "@/utils/formatter";


interface Item {
  id: number,
  was_active: boolean,
  research_seedbed:{
    name: string
    line_of_research: Array<string>,
  },
  coordinator: {
    user: {
      full_name: string
    }
  },
  tutor: {
    user: {
      full_name: string
    }
  } | null,
}

export default defineComponent({
  components: {QuickControl},

  emits: ['loaded'],
  data() {
    return {
      items: [] as Item[],
      line: [] as string[],
      search: '',
      links: '',
      headers: [
        {title: 'Nombre', key: 'research_seedbed.name'},
        {title: 'coordinator', key: 'coordinator.user.full_name'},
        {title: 'Tutor', key: 'tutor.user.full_name'},
        {title: 'Linea de investigaión', key: 'research_seedbed.line_of_research'},
        {title: 'Estado', key: 'was_active'},
        { key: 'link', sortable: false},
      ],
    }
  },
  // ...
  created() {
    this.getGroups();

  },
  methods: {
    async getGroups() {
      const headers = {
        'API-VERSION': '1',
      }
      try {
        const seedbeds = await API.get(API.RESEARCH_SEEDBEDS_PROFILES_BY_INVESTIGATION_GROUP_PROFILE + this.$route.params.idGrupo, headers);

        for (const seedbed of seedbeds) {
          const lineOfResearch = await API.get(
            API.LINES_OF_RESEARCH_BY_RESEARCH_SEEDBED + seedbed.research_seedbed.id,
            headers
          )
          seedbed.research_seedbed.line_of_research = lineOfResearch;
        }
        this.items = seedbeds;
        this.$emit('loaded');
        //return this.lines;
      } catch (error) {
        console.error('Error fetching users:', error);
      }
    },
    SeedbedStatus(state:boolean){
      return Formatter.periodActivityFormatter(state);
    },
    handleItemRefresh() {
      this.getGroups();
    },
    setInitialData(item: any) {
      return {
        research_seedbed_id: item.research_seedbed.id,
        coordinator_id: item.coordinator.user.id,
        tutor_id: item.tutor?.user.id || null,
        academic_period_id: this.$route.params.idPeriodo,
        investigation_group_profile_id: this.$route.params.idGrupo,
        was_active: item.was_active,
      }
    }
  },
})
</script>
