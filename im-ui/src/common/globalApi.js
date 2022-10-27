import globalVariable from './globalInfo';

function appendToken(url){
	console.log(url);
	if(url.indexOf('?')==-1){
		url+="?"
	}
	url += `access_token=${globalVariable.token}`;
	return url;
}


export default{
	appendToken
}