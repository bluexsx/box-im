export const isIndexedDBAvailable = () => {
  return new Promise<boolean>((resolve) => {
    if (!window.indexedDB) {
      resolve(false);
      return;
    }
    const request = window.indexedDB.open('__test_db__', 1);
    request.onsuccess = () => {
      resolve(true);
      request.result.close();
      window.indexedDB.deleteDatabase('__test_db__');
    };
    request.onerror = () => resolve(false);
    // 设置超时，防止 Safari 静默失败导致无限等待
    setTimeout(() => resolve(false), 1000);
  });
};
