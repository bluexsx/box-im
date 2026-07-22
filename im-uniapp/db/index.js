import { isIndexedDBAvailable } from '@/common/browser'
import ImStorageDB from './storageDb'
import ImIndexedDB from './indexedDb'
import ImSqliteDB from './sqliteDb'


// 单例
let dbInstance = null

export async function initDB() {
	if (dbInstance) {
		return dbInstance;
	}
	// #ifdef APP
	dbInstance = new ImSqliteDB();
	console.log("本地数据库:", "SQLite")
	return dbInstance
	// #endif
	// #ifdef H5
	if (await isIndexedDBAvailable()) {
		dbInstance = new ImIndexedDB();
		console.log("本地数据库:", "IndexedDB")
		return dbInstance;
	}
	// #endif
	dbInstance = new ImStorageDB();
	console.log("本地数据库:", "ImStorageDB")
	return dbInstance
}

// 同步获取（必须保证 initDB 已经执行过）
export function getDB() {
	if (!dbInstance) {
		throw new Error('DB 还未初始化，请先调用 await initDB()')
	}
	return dbInstance
}

export default {
	initDB,
	getDB
}