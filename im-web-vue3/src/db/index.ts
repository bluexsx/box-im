import { isIndexedDBAvailable } from '@/utils/browser';
import ImMemoryDB from './memoryDb';
import ImIndexedDB from './indexedDb';

export type ImDB = ImMemoryDB | ImIndexedDB;

let dbInstance: ImDB | null = null;

// 单例
export const initDB = async () => {
  if (dbInstance) {
    return dbInstance;
  }
  const canUseIDB = await isIndexedDBAvailable();
  dbInstance = canUseIDB ? new ImIndexedDB() : new ImMemoryDB();
  return dbInstance;
};

// 同步获取（必须保证 initDB 已经执行过）
export const getDB = () => {
  if (!dbInstance) {
    throw new Error('DB 还未初始化，请先调用 await initDB()');
  }
  return dbInstance;
};
