/*
    My attempt to separate entities: this is the verifier V.
    Load verification key and proof from files.
    Also load primary input into the protoboard.
    Finally verify proof.
*/


#include "CircuitReader.hpp"
#include <libsnark/gadgetlib2/examples/simple_example.hpp>
#include <libsnark/gadgetlib2/adapters.hpp>
#include <libsnark/gadgetlib2/gadget.hpp>
#include <libsnark/gadgetlib2/integration.hpp>

#include <libsnark/common/default_types/r1cs_ppzksnark_pp.hpp>
#include <libsnark/zk_proof_systems/ppzksnark/r1cs_ppzksnark/r1cs_ppzksnark.hpp>
// #include "libsnark/relations/constraint_satisfaction_problems/r1cs/examples/r1cs_examples.hpp"

#include <fstream>
#include <type_traits>

#include <string.h>

using namespace std;


int main(/* int argc, char **argv */)
{
    // No arguments on the command line: verifier does not need .arith and .in files

    libff::start_profiling();
	gadgetlib2::initPublicParamsFromDefaultPp();
	gadgetlib2::GadgetLibAdapter::resetVariableIndex();
	ProtoboardPtr pb = gadgetlib2::Protoboard::create(gadgetlib2::R1P);


    // Lodaing veryfing key from file
    std::ifstream in("VK_export");
    r1cs_ppzksnark_keypair<libsnark::default_r1cs_ppzksnark_pp> keypair;
    in >> keypair.vk;
    in.close();


    // Loading proof from file
    ifstream proofFile("proof_export");
    r1cs_ppzksnark_proof<libsnark::default_r1cs_ppzksnark_pp> proof;
    proofFile >> proof;
    proofFile.close();


    // Loading the input file from file
    {
        // r1cs_primary_input<libff::Fr<libsnark::default_r1cs_ppzksnark_pp>> primary_input;
        // ifstream inputstream("stmt_export");
        // // std::stringstream ss3;
        // // ss3 << argv[3];
        // string line;
        // getline(inputstream, line);
        // int num = std::stoi(line);
        // cout << num << "\n";

        // for(int i = 0; i < num; i++)
        // {
        //     string line;
        //     getline(inputstream, line);
        //     const char *cstr = line.c_str();
        //     std::stringstream ss4;
        //     // cout << cstr << "\n";

        //     ss4 << cstr;
        //     FieldT val;
        //     ss4 >> val;
        //     cout << val << "\n";
        //     primary_input.push_back(val);
        // }
        

        // Alternative code for loading the primary input from file
        // r1cs_primary_input<libff::Fr<libsnark::default_r1cs_ppzksnark_pp>> primary_input;
        // in.open("primary_export");
        // in >> primary_input;
        // in.close();
    }


    // Avoid loading the primary input from file, but rather assign the values from string
    {
        // This was for the pre-image verification 
        // const int numInputs = 9;
        // VariableArray input(numInputs, "input");
        // pb->val(input[0]) = readFieldElementFromHex("1");
        // pb->val(input[1]) = readFieldElementFromHex("b6f61122");
        // pb->val(input[2]) = readFieldElementFromHex("885eb313");
        // pb->val(input[3]) = readFieldElementFromHex("c0f2c3b4");
        // pb->val(input[4]) = readFieldElementFromHex("e69a5a"  );
        // pb->val(input[5]) = readFieldElementFromHex("48630f4a");
        // pb->val(input[6]) = readFieldElementFromHex("f4a3e640");
        // pb->val(input[7]) = readFieldElementFromHex("786aff6f");
        // pb->val(input[8]) = readFieldElementFromHex("a5ce0c1d");
        // const r1cs_variable_assignment<FieldT> full_assignment = get_variable_assignment_from_gadgetlib2(*pb);
        // const r1cs_primary_input<FieldT> primary_input(full_assignment.begin(),	full_assignment.begin() + numInputs);
    }


    // Insert input values for the CrowdPatching verification

    string fileExpectedDigestHexString     = "8457612244c5f5b7b2147b42ddbf859d68a78560d3f35ae4d411690cadd9a794";
    string rExpectedDigestHexString        = "ac9e59b4e3ca66a4cb1cfb633183de3f6b6cf244b5c70da45fda3228ce71a814";
    string fileExpectedCiphertextHexString = "a52e5f3ab41c9c15c92d242c9ab7a2286981a302ce363c7b2edacd88d16f16f4509be4bcd2dfdaf3861f23069e173a59a4bac92dfeea36815f61a3527421a2fddaf6dae55733e27987e531174725cdf033c3eedcd5a8326b9c3018edd120a81bce99aca01537118c9a743b8b3316cd455287de9ab56110ba65fe83d25055379380921813de9acd89ab378d11989d63a499069fdb719fa418efeac59f13dd898ba751f5d986f084e2fa5af0d47a2cc221bd04026c6590cfbecbe38e04a1d6574442cdeb0af749d46a9e58ca97965e903798c2ce0b6e341fdf3fd8048f3b13c4e88a78522c703b756c5d9a939cc62a376bfbc42b5df6e8c2816ce1f43281355945";
 
    int numHexDigitsInDigest = 256 / 4;
    int numHexDigitsInDigestInputVariable = 8;
    
    int numHexDigitsInCiphertext = fileExpectedCiphertextHexString.length();
    int numHexDigitsInCiphertextInputVariable = 16;

    const int numInputs = 
        1 + (numHexDigitsInDigest / numHexDigitsInDigestInputVariable) * 2 + numHexDigitsInCiphertext / numHexDigitsInCiphertextInputVariable;

    printf("numInputs = %d\n", numInputs);
    VariableArray input(numInputs, "input");


    pb->val(input[0]) = readFieldElementFromHex("1");
    // printf("input[%d] = %s\n", 0, "1");

    int i;
    string subStr;
    string subStr1;
    string subStr2;
    int inputIndex = 1;
    
    for (i = 0; i < numHexDigitsInDigest-numHexDigitsInDigestInputVariable+1; i += numHexDigitsInDigestInputVariable)
    {
        subStr = fileExpectedDigestHexString.substr(i, numHexDigitsInDigestInputVariable);

        char *cstr = new char[subStr.length() + 1];
        strcpy(cstr, subStr.c_str());

        pb->val(input[inputIndex]) = readFieldElementFromHex(cstr);
        // printf("input[%d] = %s\n", inputIndex, cstr);

        delete [] cstr;

        inputIndex++;
    }

    for (i = 0; i < numHexDigitsInDigest-numHexDigitsInDigestInputVariable+1; i += numHexDigitsInDigestInputVariable)
    {
        subStr = rExpectedDigestHexString.substr(i, numHexDigitsInDigestInputVariable);

        char *cstr = new char[subStr.length() + 1];
        strcpy(cstr, subStr.c_str());
        
        pb->val(input[inputIndex]) = readFieldElementFromHex(cstr);
        // printf("input[%d] = %s\n", inputIndex, cstr);
        
        delete [] cstr;
        
        inputIndex++;
    }

    for ( i = 0; i < numHexDigitsInCiphertext-(2*numHexDigitsInCiphertextInputVariable)+1; i += 2*numHexDigitsInCiphertextInputVariable )
    {
        // Swap the two 64-bits words in the current block before filling it to
        // compensate a bug/variant in the Jsnark Speck cipher implementation
        // with respect to the standard implementation

        subStr1 = fileExpectedCiphertextHexString.substr(i, numHexDigitsInCiphertextInputVariable);
        subStr2 = fileExpectedCiphertextHexString.substr(i+numHexDigitsInCiphertextInputVariable, numHexDigitsInCiphertextInputVariable);

        char *cstr1 = new char[subStr1.length() + 1];
        strcpy(cstr1, subStr1.c_str());

        char *cstr2 = new char[subStr2.length() + 1];
        strcpy(cstr2, subStr2.c_str());

        // printf("fileExpectedCiphertextPartial: %s\n", cstr);

        // Push the second substring first, then the second substring
        pb->val(input[inputIndex]) = readFieldElementFromHex(cstr2);
        // printf("input[%d] = %s\n", inputIndex, cstr2);
        pb->val(input[inputIndex+1]) = readFieldElementFromHex(cstr1);
        // printf("input[%d] = %s\n", inputIndex+1, cstr1);

        delete [] cstr1;
        delete [] cstr2;
        
        inputIndex += 2;
    }


    const r1cs_variable_assignment<FieldT> full_assignment = get_variable_assignment_from_gadgetlib2(*pb);
    const r1cs_primary_input<FieldT> primary_input(full_assignment.begin(),	full_assignment.begin() + numInputs);

    const bool ans = r1cs_ppzksnark_verifier_strong_IC<libsnark::default_r1cs_ppzksnark_pp>(keypair.vk, primary_input, proof);
    printf("* The verification result is: %s\n", (ans ? "PASS" : "FAIL"));
    assert(ans);


    return 0;
}