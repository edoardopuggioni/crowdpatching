/*
    This CircuitGenerator will produce a verification circuit
    which a prover can use to prove that given z, the prover
    knows x, y such that x + y = z.

    This CircuitGenerator can be used by a trusted third party
    to generate the circuit only, without having to provide
    the witness. The trusted third party can then run the 
    separate libsnark interface that will only generate the
    proving and verification key.

    In other words, this is an attempt to create a separate 
    CircuitGenerator for the different entities, in this
    case the trusted third party that generates the keys.

    The original CircuitGenerator of the jsnark library can 
    be used by the prover.

    The verifier entity does not need a CircuitGenerator: the
    independend libsnark interface can be used directly.
*/

package examples.generators;

import circuit.eval.CircuitEvaluator;
import circuit.structure.CircuitGenerator;
import circuit.structure.Wire;

public class AddVerificationCircuitGeneratorNoEvaluation extends CircuitGenerator
{
    private Wire[] privateInputs;
    private Wire expOutput;

    public AddVerificationCircuitGeneratorNoEvaluation(String circuitName)
    {
        super(circuitName);
    }

    @Override
    protected void buildCircuit()
    {
        expOutput = createInputWire();
        privateInputs = createProverWitnessWireArray(2);
        Wire result = privateInputs[0].add(privateInputs[1]);
        addEqualityAssertion(expOutput, result);
    }

    @Override
    public void generateSampleInput(CircuitEvaluator evaluator)
    {
        // I still need to implement this method because it's abstract, but I can leave it empty.

        // evaluator.setWireValue(privateInputs[0], 2);
        // evaluator.setWireValue(privateInputs[1], 5);
        // evaluator.setWireValue(expOutput, 7);
    }

    public static void main(String[] args)
    {
        AddVerificationCircuitGeneratorNoEvaluation myGen = new AddVerificationCircuitGeneratorNoEvaluation("add_verification_NO_EVALUATION");


        // This method calls initCircuitConstruction() and buildCircuit()
        myGen.generateCircuit();

        // This method creates a new CircuitEvaluator object using this CircuitGenerator as parameter for the constructor.
        // Then it calls generateSampleInput() method on the CircuitEvaluator just created.
        // Finally it calls the evaluate() method of the CircuitEvaluator object (NB: not a method of CircuitGenerator).
        // myGen.evalCircuit();

        // This method first calls writeCircuitFile() to generate the .arith file.
        // Then it calls the writeInputFile() method of the evaluator to generate the .in file.
        // myGen.prepFiles();
        
        // Instead of calling prepFiles() I will only generate the .arith file using this method
        myGen.writeCircuitFile();

        // This method will call the C++ jsnark-libsnark interface using the .arith and .in files as line arguments.
        // myGen.runLibsnark();
        // This doesn't work without evaluation, because it needs the .in file. I will use my sperate libsnark interface
        // for the generator for producing the keys using only the .arith file.
    }
}