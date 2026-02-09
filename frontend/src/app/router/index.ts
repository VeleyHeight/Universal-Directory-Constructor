import { createRouter, createWebHistory } from 'vue-router'

import DirectoriesListPage from '@/pages/DirectoriesListPage.vue'
import DirectoriesStartPage from "@/pages/DirectoriesStartPage.vue";
import {useDirectoryStore} from "@/entities/directory/model/store.ts";

const routes = [
    { path: '/', redirect: '/directories' },
    { path: '/directories', name: 'directories', component: DirectoriesStartPage,
        beforeEnter: async () => {
        const store = useDirectoryStore()
        await store.ensureLoaded()
        }
    },
    { path: '/directories/:id', name: 'directory-editor', component: DirectoriesListPage,
        beforeEnter: async () => {
            const store = useDirectoryStore()
            await store.ensureLoaded()
        }
    },
]

export default createRouter({
    history: createWebHistory(),
    routes,
})