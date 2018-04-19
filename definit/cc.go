package main

import (
	"fmt"
	"encoding/json"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	sc "github.com/hyperledger/fabric/protos/peer"
)

type smart struct {
}

type Multicert struct {
	Emp		string	`json:"emp"`
	Name	string	`json:"name"`
	Post	string	`json:"post"`
	Hash	string	`json:"hash"`
}

/*
 *	JSON Example
 *	
 *	Multicert:
 *		Emp: 	1
 *		Name:	Leo
 *		Post:	Estag
 *		Hash:	f5db7365038108f39f4da7c965348cd91f15bb2b
 *
 *
 *		Hash = hash("1,1,Leo,Estag", "SHA1");
 */


func (s *smart) Init(APIstub shim.ChaincodeStubInterface) sc.Response {
	return shim.Success(nil)
}



func (s *smart) Invoke(APIstub shim.ChaincodeStubInterface) sc.Response {
	
	f, args := APIstub.GetFunctionAndParameters()

	if f == "queryEmp" {
		return s.queryEmp(APIstub, args)
	} else if f == "newEmp" {
		return s.newEmp(APIstub, args)
	}

	return shim.Error("Invalid f.")
}

func (s *smart) newEmp (APIstub shim.ChaincodeStubInterface, args []string) sc.Response {
	if len(args) != 5 {
		return shim.Error("Bad args input")
	}

	var newEmp = Multicert{Emp: args[1], 
		Name: args[2], 
		Post: args[3], 
		Hash: args[4]}

	empAsBytes, _ := json.Marshal(newEmp)
	APIstub.PutState(args[0], empAsBytes)

	return shim.Success(nil)
}

func (s *smart) queryEmp (APIstub shim.ChaincodeStubInterface, args []string) sc.Response {
	if len(args) != 2 {
		return shim.Error("Bad args input")
	}

	empAsBytes, _ := APIstub.GetState(args[1]);
	return shim.Success(empAsBytes);
}

func main() {
	err := shim.Start(new(smart))
	if err != nil {
		fmt.Printf("Error starting Simple chaincode: %s", err)
	}
}
