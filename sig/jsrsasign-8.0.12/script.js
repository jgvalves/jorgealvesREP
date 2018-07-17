
function verifySignature(pemCert, signedData, message){
	
	var pk = KEYUTIL.getKey(pemCert);

	ver.init(pk);
	ver.updateString(message);
	return ver.verify(signedData);

}