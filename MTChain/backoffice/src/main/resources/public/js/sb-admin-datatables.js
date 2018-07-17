// Call the dataTables jQuery plugin
//var dataSet =

$(document).ready(function() {
	$('#userstable').DataTable({
	    'language' : {
                            'infoEmpty': 'No records',
                            'zeroRecords': 'No Users added'
                        },
		'ajax' : '/mtchain/admin/lists/users',
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
    		'ajax' : '/mtchain/admin/lists/chaincode',
    		'serverSide' : true,

    		columns : [
    		    {
    		        data : 'name'
    		    },
    		    {
    		        data : 'currentversion'
    		    },
    		    {
    		        data : 'avalversions'
    		    },
    		    {
    		        data : 'edit'
    		    }
    		]
    	});

    $('#availabletable').DataTable({
                'language' : {
                            'infoEmpty': 'No records',
                            'zeroRecords': 'No chaincode files found'
                        },
        		'ajax' : '/mtchain/admin/lists/notchaincode',
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

    $('#runningnodes').DataTable({
            'language' : {
                                'infoEmpty': 'No records',
                                'zeroRecords': 'No Nodes Running'
                            }
    });
});