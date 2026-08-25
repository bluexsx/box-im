/** 设置窗口标题提示（未读数等） */
export const setTitleTip = (tip: string) => {
  const name = import.meta.env.VITE_APP_NAME || '盒子IM';
  document.title = tip ? `(${tip})${name}` : name;
};
