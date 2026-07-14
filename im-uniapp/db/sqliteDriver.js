// #ifdef APP-PLUS
export function openDatabase(name, path) {
	return new Promise((resolve, reject) => {
		plus.sqlite.openDatabase({
			name,
			path,
			success: () => resolve(),
			fail: (error) => reject(error)
		})
	})
}

export function isOpenDatabase(name, path) {
	return plus.sqlite.isOpenDatabase({ name, path })
}

export function closeDatabase(name) {
	return new Promise((resolve) => {
		plus.sqlite.closeDatabase({
			name,
			success: () => resolve(),
			fail: () => resolve()
		})
	})
}

export function selectSql(name, sql) {
	return new Promise((resolve, reject) => {
		plus.sqlite.selectSql({
			name,
			sql,
			success: (rows) => resolve(rows || []),
			fail: (error) => reject(error)
		})
	})
}

export function executeSql(name, sql) {
	return new Promise((resolve, reject) => {
		plus.sqlite.executeSql({
			name,
			sql,
			success: () => resolve(),
			fail: (error) => reject(error)
		})
	})
}
// #endif

// #ifdef APP-HARMONY
import {
	openDatabase as harmonyOpenDatabase,
	isOpenDatabase as harmonyIsOpenDatabase,
	closeDatabase as harmonyCloseDatabase,
	selectSql as harmonySelectSql,
	executeSql as harmonyExecuteSql
} from '@/uni_modules/harmony-sqlite'

export function openDatabase(name, path) {
	return harmonyOpenDatabase(name)
}

export function isOpenDatabase(name, path) {
	return harmonyIsOpenDatabase(name)
}

export function closeDatabase(name) {
	return harmonyCloseDatabase(name)
}

export function selectSql(name, sql) {
	return harmonySelectSql(name, sql)
}

export function executeSql(name, sql) {
	return harmonyExecuteSql(name, sql)
}
// #endif
