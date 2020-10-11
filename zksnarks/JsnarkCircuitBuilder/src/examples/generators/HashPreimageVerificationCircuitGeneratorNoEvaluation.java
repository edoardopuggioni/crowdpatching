/* 
    This is the CircuitGenertor for the generator G of the keys PK and VK

    Check the normal CircuitGenerator for all the explanatory comments
*/

package examples.generators;

import circuit.eval.CircuitEvaluator;
import circuit.structure.CircuitGenerator;
import circuit.structure.Wire;
import circuit.structure.WireArray;
import examples.gadgets.blockciphers.SymmetricEncryptionCBCGadget;
import examples.gadgets.hash.SHA256Gadget;
import util.Util;

import java.math.BigInteger;

public class HashPreimageVerificationCircuitGeneratorNoEvaluation extends CircuitGenerator
{
    private String preImage;
    private String expectedDigest;

    private int preImageSizeInBytes;
    private int digestSizeInBytes;
    private int num32WordsInDigest = 8; // 256 bits / 32 bits = 8
    // private int numHexDigitsInDigest = 64; // 256 bit digest and each hex digit is 4 bits: 256 / 4 = 64

    private Wire[] preImageWitnessWiresInBytes;
    private Wire[] expectedDigestWiresIn32Words;


    // Constructor
    public HashPreimageVerificationCircuitGeneratorNoEvaluation(String circuitName, String preImage, String expectedDigest)
    {
        super(circuitName);

        this.preImage = preImage;
        this.expectedDigest = expectedDigest;

        this.preImageSizeInBytes = preImage.length();
        this.digestSizeInBytes = expectedDigest.length();
    }


    @Override
    protected void buildCircuit() 
    {
        preImageWitnessWiresInBytes = createProverWitnessWireArray(preImageSizeInBytes);

        // Set each wire of the expected digest to have a 32-bits word
        expectedDigestWiresIn32Words = createInputWireArray(num32WordsInDigest);


        // Constructor: SHA256Gadget(Wire[] ins, int bitWidthPerInputElement, int totalLengthInBytes, boolean binaryOutput,	boolean paddingRequired, String... desc)
        SHA256Gadget newSHA256Gadget = new SHA256Gadget(preImageWitnessWiresInBytes, 8, preImageSizeInBytes, false, true, "");
        
        // The method getOutputWires() of the gadget returns the digest as 32-bit words
        Wire[] digestWires = newSHA256Gadget.getOutputWires();


        for (int i = 0; i < digestWires.length; i++)
        {
            addEqualityAssertion(digestWires[i], expectedDigestWiresIn32Words[i]);
        }
    }


    @Override
    public void generateSampleInput(CircuitEvaluator evaluator)
    {
        // for (int i = 0; i < preImageSizeInBytes; i++) 
        // {
		// 	evaluator.setWireValue(preImageWitnessWiresInBytes[i], preImage.charAt(i));
		// }

        // int j = 0;
        // for (int i = 0; i < digestSizeInBytes-8+1; i += 8) 
        // {
        //     String subStr = expectedDigest.substring(i, i+8);
        //     System.out.println("subStr = " + subStr);
        //     evaluator.setWireValue( expectedDigestWiresIn32Words[j++], new BigInteger( subStr, 16 ) );            
		// }
    }


    public static void main(String[] args) 
    {
        String preImage;
        String expectedDigest;


        // 64-bytes pre-image
        // preImage = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijkl";
        // expectedDigest = "2fcd5a0d60e4c941381fcc4e00a4bf8be422c3ddfafb93c809e8d1e2bfffae8e";

        // Arbitrary pre-image (less than 64 bytes)
        preImage = "testPreImageFromSHA256WebTool";
        expectedDigest = "b6f61122885eb313c0f2c3b400e69a5a48630f4af4a3e640786aff6fa5ce0c1d";

        // Arbitrary pre-image (more than 64 bytes)
        // preImage = "repeatThisStringIndefinitelyrepeatThisStringIndefinitelyrepeatThisStringIndefinitely" +
        //            "repeatThisStringIndefinitelyrepeatThisStringIndefinitelyrepeatThisStringIndefinitely" +
        //            "repeatThisStringIndefinitelyrepeatThisStringIndefinitelyrepeatThisStringIndefinitely" +
        //            "repeatThisStringIndefinitelyrepeatThisStringIndefinitelyrepeatThisStringIndefinitely" +
        //            "repeatThisStringIndefinitelyrepeatThisStringIndefinitelyrepeatThisStringIndefinitely" +
        //            "repeatThisStringIndefinitelyrepeatThisStringIndefinitely17Times";
        // expectedDigest = "4cdd9ca3835a07de42375ccb222536a7056a43c3055dff1e5bc5e84515ea2cef";


        HashPreimageVerificationCircuitGeneratorNoEvaluation circuitGenerator = 
            new HashPreimageVerificationCircuitGeneratorNoEvaluation("hash_preimage_NO_EVALUATION", preImage, expectedDigest);

        circuitGenerator.generateCircuit();

        // circuitGenerator.evalCircuit();

        // circuitGenerator.prepFiles();
        circuitGenerator.writeCircuitFile();

        // circuitGenerator.runLibsnark();
    }
}