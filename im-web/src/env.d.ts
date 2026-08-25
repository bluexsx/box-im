/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue';
  const component: DefineComponent<object, object, unknown>;
  export default component;
}

interface ImportMetaEnv {
  readonly VITE_APP_NAME: string;
  readonly VITE_APP_BASE_API: string;
  readonly VITE_APP_WS_URL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
