package main

import (
    "fmt"
    "encoding/json"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"bytes"
	sc "github.com/hyperledger/fabric/protos/peer"

)

type smartcontract struct {
}


type op_start struct {
	Operation string `json:"operation"`
	ID string `json:"ID"`
	Signer string `json:"signer"`
	Hash string `json:"hash"`
	Time string `json:"time"`
}

type op_sign struct {
	Operation string `json:"operation"`
	ID string `json:"ID"`
	Signer string `json:"signer"`
	Hash string `json:"hash"`
	Time string `json:"time"`
}

type op_end struct {
	Operation string `json:"operation"`
	ID string `json:"ID"`
	Signer string `json:"signer"`
	Hash string `json:"hash"`
	Time string `json:"time"`
}

func (s *smartcontract) Init(APIstub shim.ChaincodeStubInterface) sc.Response {

	return shim.Success(nil)
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


    //Receives UID
	empAsBytes, err := APIstub.GetState(args[0])

    if err != nil { return shim.Error(err.Error())}

	buffer.WriteString("]")
	return shim.Success(empAsBytes)

}


//op_start(op_start, signature, docName)
func (s *smartcontract) op_start(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	var start = op_start{Operation: "op_start", ID: args[0], Signer: args[1], Hash: args[2], Time: args[3]}

	operationBytes, _ := json.Marshal(start)
	APIstub.PutState(args[0], operationBytes)

	return shim.Success(nil)

}

func (s *smartcontract) op_sign(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	var start = op_sign{Operation: "op_sign", ID: args[0], Signer: args[1], Hash: args[2], Time: args[3]}

	operationBytes, _ := json.Marshal(start)
	APIstub.PutState(args[0], operationBytes)

	return shim.Success(nil)

}

func (s *smartcontract) op_end(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	var start = op_end{Operation: "op_end", ID: args[0], Signer: args[1], Hash: args[2],  Time: args[3]}

	operationBytes, _ := json.Marshal(start)
	APIstub.PutState(args[0], operationBytes)

	return shim.Success(nil)

}

func main() {

	// Create a new Smart Contract
        err := shim.Start(new(smartcontract))
	if err != nil {
		fmt.Printf("Error creating new Smart Contract: %s", err)
	}
}