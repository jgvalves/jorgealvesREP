package multicert.Application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

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
            multicert.Application.Blockchain.instatiateChaincode(client, client.getChannel("mychannel"), CHAINCODE_NAME);
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
