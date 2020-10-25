/*
    My attempt to separate entities: this is the generator G.
    Generate proving and verification keys and export to files.
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


using namespace std;


int main(int argc, char **argv)
{
    if(argc != 2)
    {
        printf("Usage: %s <.arith file>\n", argv[0]);
        exit(0);
    }

    libff::start_profiling();
	gadgetlib2::initPublicParamsFromDefaultPp();
	gadgetlib2::GadgetLibAdapter::resetVariableIndex();
	ProtoboardPtr pb = gadgetlib2::Protoboard::create(gadgetlib2::R1P);


    CircuitReader reader(argv[1], pb);


    r1cs_constraint_system<FieldT> cs = get_constraint_system_from_gadgetlib2(*pb);
    const r1cs_variable_assignment<FieldT> full_assignment = get_variable_assignment_from_gadgetlib2(*pb);
    cs.primary_input_size = reader.getNumInputs() + reader.getNumOutputs();
    cs.auxiliary_input_size = full_assignment.size() - cs.num_inputs();


    // Generating keys
    
    r1cs_ppzksnark_keypair<libsnark::default_r1cs_ppzksnark_pp> keypair = r1cs_ppzksnark_generator<libsnark::default_r1cs_ppzksnark_pp>(cs);


    // Export keys into file

    ofstream pkfile;
    pkfile.open ("PK_export");
    pkfile << keypair.pk;
    pkfile.close();

    ofstream vkfile;
    vkfile.open ("VK_export");
    vkfile << keypair.vk;
    vkfile.close();


    return 0;
}