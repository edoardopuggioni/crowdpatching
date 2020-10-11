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


public class EncryptionVerificationCircuitGenerator extends CircuitGenerator
{
    private String plaintextHexString;
    private int plaintextNumHexDigits;
    private int plaintextNum64bitsWords;

    private String keyHexString;
    private String ivHexString;
    private final int blockNumHexDigits = 32;
    private final int blockNumBits = 128;

    private String expectedCiphertextHexString;
    private int expectedCiphertextNumHexDigits;
    private int ciphertextNum64bitsWords;

    private final int numHexDigitsIn64BitsWord = 64/4;

    // private Wire[] plaintextWires8BitsWords;
    private Wire[] plaintextWires64BitsWords;

    private Wire[] keyWires64BitsWords;
    private Wire[] ivWires64BitsWords;

    private Wire[] ciphertextWires64BitsWords;

    private Wire[] expectedCipherTextWires64bitsWords;

    // Currently only Speck cipher supported in CBC mode: hardcoded
    String cipherName = "speck128";


    // Constructor
    public EncryptionVerificationCircuitGenerator(String circuitName, String plaintextHexString, String keyHexString, String ivHexString, 
                                                    String expectedCiphertextHexString)
    {
        super(circuitName);

        this.plaintextHexString = plaintextHexString;
        this.plaintextNumHexDigits = plaintextHexString.length();
        this.plaintextNum64bitsWords = plaintextNumHexDigits*4/64;

        this.keyHexString = keyHexString;
        this.ivHexString = ivHexString;

        this.expectedCiphertextHexString = expectedCiphertextHexString;
        this.expectedCiphertextNumHexDigits = expectedCiphertextHexString.length();
        this.ciphertextNum64bitsWords = expectedCiphertextNumHexDigits*4/64;
    }


    @Override
    protected void buildCircuit() 
    {
        plaintextWires64BitsWords = createProverWitnessWireArray(plaintextNumHexDigits/numHexDigitsIn64BitsWord);

        keyWires64BitsWords = createProverWitnessWireArray(blockNumHexDigits/numHexDigitsIn64BitsWord);
        ivWires64BitsWords = createProverWitnessWireArray(blockNumHexDigits/numHexDigitsIn64BitsWord);

        expectedCipherTextWires64bitsWords = createInputWireArray(expectedCiphertextNumHexDigits/numHexDigitsIn64BitsWord);

        ciphertextWires64BitsWords = new Wire[0];
        
        Wire[] expandedKey = Speck128CipherGadget.expandKey(keyWires64BitsWords);
        Wire[] prevCipher = new Wire[2];
        prevCipher[0] = ivWires64BitsWords[0];
        prevCipher[1] = ivWires64BitsWords[1];
        for( int i = 0; i < plaintextWires64BitsWords.length-2+1; i+= 2 )
        {
            Wire[] xored = new Wire[2];
            xored[0] = plaintextWires64BitsWords[i].xorBitwise(prevCipher[0], 64);
            xored[1] = plaintextWires64BitsWords[i+1].xorBitwise(prevCipher[1], 64);

            prevCipher = new Speck128CipherGadget(xored, expandedKey).getOutputWires();

            ciphertextWires64BitsWords = Util.concat(ciphertextWires64BitsWords, prevCipher);
        }

        // makeOutputArray(ciphertextWires64BitsWords);
        
        for (int i = 0; i < ciphertextWires64BitsWords.length; i++)
        {
            addEqualityAssertion(ciphertextWires64BitsWords[i], expectedCipherTextWires64bitsWords[i]);
        }
    }


    @Override
    public void generateSampleInput(CircuitEvaluator evaluator)
    {
        // Transform plaintext bytes string into 64 bits words as in NSA example
        // I need to consider the hex digits two by two
        String hexString128BitsWord = "";
        String subStr;
        int wireIndex = plaintextWires64BitsWords.length-1; // Start filling wires from 0 instead of the end to try swapping the 64 bits words
        for( int i = 0; i < plaintextNumHexDigits-2+1; i += 2 )
        {
            subStr = plaintextHexString.substring(i, i+2);
            hexString128BitsWord = subStr + hexString128BitsWord;

            if( hexString128BitsWord.length() == blockNumHexDigits )
            {                
                subStr = hexString128BitsWord.substring(16, 32);
                System.out.print("Plaintext 64-bit word: " + subStr);
                System.out.println(" ---> plaintextWires64BitsWords[" + (wireIndex-1) + "]");
                BigInteger v = new BigInteger(subStr, 16);
                evaluator.setWireValue(plaintextWires64BitsWords[wireIndex-1], v);

                subStr = hexString128BitsWord.substring(0, 16);
                System.out.print("Plaintext 64-bit word: " + subStr);
                System.out.println(" ---> plaintextWires64BitsWords[" + wireIndex + "]");
                v = new BigInteger(subStr, 16);
                evaluator.setWireValue(plaintextWires64BitsWords[wireIndex], v);

                wireIndex -= 2;

                hexString128BitsWord = "";
            }
        }

        // The ciphertext is different: I must not do the transformation because I will compare it with the result in Python, which is without transf.
        // The consequence is that I do not start from the end index, but from the beginning.
        wireIndex = 0;
        for( int i = 0; i < expectedCiphertextNumHexDigits-2+1; i += 2 )
        {
            subStr = expectedCiphertextHexString.substring(i, i+2);
            hexString128BitsWord = hexString128BitsWord + subStr;

            if( hexString128BitsWord.length() == blockNumHexDigits )
            {
                subStr = hexString128BitsWord.substring(16, 32);
                System.out.print("Expected ciphertext 64-bit word: " + subStr);
                System.out.println(" ---> expectedCipherTextWires64bitsWords[" + wireIndex + "]");
                BigInteger v = new BigInteger(subStr, 16);
                evaluator.setWireValue(expectedCipherTextWires64bitsWords[wireIndex], v);

                subStr = hexString128BitsWord.substring(0, 16);
                System.out.print("Expected ciphertext 64-bit word: " + subStr);
                System.out.println(" ---> expectedCipherTextWires64bitsWords[" + (wireIndex+1) + "]");
                v = new BigInteger(subStr, 16);
                evaluator.setWireValue(expectedCipherTextWires64bitsWords[wireIndex+1], v);

                wireIndex += 2;

                hexString128BitsWord = "";
            }
        }


        /*
            Similarly for the key and the IV I will trasnform the hex strings in new hex
            strings with the same trasnformation as in the NSA example.
        */

        wireIndex = keyWires64BitsWords.length-1;
        for( int i = 0; i < blockNumHexDigits-2+1; i += 2 )
        {
            subStr = keyHexString.substring(i, i+2);
            hexString128BitsWord = subStr + hexString128BitsWord;

            if( hexString128BitsWord.length() == blockNumHexDigits )
            {
                subStr = hexString128BitsWord.substring(16, 32);
                System.out.print("Key 64-bit word: " + hexString128BitsWord);
                System.out.println(" ---> keyWires64BitsWords[" + (wireIndex-1) + "]");
                BigInteger v = new BigInteger(subStr, 16);
                evaluator.setWireValue(keyWires64BitsWords[wireIndex-1], v);

                subStr = hexString128BitsWord.substring(0, 16);
                System.out.print("Key 64-bit word: " + hexString128BitsWord);
                System.out.println(" ---> keyWires64BitsWords[" + (wireIndex) + "]");
                v = new BigInteger(subStr, 16);
                evaluator.setWireValue(keyWires64BitsWords[wireIndex], v);

                wireIndex -= 2;

                hexString128BitsWord = "";
            }
        }

        wireIndex = ivWires64BitsWords.length-1;
        for( int i = 0; i < blockNumHexDigits-2+1; i += 2 )
        {
            subStr = ivHexString.substring(i, i+2);
            hexString128BitsWord = subStr + hexString128BitsWord;

            if( hexString128BitsWord.length() == blockNumHexDigits )
            {
                subStr = hexString128BitsWord.substring(16, 32);
                System.out.print("IV 64-bit word: " + hexString128BitsWord);
                System.out.println(" ---> ivWires64BitsWords[" + (wireIndex-1) + "]");
                BigInteger v = new BigInteger(subStr, 16);
                evaluator.setWireValue(ivWires64BitsWords[wireIndex-1], v);

                subStr = hexString128BitsWord.substring(0, 16);
                System.out.print("IV 64-bit word: " + hexString128BitsWord);
                System.out.println(" ---> ivWires64BitsWords[" + (wireIndex) + "]");
                v = new BigInteger(subStr, 16);
                evaluator.setWireValue(ivWires64BitsWords[wireIndex], v);

                wireIndex -= 2;

                hexString128BitsWord = "";
            }
        }
    }


    public static void main(String[] args) 
    {
        String expectedCiphertextHexString;

        /*
            Assumption by Hasini: plaintext number of hex digits in multiples of 16 (i.e. original
            string size in multiples of 8). This is because for the symmetric encryption gadget
            input size is 64bit words (=16 hex, =8 chars).

            For me it seems like the assumpion should be different. I need the plaintext to be in
            multiple of the block size of the speck cipher, which is 128 bits. 128 bits means 16
            bytes or chars, or 32 hex digits.
        */

        // 64 bytes plaintext = 512 bits. 512 / 128 = 4 blocks.
        // String plaintextCharString = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijkl";
        // expectedCiphertextHexString = 
        //     "1c806416ca68daa56014726f1a22be576caafe3c6cb288c061d94b7e669cc40c9035ff33845eebf65844ce5beaa02c7bda788b3366e6a459e0b2a1503cd299e3";

        // Plaintext with size of a block (128 bits)
        // String plaintextCharString = "12345678" + "abcdefgh";
        // plaintextHexString: 31323334353637386162636465666768
        // expectedCiphertextHexString = "a84e5c54dcde523331a789134a166eb7";

        // 64 Bytes * 4
        String plaintextCharString =  "AJDSFAHDVKJSMNakljfkajhsfkawasalwhertaljhg9835498hgbq98fdaojsas1" +
                                      "94805uyh7wjkfssfsslkdq0lkjdlsj029vnaf13iaifdjlk2jrefdjlksdflafa1" +
                                      "94805uyhlkdq0lkjdkdq0lkjdlsj029vnaf13iaifdjlk2jrefdjlksghdflafa1" +
                                      "h7wjkfssfsslkdq0hlkdq0lkjdkdq0lkjdlsjhlkdq0lkjdkdq0lkaslkjdlsj01";
        expectedCiphertextHexString = "a25617af6e250a234a46bc2d16931afba69e789d90f4f79b80a8ce554b57e2c9" +
                                      "d3129c7136fa13a9448335e5e4228aedf7ad4aa13f757c7830e40cb18d34a54a" +
                                      "387fc14ee5a3e12641cbade60867c6fc20803b9ec5750c3b4ca73307ead30b70" +
                                      "da5fb8e8b8e6136c904af51feeebc3dc6620174579044b7ea0bb93b1d1188be5" +
                                      "8bffa0bcbe56358e2cafa21b86837b08a62d3fea20b860393692531e541c70e3" +
                                      "f8522e9762154de8833c95da1a7f4f30acbf2dc096e3626a62360574348a478f" +
                                      "5c2d1a73cc7b9b799b110a9c0792134a7d5cecb4e5782e535b24a3577e45be39" +
                                      "88a00e51db6504b86d4c5a0f01542718cf1ad1a284ca1769110e92c59ee6e723";


        String plaintextHexString = Util.stringToHex(plaintextCharString);
        System.out.println("plaintextHexString: " + plaintextHexString);

        
        // Key and IV from PrivIdEx
        String keyHexString = "1f1e1d1c1b1a19181716151413121110";
        String ivHexString  = "0f0e0d0c0b0a09080706050403020100";


        EncryptionVerificationCircuitGenerator circuitGenerator = 
            new EncryptionVerificationCircuitGenerator("encryption", plaintextHexString, keyHexString, ivHexString, expectedCiphertextHexString);


        circuitGenerator.generateCircuit();
        circuitGenerator.evalCircuit();


        // Now I want to print the output array of the CBC gadget, aka the result ciphertext
        // Uncomment the makeOutputArray() function inside buildCircuit to make this work

            // CircuitEvaluator evaluator = circuitGenerator.getCircuitEvaluator();
            // ArrayList<Wire> ciphertextOutputWires = circuitGenerator.getOutWires();

            // BigInteger wireValue;
            // String wireValueHexString;
            // System.out.println("Output ciphertext:");
            // for(int i = 0; i < ciphertextOutputWires.size(); i++)
            // {
            //     wireValue = evaluator.getWireValue(ciphertextOutputWires.get(i));
            //     // System.out.println(wireValue);
            //     wireValueHexString = wireValue.toString(16);   
            //     System.out.println(wireValueHexString);
            // }

            // // Print the expected ciphertext for comparison
            // System.out.println("\nExpected ciphertext: " + expectedCiphertextHexString);
        
        // If you uncomment the above part you need to use makeOutputArray() again

        
        circuitGenerator.prepFiles();
        circuitGenerator.runLibsnark();
    }
}





