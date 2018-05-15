package chaincode

import (
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"bytes"
	sc "github.com/hyperledger/fabric/protos/peer"
	"encoding/json"
	"fmt"
)

type smartcontract struct {
}


type op_start struct {
	Operation string `json:"operation"`
	Signature string `json:"signature"`
	Document string `json:"docName"`
}

type op_sign struct {
	Operation string `json:"Operation"`
	Hash string `json:"hash"`
	Document string `json:"docName"`
}

type op_end struct {
	Operation string `json:"operation"`
	Signature string `json:"signature"`
	Document string `json:"docName"`
}

func (s *smartcontract) Init(APIstub shim.ChaincodeStubInterface) sc.Response {

	var start = op_start{Operation: "op_start", Signature: "assinatura", Document: "documento"}

	operationBytes, _ := json.Marshal(start)
	APIstub.PutState("documento", operationBytes)

	return shim.Success(operationBytes)
}

func (s *smartcontract) Invoke(APIstub shim.ChaincodeStubInterface) sc.Response {

	f, args := APIstub.GetFunctionAndParameters()

	if f == "op_query" {
		return s.query(APIstub, args)
	} else if f == "op_start" {
		return s.op_start(APIstub, args)
	} else if f == "op_sign"{
		return s.op_sign(APIstub, args)
	} else if f == "op_end" {
		return s.op_end(APIstub, args)
	}

	return shim.Error("Function called not found!")
}

func (s *smartcontract) query(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {
	var buffer bytes.Buffer

	empAsBytes, err := APIstub.GetQueryResult(args[0])

	if err != nil { return shim.Error(err.Error())}

	buffer.WriteString("[")

	for empAsBytes.HasNext() {
		historyResult, err := empAsBytes.Next()
		if err != nil {
			return shim.Error(err.Error())
		}

		buffer.Write(historyResult.Value)
		buffer.WriteString("\n")
	}

	buffer.WriteString("]")
	return shim.Success(buffer.Bytes())

}


//op_start(op_start, signature, docName)
func (s *smartcontract) op_start(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	var start = op_start{Operation: "op_start", Signature: args[0], Document: args[1]}

	operationBytes, _ := json.Marshal(start)
	APIstub.PutState(args[1], operationBytes)

	return shim.Success(nil)

}

func (s *smartcontract) op_sign(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	var start = op_start{Operation: "op_start", Signature: args[0], Document: args[1]}

	operationBytes, _ := json.Marshal(start)
	APIstub.PutState(args[1], operationBytes)

	return shim.Success(nil)

}

func (s *smartcontract) op_end(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	var start = op_start{Operation: "op_start", Signature: args[0], Document: args[1]}

	operationBytes, _ := json.Marshal(start)
	APIstub.PutState(args[1], operationBytes)

	return shim.Success(nil)

}

func main() {

	// Create a new Smart Contract
	err := shim.Start(new(smartcontract))
	if err != nil {
		fmt.Printf("Error creating new Smart Contract: %s", err)
	}
}