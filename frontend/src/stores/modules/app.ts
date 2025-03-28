import { HBlue } from "@/constants";
import { adaptedFileIcons, adaptedThemeIcons } from '@/constants/fileIcon';
function appStore() {
  const isDark = ref<boolean>(false);
  const toggleTheme = () => {
    if (isDark.value) {
      document.body.removeAttribute('arco-theme');
      isDark.value = false;
    } else {
      document.body.setAttribute('arco-theme', 'dark');
      isDark.value = true;
    }
  }
  const textColor = computed(() => isDark.value ? '#fff' : '#17171A')
  const bgColor = computed(() => isDark.value ? '#17171A' : '#fff')
  const iconColor = computed(() => isDark.value ? '#fff' : HBlue)
  // 左侧菜单激活的索引
  const menuIdx = ref(0);
  // 左侧二级菜单（如果有）激活的索引
  const subMenuIdx = ref(0);
  // 文件icon
  const FileIcons = computed(() => adaptedFileIcons(isDark.value))
  // 其他icon
  const Icons = computed(() => adaptedThemeIcons(isDark.value))


  return { isDark, toggleTheme, menuIdx, subMenuIdx, textColor, bgColor, iconColor, FileIcons, Icons };
}

export const useAppStore = defineStore('appStore', appStore, {
  persist: true
});
