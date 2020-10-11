/*
    This is a very simple verification circuit which a prover can use to
    prove that given z, the prover knows x, y such that x + y = z.
    This is the example explained in the zcash blog part 1.
*/

package examples.generators;

import circuit.eval.CircuitEvaluator;
import circuit.structure.CircuitGenerator;
import circuit.structure.Wire;

public class AddVerificationCircuitGenerator extends CircuitGenerator
{
    private Wire[] privateInputs;
    private Wire expOutput;

    public AddVerificationCircuitGenerator(String circuitName)
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
        evaluator.setWireValue(privateInputs[0], 2);
        evaluator.setWireValue(privateInputs[1], 5);
        evaluator.setWireValue(expOutput, 7);
    }

    public static void main(String[] args)
    {
        AddVerificationCircuitGenerator myGen = new AddVerificationCircuitGenerator("add_verification");


        // This method calls initCircuitConstruction() and buildCircuit()
        myGen.generateCircuit();

        // This method creates a new CircuitEvaluator object using this CircuitGenerator as parameter for the constructor.
        // Then it calls generateSampleInput() method on the CircuitEvaluator just created.
        // Finally it calls the evaluate() method of the CircuitEvaluator object (NB: not a method of CircuitGenerator).
        myGen.evalCircuit();

        // This method first calls writeCircuitFile() to generate the .arith file.
        // Then it calls the writeInputFile() method of the evaluator to generate the .in file.
        myGen.prepFiles();

        // This method will call the C++ jsnark-libsnark interface using the .arith and .in files as line arguments.
        myGen.runLibsnark();
    }
}