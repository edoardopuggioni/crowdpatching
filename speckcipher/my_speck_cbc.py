import sys

from speck import SpeckCipher
import binascii

    
key = 0xd4c6ecb0035d57a13e59135d29c2d4c5 # same key as CrowdPatching
iv =  0x9c26393e3032af5461f181b91e6176e4 # same IV  as CrowdPatching

cipher = SpeckCipher(key, key_size=128, block_size=128, mode='CBC', init=iv)

numHexDigitsPerBlock = int(128/4)


##################### CBC ENCRYPTION #####################
if(1):

    # Plaintext size: 64 Bytes * 4 = 256 bytes
    # plaintextStr = "AJDSFAHDVKJSMNakljfkajhsfkawasalwhertaljhg9835498hgbq98fdaojsas1" + \
    #                "94805uyh7wjkfssfsslkdq0lkjdlsj029vnaf13iaifdjlk2jrefdjlksdflafa1" + \
    #                "94805uyhlkdq0lkjdkdq0lkjdlsj029vnaf13iaifdjlk2jrefdjlksghdflafa1" + \
    #                "h7wjkfssfsslkdq0hlkdq0lkjdkdq0lkjdlsjhlkdq0lkjdkdq0lkaslkjdlsj01"
    plaintextStr = "AJDSFAHDVKJSMNakljfkajhsfkawasalwhertaljhg9835498hgbq98fdaojsas194805uyh7wjkfssfsslkdq0lkjdlsj029vnaf13iaifdjlk2jrefdjlksdflafa194805uyhlkdq0lkjdkdq0lkjdlsj029vnaf13iaifdjlk2jrefdjlksghdflafa1h7wjkfssfsslkdq0hlkdq0lkjdkdq0lkjdlsjhlkdq0lkjdkdq0lkaslkjdlsj01"

    print("Plaintext string: " + plaintextStr)

    # Convert from string of chars to integer
    plaintext = sum([ord(c) << (8 * x) for x, c in enumerate(reversed(plaintextStr))])

    # Convert from integer to hex string
    plaintextHex = hex(plaintext)
    plaintextHexClean = plaintextHex[2:] # Prima stavo tagliando l'ultima cifra...
    # print("\n plaintextHex:\n" + plaintextHex)

    # Alternatively you can directly assign the hex string of the plaintext
    # plaintextHexClean = ""

    # Concatenate string with itself to obtain larger files
    plaintextHexCleanUNIT = plaintextHexClean
    plaintextHexClean = ""
    cycles = 20
    # cycles = 800
    # cycles = 12288
    for x in range(cycles): # Execute this loop a number of times EQUAL to cycles
        plaintextHexClean += plaintextHexCleanUNIT

    print("\n plaintextHexClean:\n" + plaintextHexClean)

    plaintextHexCleanLen = len(plaintextHexClean)
    plaintextHexCleanNumChars = len(plaintextHexClean)
    numBlocksInPlainText = plaintextHexCleanNumChars * 4 / 128

    print("Number of blocks in plaintext: " + str(numBlocksInPlainText))

    ciphertextHexClean = ""
    i = 0
    while (i < plaintextHexCleanNumChars):
        
        curPlaintextBlock = plaintextHexClean[i:i+numHexDigitsPerBlock]

        # Padding in case plaintext is not multiple of blocksize
        l = len(curPlaintextBlock)
        if(l != numHexDigitsPerBlock):
            padLen = numHexDigitsPerBlock-l
            j = 0
            while (j < padLen):
                curPlaintextBlock = "0" + curPlaintextBlock
                j += 1

        # print("Plaintext block currently being encrypted:    " + curPlaintextBlock)

        curPlaintextInt = int(curPlaintextBlock, 16)

        curCiphertextInt = cipher.encrypt(curPlaintextInt)
        curCipertextHexString = hex(curCiphertextInt)

        curCiphertextHexClean = curCipertextHexString[2:]

        # Looking at the prints I noticed that one ciphertext was
        # "missing" one digit, meaning I need to do padding...
        # PADDING IS PRODUCING PROBLEMS I WILL TRY TO COMMENT IT AND USE MULTIPLE SIZE PLAINTEXT !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        l = len(curCiphertextHexClean)
        if(l != numHexDigitsPerBlock):
            padLen = numHexDigitsPerBlock-l
            j = 0
            while (j < padLen):
                curCiphertextHexClean = "0" + curCiphertextHexClean
                j += 1

        # print("Current block encryption result:   " + curCiphertextHexClean)

        ciphertextHexClean += curCiphertextHexClean
    
        i += numHexDigitsPerBlock


    print("Final ciphertext:\n" + ciphertextHexClean)


##################### CBC DECRYPTION #####################
if(0):

    # Reset IV
    cipher.update_iv(iv)

    # Set ciphertext manually
    ciphertextHexClean = "a52e5f3ab41c9c15c92d242c9ab7a2286981a302ce363c7b2edacd88d16f16f4509be4bcd2dfdaf3861f23069e173a59a4bac92dfeea36815f61a3527421a2fddaf6dae55733e27987e531174725cdf033c3eedcd5a8326b9c3018edd120a81bce99aca01537118c9a743b8b3316cd455287de9ab56110ba65fe83d25055379380921813de9acd89ab378d11989d63a499069fdb719fa418efeac59f13dd898ba751f5d986f084e2fa5af0d47a2cc221bd04026c6590cfbecbe38e04a1d6574442cdeb0af749d46a9e58ca97965e903798c2ce0b6e341fdf3fd8048f3b13c4e88a78522c703b756c5d9a939cc62a376bfbc42b5df6e8c2816ce1f43281355945"
    # Or comment this to use the final ciphertext of the CBC encryptin above

    ciphertextHexCleanNumChars = len(ciphertextHexClean)

    decryptedHexClean = ""
    i = 0
    while (i < ciphertextHexCleanNumChars):
        
        curCiphertextBlock = ciphertextHexClean[i:i+numHexDigitsPerBlock]

        # print("Ciphertext block currently being decrypted:   " + curCiphertextBlock)

        curCiphertextInt = int(curCiphertextBlock, 16)

        curDecryptedInt = cipher.decrypt(curCiphertextInt)
        curDecryptedHexString = hex(curDecryptedInt)

        curDecryptedHexClean = curDecryptedHexString[2:-1]

        # Padding
        # PADDING IS PRODUCING PROBLEMS I WILL TRY TO COMMENT IT AND USE MULTIPLE SIZE PLAINTEXT !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        # l = len(curDecryptedHexClean)
        # if(l != numHexDigitsPerBlock):
        #     padLen = numHexDigitsPerBlock-l
        #     j = 0
        #     while (j < padLen):
        #         curDecryptedHexClean = "0" + curDecryptedHexClean
        #         j += 1

        # print("Current block decryption result:    " + curDecryptedHexClean)

        decryptedHexClean += curDecryptedHexClean
    
        i += numHexDigitsPerBlock

    print("\nFinal decrypted plaintext:\n" + decryptedHexClean)

    # Uncomment if encryption is enabled
    print("\nOriginal plaintext:\n" + plaintextHexClean)