import { defineStore } from 'pinia';
import { ref } from 'vue';
import { loadConfig as loadSystemConfig } from '@/api/system';
import { getThemeAccent, setThemeAccent as persistThemeAccent } from '@/utils/theme';

export const useConfigStore = defineStore('config', () => {
  const fullScreen = ref(true);
  const themeAccent = ref(getThemeAccent());
  const webrtc = ref<Record<string, unknown>>({});
  const setFullScreen = (value: boolean) => {
    fullScreen.value = value;
  };

  const setThemeAccent = (color: string) => {
    themeAccent.value = persistThemeAccent(color);
    return themeAccent.value;
  };

  const loadConfig = async () => {
    const config = await loadSystemConfig();
    webrtc.value = config.webrtc || {};
  };
  return {
    fullScreen,
    themeAccent,
    webrtc,
    setFullScreen,
    setThemeAccent,
    loadConfig
  };
});
