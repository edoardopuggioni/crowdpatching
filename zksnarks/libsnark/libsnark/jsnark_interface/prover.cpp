/*
    My attempt to separate entities: this is the prover P.
    Load proving key from file, generate proof and export to file.
    Also export primary input to file.
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

#include <bitset>

using namespace std;


int main(int argc, char **argv)
{
    if(argc != 3)
    {
        printf("Usage: %s <.arith file> <.in file>\n", argv[0]);
        exit(0);
    }

    libff::start_profiling();
	gadgetlib2::initPublicParamsFromDefaultPp();
	gadgetlib2::GadgetLibAdapter::resetVariableIndex();
	ProtoboardPtr pb = gadgetlib2::Protoboard::create(gadgetlib2::R1P);


    r1cs_ppzksnark_keypair<libsnark::default_r1cs_ppzksnark_pp> keypair;

    // Loading proving key from file
    ifstream in("PK_export");
    in >> keypair.pk;
    in.close();


    CircuitReader reader(argv[1], argv[2], pb);


    r1cs_constraint_system<FieldT> cs = get_constraint_system_from_gadgetlib2(*pb);
    const r1cs_variable_assignment<FieldT> full_assignment = get_variable_assignment_from_gadgetlib2(*pb);
    cs.primary_input_size = reader.getNumInputs() + reader.getNumOutputs();
    cs.auxiliary_input_size = full_assignment.size() - cs.num_inputs();


    // Extract primary and auxiliary input
    const r1cs_primary_input<FieldT> primary_input(full_assignment.begin(),	full_assignment.begin() + cs.num_inputs());
    const r1cs_auxiliary_input<FieldT> auxiliary_input(full_assignment.begin() + cs.num_inputs(), full_assignment.end());


    assert(cs.is_valid());
    assert(cs.is_satisfied(primary_input, auxiliary_input));


    r1cs_example<FieldT> example(cs, primary_input, auxiliary_input);
    r1cs_ppzksnark_proof<libsnark::default_r1cs_ppzksnark_pp> proof = 
        r1cs_ppzksnark_prover<libsnark::default_r1cs_ppzksnark_pp>(keypair.pk, example.primary_input, example.auxiliary_input);


    // Exporting proof into file
    ofstream prooffile;
    prooffile.open("proof_export");
    prooffile << proof;
    prooffile.close();


    // Exporting the primary input for the benefit of the verifier:
    // this approach does not work, verifier cannot validate.

    // ofstream iofile;
    // // strcpy(str, argv[5]);
    // // strcat(str, ".stmt"); 
    // iofile.open("stmt_export"); // I think stmt stands for "statement" 

    // iofile << primary_input.size() << "\n" ;
    // for(int i = 0; i < primary_input.size() ; i++){
    //     iofile << primary_input[i] << "\n";
    // }

    // iofile.close();


    // Alternative code for exporting the whole primary input
    // Not needed anymore: found a way to make the verifier independent of this
    // ofstream primaryfile;
    // primaryfile.open("primary_export");
    // primaryfile << primary_input;
    // primaryfile.close();


    // Try to export each element of the primary input separately
    // ofstream separateprimaryfile("separateprimary_export");
    // separateprimaryfile << primary_input.size() << "\n" ;
    // for(int i = 0; i < primary_input.size() ; i++)
    // {
    //     separateprimaryfile << primary_input[i] << "\n";
    // }
    // separateprimaryfile.close();


    // Testing the verifier here to see if the proof works: it does.

    cout << "\n\nSTARTING THE VERIFIER HERE JUST FOR TESTING\n\n";

    // Loading veryfing key from file
    in.open("VK_export");
    in >> keypair.vk;
    in.close();

    const bool ans = r1cs_ppzksnark_verifier_strong_IC<libsnark::default_r1cs_ppzksnark_pp>(keypair.vk, primary_input, proof);
    printf("* The verification result is: %s\n", (ans ? "PASS" : "FAIL"));
    assert(ans);


    return 0;
}