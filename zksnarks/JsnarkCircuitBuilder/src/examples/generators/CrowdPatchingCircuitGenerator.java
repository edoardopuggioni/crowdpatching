package examples.generators;

import circuit.eval.CircuitEvaluator;
import circuit.structure.CircuitGenerator;
import circuit.structure.Wire;
import circuit.structure.WireArray;

import examples.gadgets.blockciphers.SymmetricEncryptionCBCGadget;
import examples.gadgets.hash.SHA256Gadget;
import examples.gadgets.blockciphers.Speck128CipherGadget;

import util.Util;

import java.math.BigInteger;
import java.util.ArrayList;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import java.io.File;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class CrowdPatchingCircuitGenerator extends CircuitGenerator
{
    // Only value needed by the generator to generate the keys
    int fileNumHexDigits;

    // Secret parameters known only by the prover
    String filePlaintextHexString;
    String rHexString;

    // Public parameters known also by the verifier
    String fileExpectedDigestHexString;
    String rExpectedDigestHexString;
    String fileExpectedCiphertextHexString;  

    // Wire variables for the pre-image verifications part of the circuit
    private Wire[] filePlaintextWitness8bitsWires;
    private Wire[] rWitness8bitsWires;
    private Wire[] fileExpectedDigest32bitsWires;
    private Wire[] rExpectedDigest32bitsWires;

    // Wire variables for the encryption verification part of the circuit
    private Wire[] filePlaintextWitness64bitsWires;
    private Wire[] keyWitness64bitsWires;
    private Wire[] ivWitness64bitsWires;
    private Wire[] fileExpectedCiphertext64bitsWires;

    // Useful coonstant values
    final int numHexDigitsIn8bits   =    8 / 4;
    final int numHexDigitsIn32bits  =   32 / 4;
    final int numHexDigitsIn64bits  =   64 / 4;
    final int numHexDigitsIn128bits =  128 / 4;
    final int numHexDigitsIn256bits =  256 / 4;

    // Constructor for the prover
    public CrowdPatchingCircuitGenerator(String circuitName, String filePlaintextHexString, String rHexString, String fileExpectedDigestHexString, 
                                            String rExpectedDigestHexString, String fileExpectedCiphertextHexString)
    {
        super(circuitName);

        this.filePlaintextHexString          = filePlaintextHexString;
        this.rHexString                      = rHexString;
        this.fileExpectedDigestHexString     = fileExpectedDigestHexString;
        this.rExpectedDigestHexString        = rExpectedDigestHexString;
        this.fileExpectedCiphertextHexString = fileExpectedCiphertextHexString;

        this.fileNumHexDigits = filePlaintextHexString.length();
    }

    // Constructor
    public CrowdPatchingCircuitGenerator(String circuitName, int fileNumHexDigits)
    {
        super(circuitName);

        this.fileNumHexDigits = fileNumHexDigits;
    }


    @Override
    protected void buildCircuit() 
    {
        // Initialize the arrays of wires with the right sizes (number of wires)

        // Number of bytes given by the number of hex digits (length of the hex string) divided by 2
        filePlaintextWitness8bitsWires = createProverWitnessWireArray( fileNumHexDigits / 2 );

        // Parameter r is 256 bits: 256 / 8 = 32 bytes
        rWitness8bitsWires = createProverWitnessWireArray(32);

        // Number of 32-bits words in a SHA256 digest is 256 / 32 = 8
        final int num32bitsWordsInDigest = 8;
        fileExpectedDigest32bitsWires = createInputWireArray(num32bitsWordsInDigest);
        rExpectedDigest32bitsWires    = createInputWireArray(num32bitsWordsInDigest);

        // Different array of wires for the same plaintext to use in the second part of the circuit
        filePlaintextWitness64bitsWires = createProverWitnessWireArray( fileNumHexDigits / numHexDigitsIn64bits );

        // Key and IV are 128 bits each: I need two 64-bits wires for each array
        keyWitness64bitsWires = createProverWitnessWireArray(2);
        ivWitness64bitsWires  = createProverWitnessWireArray(2);

        fileExpectedCiphertext64bitsWires = createInputWireArray( fileNumHexDigits / numHexDigitsIn64bits );


        // First the part of the circuit regarding the two hash pre-image verifications

        // Create instances of the SHA256 gadget
        SHA256Gadget fileSHA256Gadget1 = new SHA256Gadget(filePlaintextWitness8bitsWires, 8, filePlaintextWitness8bitsWires.length, false, true, "");
        SHA256Gadget rSHA256Gadget2    = new SHA256Gadget(rWitness8bitsWires, 8, rWitness8bitsWires.length, false, true, "");

        // The method getOutputWires() of the gadget returns the digest as 32-bits wires array
        Wire[] fileDigest32bitsWires = fileSHA256Gadget1.getOutputWires();
        Wire[] rDigest32bitsWires    = rSHA256Gadget2.getOutputWires();

        // Enforce the resulting digests and expected digests to be identical
        for (int i = 0; i < num32bitsWordsInDigest; i++)
        {
            addEqualityAssertion(fileDigest32bitsWires[i], fileExpectedDigest32bitsWires[i]);
            addEqualityAssertion(rDigest32bitsWires[i], rExpectedDigest32bitsWires[i]);
        }


        // Then the part of the circuit regarding the encryption verification
        
        // Variable that will gradually become the result of the encryption
        Wire[] fileCiphertext64bitsWires = new Wire[0];

        // Implementation of the Cipher Block Chaining (CBC) mode of operation
        Wire[] expandedKey = Speck128CipherGadget.expandKey(keyWitness64bitsWires);
        Wire[] prevCipher = new Wire[2];
        prevCipher[0] = ivWitness64bitsWires[0];
        prevCipher[1] = ivWitness64bitsWires[1];
        for( int i = 0; i < filePlaintextWitness64bitsWires.length-2+1; i+= 2 )
        {
            Wire[] xored = new Wire[2];
            xored[0] = filePlaintextWitness64bitsWires[i].xorBitwise(prevCipher[0], 64);
            xored[1] = filePlaintextWitness64bitsWires[i+1].xorBitwise(prevCipher[1], 64);

            prevCipher = new Speck128CipherGadget(xored, expandedKey).getOutputWires();

            fileCiphertext64bitsWires = Util.concat(fileCiphertext64bitsWires, prevCipher);
        }

        for (int i = 0; i < fileCiphertext64bitsWires.length; i++)
        {
            addEqualityAssertion(fileCiphertext64bitsWires[i], fileExpectedCiphertext64bitsWires[i]);
        }
    }


    
    // The purpose of this method is to "fill" the wires with the actual data at the time of evaluation
    @Override
    public void generateSampleInput(CircuitEvaluator evaluator)
    {
        // Helper variables
        String subStr;
        int wireIndex;
        BigInteger b;
        String hexString128bits;


        // First fill the wires for the hash pre-image verifications

        // Fill the wires of the file plaintext used by the SHA256 gadget
        wireIndex = 0;
        for( int i = 0; i < filePlaintextHexString.length()-numHexDigitsIn8bits+1; i += numHexDigitsIn8bits )
        {
            subStr = filePlaintextHexString.substring(i, i+numHexDigitsIn8bits);
            b = new BigInteger(subStr, 16);
            evaluator.setWireValue(filePlaintextWitness8bitsWires[wireIndex], b);

            wireIndex++;
        }

        // Fill the wires of parameter r used by the SHA256 gadget
        wireIndex = 0;
        for( int i = 0; i < rHexString.length()-numHexDigitsIn8bits+1; i += numHexDigitsIn8bits )
        {
            subStr = rHexString.substring(i, i+numHexDigitsIn8bits);
            b = new BigInteger(subStr, 16);
            evaluator.setWireValue(rWitness8bitsWires[wireIndex], b);

            wireIndex++;
        }

        // Fill the wires of the expected digest for the file (used in the equality assertion)
        wireIndex = 0;
        for( int i = 0; i < fileExpectedDigestHexString.length()-numHexDigitsIn32bits+1; i += numHexDigitsIn32bits )
        {
            subStr = fileExpectedDigestHexString.substring(i, i+numHexDigitsIn32bits);
            b = new BigInteger(subStr, 16);
            evaluator.setWireValue(fileExpectedDigest32bitsWires[wireIndex], b);

            wireIndex++;
        }

        // Fill the wires of the expected digest for r (used in the equality assertion)
        wireIndex = 0;
        for( int i = 0; i < rExpectedDigestHexString.length()-numHexDigitsIn32bits+1; i += numHexDigitsIn32bits )
        {
            subStr = rExpectedDigestHexString.substring(i, i+numHexDigitsIn32bits);
            b = new BigInteger(subStr, 16);
            evaluator.setWireValue(rExpectedDigest32bitsWires[wireIndex], b);

            wireIndex++;
        }


        // Then fill the wires for the encryption verification

        // Fill the file plaintext wires
        wireIndex = 0;
        for( int i = 0; i < filePlaintextHexString.length()-numHexDigitsIn128bits+1; i += numHexDigitsIn128bits )
        {
            hexString128bits = filePlaintextHexString.substring(i, i+numHexDigitsIn128bits);
                        
            // Swap the two 64-bits words in the current block before filling it to
            // compensate a bug/variant in the Jsnark Speck cipher implementation
            // with respect to the standard implementation

            subStr = hexString128bits.substring(16, 32);
            b = new BigInteger(subStr, 16);
            evaluator.setWireValue(filePlaintextWitness64bitsWires[wireIndex], b);

            subStr = hexString128bits.substring(0, 16);
            b = new BigInteger(subStr, 16);
            evaluator.setWireValue(filePlaintextWitness64bitsWires[wireIndex+1], b);

            wireIndex += 2;
        }

        // Fill the expected file ciphertext wires
        wireIndex = 0;
        for( int i = 0; i < fileExpectedCiphertextHexString.length()-numHexDigitsIn128bits+1; i += numHexDigitsIn128bits )
        {
            hexString128bits = fileExpectedCiphertextHexString.substring(i, i+numHexDigitsIn128bits);

            // Swap the two 64-bits words in the current block before filling it to
            // compensate a bug/variant in the Jsnark Speck cipher implementation
            // with respect to the standard implementation

            subStr = hexString128bits.substring(16, 32);
            b = new BigInteger(subStr, 16);
            evaluator.setWireValue(fileExpectedCiphertext64bitsWires[wireIndex], b);

            subStr = hexString128bits.substring(0, 16);
            b = new BigInteger(subStr, 16);
            evaluator.setWireValue(fileExpectedCiphertext64bitsWires[wireIndex+1], b);

            wireIndex += 2;
        }

        // Get the key and IV hex strings from the parameter r
        // (Size of r is 256 bits: 128 bits key and 128 bits IV)
        String keyHexString = rHexString.substring(0, numHexDigitsIn128bits);
        String ivHexString  = rHexString.substring(numHexDigitsIn128bits, numHexDigitsIn256bits);

        // Fill the key wires
        // Swap the two 64-bits words in the current block before filling it to
        // compensate a bug/variant in the Jsnark Speck cipher implementation
        // with respect to the standard implementation
        subStr = keyHexString.substring(16, 32);
        b = new BigInteger(subStr, 16);
        evaluator.setWireValue(keyWitness64bitsWires[0], b);
        subStr = keyHexString.substring(0, 16);
        b = new BigInteger(subStr, 16);
        evaluator.setWireValue(keyWitness64bitsWires[1], b);

        
        // Fill the IV wires
        // Swap the two 64-bits words in the current block before filling it to
        // compensate a bug/variant in the Jsnark Speck cipher implementation
        // with respect to the standard implementation
        subStr = ivHexString.substring(16, 32);
        b = new BigInteger(subStr, 16);
        evaluator.setWireValue(ivWitness64bitsWires[0], b);
        subStr = ivHexString.substring(0, 16);
        b = new BigInteger(subStr, 16);
        evaluator.setWireValue(ivWitness64bitsWires[1], b);
    }


    public static void main(String[] args) 
    {
        if( args.length != 1 || ( args[0].charAt(0) != 'g' && args[0].charAt(0) != 'p' ) )
        {
            System.out.println("Error: use argument 'g' to run generator G or 'p' to run prover P");
            System.exit(1);
        }
        
        boolean prover;
        if( args[0].charAt(0) == 'p' )
            prover = true;
        else
            prover = false;

        // Only value needed by the generator to generate the keys
        int fileNumHexDigits;
    
        // Secret parameters known only by the prover
        String filePlaintextHexString = "";
        String rHexString = "";

        // Public parameters known also by the generator and the verifier
        String plaintextCharString = "";
        String fileExpectedDigestHexString = "";
        String rExpectedDigestHexString = "";
        String fileExpectedCiphertextHexString = "";



        // Plaintext size = 64 bytes * 4 = 256 bytes = 512 hex digits = 2048 bits
        plaintextCharString = "AJDSFAHDVKJSMNakljfkajhsfkawasalwhertaljhg9835498hgbq98fdaojsas1" +
                                "94805uyh7wjkfssfsslkdq0lkjdlsj029vnaf13iaifdjlk2jrefdjlksdflafa1" +
                                "94805uyhlkdq0lkjdkdq0lkjdlsj029vnaf13iaifdjlk2jrefdjlksghdflafa1" +
                                "h7wjkfssfsslkdq0hlkdq0lkjdkdq0lkjdlsjhlkdq0lkjdkdq0lkaslkjdlsj01";
        
        filePlaintextHexString = Util.stringToHex(plaintextCharString);
        // Alternatively you can directly set the hex string manually

        // System.out.println("Plain hex before: " + filePlaintextHexString);

        //File plaintext (in hex) from txt file:
        try{
            Scanner scanner1 = new Scanner(new File("fileHex.txt"));
            filePlaintextHexString = scanner1.nextLine();
        }
        catch(FileNotFoundException ex){}

        // System.out.println("Plain hex after: " + filePlaintextHexString);


        rHexString = "d4c6ecb0035d57a13e59135d29c2d4c59c26393e3032af5461f181b91e6176e4";
        rExpectedDigestHexString    = "ac9e59b4e3ca66a4cb1cfb633183de3f6b6cf244b5c70da45fda3228ce71a814";


        // Expected digest for 256 bytes file:
        // fileExpectedDigestHexString = "8457612244c5f5b7b2147b42ddbf859d68a78560d3f35ae4d411690cadd9a794";
        // Expecred digest for 0.5 kilobytes file:
        // fileExpectedDigestHexString = "5674619e4cc2517a287ca18723d14c6434188e0d1b4c9c2a3643d6e963faeaec";
        // Expecred digest for 2 kilobytes file:
        // fileExpectedDigestHexString = "cad79ebcc5d2b369c40245e97b329238a92780cf6681e6347c9840c79640e2e1";
        // Expecred digest for 5 kilobytes file:
        fileExpectedDigestHexString = "103ae1aca797bd7e4bc82c5b432f05c1cdbdbbb17c371931511f431f7cd5fc5a";



        // File expected ciphertext for 256 bytes file:
        fileExpectedCiphertextHexString = "a52e5f3ab41c9c15c92d242c9ab7a2286981a302ce363c7b2edacd88d16f16f4509be4bcd2dfdaf3861f23069e173a59a4bac92dfeea36815f61a3527421a2fddaf6dae55733e27987e531174725cdf033c3eedcd5a8326b9c3018edd120a81bce99aca01537118c9a743b8b3316cd455287de9ab56110ba65fe83d25055379380921813de9acd89ab378d11989d63a499069fdb719fa418efeac59f13dd898ba751f5d986f084e2fa5af0d47a2cc221bd04026c6590cfbecbe38e04a1d6574442cdeb0af749d46a9e58ca97965e903798c2ce0b6e341fdf3fd8048f3b13c4e88a78522c703b756c5d9a939cc62a376bfbc42b5df6e8c2816ce1f43281355945";
        // File expected ciphertext from txt file:
        try{
            Scanner scanner2 = new Scanner(new File("fileHexENC.txt"));
            fileExpectedCiphertextHexString = scanner2.nextLine();
        }
        catch(FileNotFoundException ex){}




        // Set the only value needed by the generator 
        fileNumHexDigits = filePlaintextHexString.length();
        // Alternatively you can set this number manually

        CrowdPatchingCircuitGenerator circuitGenerator;
        if(prover)
        {
            circuitGenerator = new CrowdPatchingCircuitGenerator("crowdpatching_prover", filePlaintextHexString, rHexString, 
                fileExpectedDigestHexString, rExpectedDigestHexString, fileExpectedCiphertextHexString);
        }
        else
        {
            circuitGenerator = new CrowdPatchingCircuitGenerator("crowdpatching_generator", fileNumHexDigits);
        }


        circuitGenerator.generateCircuit();


        if (prover)
        {
            circuitGenerator.evalCircuit();     
            circuitGenerator.prepFiles();
        }
        else
            circuitGenerator.writeCircuitFile();


        // circuitGenerator.runLibsnark();
    }
}