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
      type="seedbed"
      @itemCreated="handleItemRefresh"
      @refresh="handleItemRefresh"
    />
    </VCardTitle>
    <VDataTable
      :items="items"
      :search="search"
      :headers="headers"
    >
    <template v-slot:item.line_of_research="{item}">
        <VChip>
          {{item.line_of_research}}
        </VChip>
    </template>

    <template v-slot:item.link="{item}">
      <VBtn
        icon
        class="action view"
        flat
        color="transparent"
        @click="viewDetails(item)"
      >
        <VIcon icon="ri-eye-line" />
        <VTooltip activator="parent" location="top">
          Ver detalles
        </VTooltip>
      </VBtn>
      <QuickActions
        toEdit
        toDelete
        type="seedbed"
        :name="item.name"
        :index="item.id"
        :initialData="setInitialData(item)"
        @itemDeleted="handleItemRefresh"
        @itemEdited="handleItemRefresh"
        ></QuickActions>
    </template>
    </VDataTable>

    <!-- Detail Dialog -->
    <VDialog v-model="detailDialog" max-width="800" scrollable>
      <VCard v-if="selectedSeedbed">
        <VCardTitle class="d-flex align-center justify-space-between">
          <span>Detalles del Semillero: {{ selectedSeedbed.name }}</span>
          <VBtn icon="ri-close-line" variant="text" @click="closeDialog" />
        </VCardTitle>

        <VDivider />

        <VCardText class="pt-4">
          <VRow>
            <VCol cols="12">
              <h3 class="text-subtitle-1 mb-2">Información General</h3>
            </VCol>


            <VCol cols="12" md="6">
              <div class="text-caption text-medium-emphasis">Nombre</div>
              <div class="text-body-2">{{ selectedSeedbed.name }}</div>
            </VCol>

            <VCol cols="12" md="6">
              <div class="text-caption text-medium-emphasis">Línea de Investigación</div>
              <VChip size="small" class="mt-1">{{ selectedSeedbed.line_of_research }}</VChip>
            </VCol>

            <VCol cols="12" md="6">
              <div class="text-caption text-medium-emphasis">Fecha de Creación</div>
              <div class="text-body-2">{{ formatDate(selectedSeedbed.creation_date) }}</div>
            </VCol>

            <VCol cols="12">
              <VDivider class="my-2" />
            </VCol>

            <VCol cols="12">
              <h3 class="text-subtitle-1 mb-2">Misión</h3>
              <div class="text-body-2">{{ selectedSeedbed.mission }}</div>
            </VCol>

            <VCol cols="12">
              <h3 class="text-subtitle-1 mb-2">Visión</h3>
              <div class="text-body-2">{{ selectedSeedbed.vision }}</div>
            </VCol>

            <VCol cols="12">
              <h3 class="text-subtitle-1 mb-2">Descripción de la Propuesta de Investigación</h3>
              <div class="text-body-2">{{ selectedSeedbed.research_proposal_description }}</div>
            </VCol>
          </VRow>
        </VCardText>

        <VDivider />

        <VCardActions>
          <VSpacer />
          <VBtn color="primary" @click="closeDialog">Cerrar</VBtn>
        </VCardActions>
      </VCard>
    </VDialog>
  </VCard>
</template>

<script lang="ts">
import { defineComponent } from "vue"

//utils
import API from "@/utils/api";
import Formatter from "@/utils/formatter";
import QuickControl from "@/components/operators/quickControl.vue";

interface Item {
  id: number,
  name: string,
  mission: string,
  vision: string,
  research_proposal_description:string
  creation_date: Date,
  line_of_research: string
}

export default defineComponent({
  components: {QuickControl},

  data() {
    return {
      items: [] as Item[],
      line: [] as string[],
      search: '',
      detailDialog: false,
      selectedSeedbed: null as Item | null,
      headers: [
        {title: 'Nombre', key: 'name'},
        {title: 'Linea de investigación', key: 'line_of_research'},
        {title: 'Fecha de creación', key: 'creation_date'},
        { key: 'link', sortable: false},
      ]
    }
  },
  // ...
  created() {
    this.getSeedbeds();
    //this.externalFormatter();
  },
  methods: {
    async getSeedbeds() {
      const headers={
        'API-VERSION': '1',
      }
      try {
        const seedbeds = await API.get(API.RESEARCH_SEEDBEDS, headers);

        for (const seedbed of seedbeds) {
        const lineOfResearch = await API.get(
          API.LINES_OF_RESEARCH_BY_RESEARCH_SEEDBED + seedbed.id,
          headers
        )
        seedbed.line_of_research = lineOfResearch;
        }
        this.items = seedbeds;
        this.$emit('loaded');
        //return this.lines;
      } catch (error) {
        console.error('Error fetching users:', error);
      }
    },
    externalFormatter(state: boolean){
      return Formatter.externalFormatter(state)
    },
    handleItemRefresh(){
      this.getSeedbeds();
    },
    setInitialData(item: any) {
      return {
        name: item.name,
        mission: item.mission,
        vision: item.vision,
        research_proposal_description: item.research_proposal_description,
        creation_date: item.creation_date,
        line_of_research: item.line_of_research,
      }
    },
    viewDetails(item: Item) {
      this.selectedSeedbed = item;
      this.detailDialog = true;
    },
    closeDialog() {
      this.detailDialog = false;
      this.selectedSeedbed = null;
    },
    formatDate(date: Date | string) {
      return date ? new Date(date).toLocaleDateString() : 'N/A';
    }
  },
})


</script>



