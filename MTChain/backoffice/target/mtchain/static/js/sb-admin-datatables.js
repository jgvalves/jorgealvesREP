// Call the dataTables jQuery plugin
//var dataSet =

$(document).ready(function() {
	$('#userstable').DataTable({
	    'language' : {
                            'infoEmpty': 'No records',
                            'zeroRecords': 'No Users added'
                        },
		'ajax' : 'lists/users',
		'serverSide' : true,
		columns : [
		    {
		        data : 'name'
		    },
		    {
		        data : 'app'
		    },
		    {
		        data : 'certdate'
		    },
		    {
		        data : 'state'
		    },
		    {
		        data : 'lastlogin'
		    },
		    {
		        data : 'edit'
		    }
		]
	});

	$('#chaincodetable').DataTable({
	        'language' : {
                        'infoEmpty': 'No records',
                        'zeroRecords': 'Chaincode is not installed'
                    },
    		'ajax' : 'lists/chaincode',
    		'serverSide' : true,

    		columns : [
    		    {
    		        data : 'name'
    		    },
    		    {
    		        data : 'currentVersion'
    		    },
    		    {
    		        data : 'chaincodeVersions'
    		    },
    		    {
    		        data : 'edit'
    		    }
    		]
    	});

    $('#availabletable').DataTable({
                'language' : {
                            'infoEmpty': 'No records',
                            'zeroRecords': 'No chaincode files are available'
                        },
        		'ajax' : 'lists/notchaincode',
        		'serverSide' : true,
        		columns : [
        		    {
        		        data : 'name'
        		    },
        		    {
        		        data : 'installed'
        		    },
        		    {
        		        data : 'install'
        		    }
        		]
        	});

    $('#apptable').DataTable({
                    'language' : {
                                'infoEmpty': 'No App',
                                'zeroRecords': 'No apps installed'
                            },
                    'ajax' : 'lists/applications',
                    'serverSide' : true,
                    columns : [
                        {
                            data : 'name'
                        },
                        {
                            data : 'chaincode'
                        }
                    ]
                });

    $('#runningnodes').DataTable({
            'language' : {
                                'infoEmpty': 'No records',
                                'zeroRecords': 'No Users added'
                            }
    });
});