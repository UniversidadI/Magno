<template>
  <VNavigationDrawer
    :model-value="true"
    location="right"
    :permanent="true"
    width="60"
    class="quick-tools-drawer"
  >
    <!-- Header -->
    <div class="d-flex align-center justify-center pa-3">
      <VIcon icon="ri-apps-line" size="20" />
    </div>

    <VDivider />

    <!-- Google Tools Section -->
    <div class="py-2">
      <VList density="compact" nav class="icon-only-list">
        <VTooltip
          v-for="tool in googleTools"
          :key="tool.name"
          location="left"
        >
          <template #activator="{ props }">
            <VListItem
              :href="tool.url"
              target="_blank"
              class="tool-item justify-center"
              v-bind="props"
            >
              <template #prepend>
                <img
                  v-if="tool.logo"
                  :src="tool.logo"
                  :alt="tool.name"
                  class="tool-logo"
                />
                <VIcon v-else :icon="tool.icon" :color="tool.color" size="20" />
              </template>
            </VListItem>
          </template>
          <span>{{ tool.name }}</span>
        </VTooltip>
      </VList>
    </div>

    <VDivider class="my-2" />

    <!-- Academic & Productivity Tools Section -->
    <div class="py-2">
      <VList density="compact" nav class="icon-only-list">
        <VTooltip
          v-for="tool in academicTools"
          :key="tool.name"
          location="left"
        >
          <template #activator="{ props }">
            <VListItem
              :href="tool.url"
              target="_blank"
              class="tool-item justify-center"
              v-bind="props"
            >
              <template #prepend>
                <img
                  v-if="tool.logo"
                  :src="tool.logo"
                  :alt="tool.name"
                  class="tool-logo"
                />
                <VIcon v-else :icon="tool.icon" :color="tool.color" size="20" />
              </template>
            </VListItem>
          </template>
          <span>{{ tool.name }}</span>
        </VTooltip>
      </VList>
    </div>
  </VNavigationDrawer>
</template>

<script lang="ts" setup>
interface Tool {
  name: string
  url: string
  logo?: string
  icon?: string
  color?: string
}

const googleTools: Tool[] = [
  {
    name: 'Drive',
    url: 'https://drive.google.com',
    logo: new URL('@/assets/images/logos/google/drive.png', import.meta.url).href
  },
  {
    name: 'Gmail',
    url: 'https://mail.google.com',
    logo: new URL('@/assets/images/logos/google/gmail.png', import.meta.url).href
  },
  {
    name: 'Docs',
    url: 'https://docs.google.com',
    logo: new URL('@/assets/images/logos/google/docs.png', import.meta.url).href
  },
  {
    name: 'Sheets',
    url: 'https://sheets.google.com',
    logo: new URL('@/assets/images/logos/google/sheets.png', import.meta.url).href
  },
  {
    name: 'Slides',
    url: 'https://slides.google.com',
    logo: new URL('@/assets/images/logos/google/slides.png', import.meta.url).href
  },
  {
    name: 'Forms',
    url: 'https://forms.google.com',
    logo: new URL('@/assets/images/logos/google/forms.png', import.meta.url).href
  },
  {
    name: 'Calendar',
    url: 'https://calendar.google.com',
    logo: new URL('@/assets/images/logos/google/calendar.png', import.meta.url).href
  }
]

const academicTools: Tool[] = [
  {
    name: 'Notion',
    url: 'https://notion.so',
    logo: new URL('@/assets/images/logos/notion.png', import.meta.url).href
  },
  {
    name: 'Trello',
    url: 'https://trello.com',
    logo: new URL('@/assets/images/logos/trello.png', import.meta.url).href
  },
  {
    name: 'Mendeley',
    url: 'https://www.mendeley.com',
    logo: new URL('@/assets/images/logos/mendely.png', import.meta.url).href
  },
  {
    name: 'LanguageTool',
    url: 'https://languagetool.org',
    logo: new URL('@/assets/images/logos/language-tool.png', import.meta.url).href
  },
  {
    name: 'Scopus',
    url: 'https://www.scopus.com',
    logo: new URL('@/assets/images/logos/scopus.png', import.meta.url).href
  },
  {
    name: 'Academia.edu',
    url: 'https://www.academia.edu',
    logo: new URL('@/assets/images/logos/academy.edu.png', import.meta.url).href
  }
]
</script>

<style scoped>
.quick-tools-drawer {
  border-left: thin solid rgba(var(--v-border-color), var(--v-border-opacity));
}

.icon-only-list .v-list-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 !important;
  min-height: 40px;
}

.icon-only-list :deep(.v-list-item__prepend) {
  margin: 0 !important;
}

.icon-only-list :deep(.v-list-item__content) {
  display: none;
}

.icon-only-list :deep(.v-list-item__spacer) {
  display: none;
}

.tool-logo {
  width: 24px;
  height: 24px;
  object-fit: contain;
  display: block;
}

.tool-item {
  border-radius: 8px;
  margin: 4px 8px;
  transition: all 0.2s;
}

.tool-item:hover {
  background-color: transparent !important;
}
</style>
