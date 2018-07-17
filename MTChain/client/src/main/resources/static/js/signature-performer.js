function Sign1()
	{
	// Check validity
	var myForm = document.getElementById('signform');
	if (myForm.checkValidity())
		{
	        Sign2();
        }
	}

function Sign2()
	{
	// Get PFX
	var form = document.getElementById("signform");
	var fileInput = document.getElementById('pfx');
	var file = fileInput.files[0];

	// Read it
	var reader = new FileReader();
	reader.onload = function(e)
		{
		var contents = e.target.result;
		var pkcs12Der = arrayBufferToString(contents)
		var pkcs12B64 = forge.util.encode64(pkcs12Der);
		var privateKey;
		var pkcs12Asn1 = forge.asn1.fromDer(pkcs12Der);
		var password = $('#pfxp').val();

		var pkcs12 = forge.pkcs12.pkcs12FromAsn1(pkcs12Asn1, false, password);
		// load keys
        for(var sci = 0; sci < pkcs12.safeContents.length; ++sci)
            {
            var safeContents = pkcs12.safeContents[sci];
            for(var sbi = 0; sbi < safeContents.safeBags.length; ++sbi)
                {
                var safeBag = safeContents.safeBags[sbi];
                if(safeBag.type === forge.pki.oids.keyBag)
                    {
                    //Found plain private key
                    privateKey = safeBag.key;


                    }
                else
                if(safeBag.type === forge.pki.oids.pkcs8ShroudedKeyBag)
                    {
                    // found encrypted private key
                    privateKey = safeBag.key;

                    }
                }
            }

            var sig = new KJUR.crypto.Signature({"alg": "SHA1withRSA"});

            //Get PrivateKey PEM
            var privateKeyP12Pem = forge.pki.privateKeyToPem(privateKey);

            //Generate RSA Private key from PEM
            var rsa = new RSAKey();
            rsa.readPrivateKeyFromPEMString(privateKeyP12Pem);

            //Value to sign, the document hash
            var dochash = document.getElementById("dochash").value;

            // rsaPrivateKey of RSAKey object
            sig.init(rsa, password);

            // update data
            sig.updateString(dochash)

            // calculate signature
            var signature = sig.sign();

            //set form hidden input and submit form
            $('#clientSignature').val(signature);
      	    document.getElementById("signform").submit();
    	}

    	reader.readAsArrayBuffer(file);
    }

function arrayBufferToString( buffer )
	{
	var binary = '';
	var bytes = new Uint8Array( buffer );
	var len = bytes.byteLength;
	for (var i = 0; i < len; i++)
		{
		binary += String.fromCharCode( bytes[ i ] );
		}
	return binary;
	}

function computeHash()
    {
        let fileSelect = document.getElementById('file')
        let files = fileSelect.files
        let file = files[0]

        var r = new FileReader();
        r.onload = function(){ console.log(r.result); };
        var reader = new FileReader();
        reader.onload = function (event) {
          var file_sha1 = sha1(event.target.result)
        };

        reader.readAsArrayBuffer(file);


    }