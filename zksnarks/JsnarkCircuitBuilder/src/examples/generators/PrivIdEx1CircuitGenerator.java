package examples.generators;

import circuit.eval.CircuitEvaluator;
import circuit.structure.CircuitGenerator;
import circuit.structure.Wire;
import circuit.structure.WireArray;
import examples.gadgets.blockciphers.SymmetricEncryptionCBCGadget;
import examples.gadgets.hash.SHA256Gadget;
import util.Util;

import java.math.BigInteger;

public class PrivIdEx1CircuitGenerator extends CircuitGenerator
{
    // Inputs to the SHA256 gadget (the digest variable is created later)
    // private Wire[] plainTextWiresToSHA256;
    // private int plainTextWordSizeForSHA256; // as 8-bits words
    private int plainTextSizeInBytes;


    // Inputs and outputs of the symmetric CBC gadget

    private Wire[] plainTextWiresInHex;        // as 64 bit words

    private int numHexDigitsPerInputWire = 16; // i.e. 64 bits
    // Each hex digit is 4 bits

    private Wire[] keyBitsWires;               // 128 bits
    private Wire[] ivBitsWires;                // 128 bits

    private final int keyIVSize = 128;

    private Wire[] cipherText;


    // Inputs to the circuit

    private String plainTextInHex; // in hex
    private String keyString;      // in hex
    private String ivString;       // in hex


    // Currently only Speck cipher supported in CBC mode: we hardcode it here
    String cipherName = "speck128";


    // TO DO: write a test case
    // (This to do was orginally written just above the constructor: don't know what she meant)


    // Constructor
    public PrivIdEx1CircuitGenerator(String circuitName, String plainText, String key, String iv)
    {
        super(circuitName);

        this.plainTextInHex = plainText;

        // TO DO: check the constraints on plaintext size

        this.keyString = key;
        this.ivString = iv;

        plainTextSizeInBytes = plainTextInHex.length()/2; // (4-bits digits vs 8-bits digits)
        // plainTextWordSizeForSymEncr = (plainTextInHex.length() * 4) / 64;
    }

    @Override
    protected void buildCircuit() 
    {
        // Prepare plaintext wires for both SHA and encryption
        plainTextWiresInHex = 
            createProverWitnessWireArray( (plainTextInHex.length()/numHexDigitsPerInputWire) + ( (plainTextInHex.length()) % numHexDigitsPerInputWire != 0 ? 1 : 0) );


        // Use SHA256 gadget on plaintext wires
        Wire[] digest = new SHA256Gadget(plainTextWiresInHex, 4*numHexDigitsPerInputWire, plainTextSizeInBytes, false, true, "").getOutputWires();
        // Each hex digit is 4 bits, so she is declaring each input wire to have 4*16 bits, which is 64 bits, or 8 bytes

        makeOutputArray(digest);


        // Symmetric CBC sub-circuit logic
        // In other words: SymmetricEncryptionCBC gadget used on the plaintext wires and on the key and IV wires

        keyBitsWires = createProverWitnessWireArray(keyIVSize);
        ivBitsWires = createProverWitnessWireArray(keyIVSize);

        Wire[] plainTextBits = new WireArray(plainTextWiresInHex).getBits(64).asArray();

        // DEBUG by Edo
        System.out.println("plainTextWiresInHex length: " + plainTextWiresInHex.length); // 8
        System.out.println("plainTextBits length: " + plainTextBits.length); // 512 (= 8 * 64)

        cipherText = new SymmetricEncryptionCBCGadget(plainTextBits, keyBitsWires, ivBitsWires, cipherName).getOutputWires();

        makeOutputArray(cipherText);
    }

    @Override
    public void generateSampleInput(CircuitEvaluator evaluator)
    {
        // Set input wire

        for (int i = 0; i < plainTextWiresInHex.length; i++)
        {
            // String inputSubString = plainTextInHex.substring(i * 16, i * 16 + 16);
            // evaluator.setWireValue(plainTextWiresInHex[i], new BigInteger(inputSubString, 16));
            BigInteger sum = BigInteger.ZERO;
            for (int j = i * numHexDigitsPerInputWire; j < (i + 1) * numHexDigitsPerInputWire && j < plainTextInHex.length(); j+=2)
            {
                String substring = plainTextInHex.substring(j, j+2);
                BigInteger v = new BigInteger(substring, 16);
                sum = sum.add(v.shiftLeft(((j % numHexDigitsPerInputWire)/2) * 8));
            }
            evaluator.setWireValue(plainTextWiresInHex[i], sum);
        }

        // Convert hex representations of key to binary representation
        String binaryKey = new BigInteger(keyString, 16).toString(2);

        // Apply padding if needed
        int binaryKeyLength = binaryKey.length();
        if (binaryKeyLength != 128) 
        {
            int paddingLength = 128 - binaryKeyLength;
            for (int i = 0; i < paddingLength; i++) 
            {
                binaryKey = "0" + binaryKey;
            }
        }

        // Convert hex representations of iv to binary representation
        String binaryIV = new BigInteger(ivString, 16).toString(2);

        // Apply padding if needed
        int binaryIVLength = binaryIV.length();
        if (binaryIVLength != 128)
        {
            int paddingLength = 128 - binaryIVLength;
            for (int i = 0; i < paddingLength + 1; i++)
            {
                binaryIV = "0" + binaryIV;
            }
        }

        for (int j = 0; j < 128; j++)
        {
            evaluator.setWireValue(keyBitsWires[j], new BigInteger(binaryKey.substring(j, j + 1), 2));
            evaluator.setWireValue(ivBitsWires[j], new BigInteger(binaryIV.substring(j, j + 1), 2));
        }
    }

    public static void main(String[] args) 
    {
        String plainText64Bytes = 
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijkl";
        String plainText128Bytes = 
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijkl";
        String plainText256Bytes = 
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzab" +
            "cdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabc" +
            "defghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijkl";
        String plainText512Bytes = 
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijkl+" +
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijkl";
        String plainText1024Bytes = 
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijkl" +
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijkl" +
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijkl" +
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijkl" ;


        String plainText = plainText64Bytes;

        // We assume plainTextInHex to be of size in multiples of 16 (i.e. we assume the original string to be in size in multiples of 8)
        // This is because, for the symmetric encryption gadget, input size is 64bit words (=16 hex, =8 chars)
        String plainTextInHex = Util.stringToHex(plainText);
        
        // Key and IV were obtained from Speck256 test at: https://github.com/inmcm/Simon_Speck_Ciphers/blob/master/Python/simonspeckciphers/speck/speck.py
        String key = "1f1e1d1c1b1a19181716151413121110";
        String iv  = "0f0e0d0c0b0a09080706050403020100";


        PrivIdEx1CircuitGenerator circuitGenerator = new PrivIdEx1CircuitGenerator("prividex1", plainTextInHex, key, iv);

        circuitGenerator.generateCircuit();
        circuitGenerator.evalCircuit();
        circuitGenerator.prepFiles();
        circuitGenerator.runLibsnark();
    }
}
