export const html2Escape = (strText?: string | null) => {
  if (!strText) {
    return '';
  }
  return strText.replace(/[<>&"]/g, (c) => {
    return (
      {
        '<': '&lt;',
        '>': '&gt;',
        '&': '&amp;',
        '"': '&quot;'
      }[c] || c
    );
  });
};
