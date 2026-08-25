/** 主色调：存 #hex */
export const themeStorageKey = 'im-primary-color';
export const themeDefaultColor = '#2830d3';

export const themeAccents = [
  { id: 'default', name: '经典蓝', color: '#2830d3' },
  { id: 'violet', name: '紫罗兰', color: '#6d28d9' },
  { id: 'teal', name: '青绿', color: '#0f766e' },
  { id: 'green', name: '翡翠绿', color: '#15803d' },
  { id: 'amber', name: '琥珀', color: '#c2410c' },
  { id: 'rose', name: '珊瑚红', color: '#be123c' }
] as const;

const isHex = (value: unknown): value is string => {
  return typeof value === 'string' && /^#[0-9a-fA-F]{6}$/.test(value);
};

const clampByte = (n: number) => Math.max(0, Math.min(255, Math.round(n)));

const hexToRgb = (hex: string) => ({
  r: parseInt(hex.slice(1, 3), 16),
  g: parseInt(hex.slice(3, 5), 16),
  b: parseInt(hex.slice(5, 7), 16)
});

const rgbToHex = ({ r, g, b }: { r: number; g: number; b: number }) => {
  const to = (n: number) => clampByte(n).toString(16).padStart(2, '0');
  return `#${to(r)}${to(g)}${to(b)}`;
};

/** 等同 Sass mix(#fff, primary, weight%)：weight 为白色占比 */
const mixWhite = (primary: string, weight: number) => {
  const w = weight / 100;
  const a = hexToRgb(primary);
  return rgbToHex({
    r: a.r * (1 - w) + 255 * w,
    g: a.g * (1 - w) + 255 * w,
    b: a.b * (1 - w) + 255 * w
  });
};

const mixBlack = (primary: string, weight: number) => {
  const w = weight / 100;
  const a = hexToRgb(primary);
  return rgbToHex({
    r: a.r * (1 - w),
    g: a.g * (1 - w),
    b: a.b * (1 - w)
  });
};

export const getThemeAccent = () => {
  const color = localStorage.getItem(themeStorageKey);
  return isHex(color) ? color.toLowerCase() : themeDefaultColor;
};

export const setThemeAccent = (color: string) => {
  const next = isHex(color) ? color.toLowerCase() : themeDefaultColor;
  localStorage.setItem(themeStorageKey, next);
  applyThemeAccent(next);
  return next;
};

export const applyThemeAccent = (color: string) => {
  const primary = isHex(color) ? color.toLowerCase() : themeDefaultColor;
  const root = document.documentElement;
  const { r, g, b } = hexToRgb(primary);
  root.style.setProperty('--im-color-primary', primary);
  root.style.setProperty('--im-color-primary-rgb', `${r}, ${g}, ${b}`);
  root.style.setProperty('--el-color-primary', primary);
  for (let i = 1; i <= 9; i++) {
    const light = mixWhite(primary, i * 10);
    root.style.setProperty(`--im-color-primary-light-${i}`, light);
  }
  for (let i = 1; i <= 4; i++) {
    root.style.setProperty(`--im-color-primary-dark-${i}`, mixBlack(primary, i * 10));
  }
  root.style.setProperty('--el-color-primary-light-3', mixWhite(primary, 30));
  root.style.setProperty('--el-color-primary-light-5', mixWhite(primary, 50));
  root.style.setProperty('--el-color-primary-light-7', mixWhite(primary, 70));
  root.style.setProperty('--el-color-primary-light-8', mixWhite(primary, 80));
  root.style.setProperty('--el-color-primary-light-9', mixWhite(primary, 90));
  root.style.setProperty('--im-background-active', mixWhite(primary, 95));
  root.style.setProperty('--im-background-active-dark', mixWhite(primary, 90));
  return primary;
};
