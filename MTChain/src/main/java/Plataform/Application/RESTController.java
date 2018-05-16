package Plataform.Application;

//@Controller
//@RequestMapping(value = "/controller")
public class RESTController {


    /*@RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public String getRequestTest(){
        //PushBuilder push = request.newPushBuilder();

        return new String("GOTTEN!");
    }*/


    /*
    public static String queryBlockchain() throws Exception {
        return Blockchain.op_query(client, client.getChannel("mychannel"), CHAINCODE_NAME, "documento".getBytes());
    }




    //@Path("/instantiate")
    //@GET
    //@RequestMapping("/instantiate")
    public void instantiateBlockchain(){//@RequestParam(value="name", defaultValue="World") String name) {

        String template = "Hello, %s!";
        String.format(template);

        /*try {
            Plataform.Application.Blockchain.instatiateChaincode(client, client.getChannel("mychannel"), CHAINCODE_NAME);
        }
        catch(Exception a){
            a.printStackTrace();
        }*/

/*}


    public static void upgradeBlockchain() throws Exception {
        Blockchain.upgradeChaincode(client, client.getChannel("mychannel"), CHAINCODE_NAME);
    }


    public static HFClient getClient() {
        return client;
    }


    public static String print(){
        return String.format("Hi %s", "hey");
    }
    */
}
