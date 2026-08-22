/** accessToken是否已过期 */
export const isAccessTokenExpired = (token) => {
	try {
		const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
		if (!payload.exp) {
			return true;
		}
		return Date.now() >= payload.exp * 1000;
	} catch {
		return true;
	}
}
