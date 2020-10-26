from speck import SpeckCipher
import binascii


# I need to set 128 bit key size and block size.
# Default is 128-bit encryption keys and block sizes.
# But I will enforce it explicitely anyway.


if(0):
    charString = "12345678" + "abcdefgh"
    # intValue = sum([ord(c) << (8 * x) for x, c in enumerate(reversed(charString))])
    intValue = sum([ord(c) << (8 * x) for x, c in enumerate(charString)]) # non-reversed
    print("Hex value: " + hex(intValue))
    exit()

if(0):

    # Key from Jsnark test
    # key =   |   key[1]     ||    key[0]    |
    # key =   0x0f0e0d0c0b0a09080706050403020100 

    # Key from NSA example at p.6 (https://nsacyber.github.io/simon-speck/implementations/ImplementationGuide1.1.pdf)
    # key = 0x000102030405060608090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f
    # key = 0x1f1e1d1c1b1a191817161514131211100f0e0d0c0b0a09080706050403020100

    # Key from PrivIdEx
    # key = 0x101112131415161718191a1b1c1d1e1f 
    key = 0x18191a1b1c1d1e1f1011121314151617 # swapped with respect to Jsnark

    # Plaintext from Jsnark test
    # pln =   | plaintext[1] || plaintext[0] |  
    # pln =   0x6c617669757165207469206564616d20

    # Plaintext from NSA example  
    # plaintextStr = "pooner. In those"
    # pln = 0x65736f6874206e49202e72656e6f6f70

    # My plaintext 2
    # pln = 0x68676665646362613837363534333231 
    pln = 0x38373635343332316867666564636261 # swapped with respect to Jsnark

    # Expected ciphertext for the Jsnark test
    #                             cipherText.get(1)    cipherText.get(0)
    # expectedCiphertextHexStr =   "a65d985179783265" + "7860fedf5c570d18"

    # Expected ciphertext for the NSA example
    # expectedCiphertextHexStr = "4109010405c0f53e4eeeb48d9c188f43"

    print("keyHex                   " + hex(key))
    print("plaintextHex             " + hex(pln))
    # print("expectedCiphertextHexStr   " + expectedCiphertextHexStr)

    cipher = SpeckCipher(key, key_size=128, block_size=128, mode="ECB")
    # cipher = SpeckCipher(key, key_size=256, block_size=128, mode="ECB")
    cpt = cipher.encrypt(pln)

    print("ciphertextHexStr         " + hex(cpt))

    exit()


# My plaintext 1
# plaintextStr = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijkl"
            
# Plaintext from Crypto++ (https://www.cryptopp.com/wiki/SPECK)
# plaintextStr = "CBC Mode Test"

# My plaintext 2
# plaintextStr = "12345678" + "abcdefgh"
# plaintextHexClean = "38373635343332316867666564636261"

# My plaintext 3: 64 Bytes * 4
plaintextStr = "AJDSFAHDVKJSMNakljfkajhsfkawasalwhertaljhg9835498hgbq98fdaojsas1" + \
               "94805uyh7wjkfssfsslkdq0lkjdlsj029vnaf13iaifdjlk2jrefdjlksdflafa1" + \
               "94805uyhlkdq0lkjdkdq0lkjdlsj029vnaf13iaifdjlk2jrefdjlksghdflafa1" + \
               "h7wjkfssfsslkdq0hlkdq0lkjdkdq0lkjdlsjhlkdq0lkjdkdq0lkaslkjdlsj01"

# Convert from string of chars to integer
plaintext = sum([ord(c) << (8 * x) for x, c in enumerate(reversed(plaintextStr))])
# plaintext = sum([ ord(c) << (8 * x) for x, c in enumerate(plaintextStr) ]) # non-reversed

# Convert from integer to hex string
plaintextHex = hex(plaintext)
plaintextHexClean = plaintextHex[2:-1]
plaintextHexCleanLen = len(plaintextHexClean)


print("plaintext string:           " + plaintextStr)
print("plaintext-hex:            " + plaintextHex)
print("plaintext-hex clean:        " + plaintextHexClean)
    
print("plaintext-hex clean length: " + str(plaintextHexCleanLen))


# Key and IV from PrivIdEx
# key = 0x1f1e1d1c1b1a19181716151413121110
# iv  = 0x0f0e0d0c0b0a09080706050403020100

# Key and IV from Crypto++
# key = 0xF36D4289293A07A0C1E3D8EAFBF83C6F
# iv  = 0x50650B834D62457D3D5CBFE9708EC927


# Key and IV from PrivIdEx with byte order according to NSA example
key = 0x101112131415161718191a1b1c1d1e1f # this worked!
# key = 0x18191a1b1c1d1e1f1011121314151617 # swapped with respect to Jsnark
iv  = 0x000102030405060708090a0b0c0d0e0f # this worked!
# iv  = 0x08090a0b0c0d0e0f0001020304050607 # swapped with respect to Jsnark
         
         
key = 0xd4c6ecb0035d57a13e59135d29c2d4c5 # same key as CrowdPatching
iv =  0x9c26393e3032af5461f181b91e6176e4 # same IV  as CrowdPatching
        

print("key:                      " + hex(key))
print("IV:                       " + hex(iv))


cipher = SpeckCipher(key, key_size=128, block_size=128, mode='CBC', init=iv)



############################ CBC ENCRYPTION ############################
print("\nENCRYPTION")

plaintextHexCleanNumChars = len(plaintextHexClean)
numHexDigitsPerBlock = 128 / 4
numBlocksInPlainText = plaintextHexCleanNumChars * 4 / 128

print("num blocks in plaintext: " + str(numBlocksInPlainText))

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

    print("current plaintext block:    " + curPlaintextBlock)

    curPlaintextInt = int(curPlaintextBlock, 16)

    curCiphertextInt = cipher.encrypt(curPlaintextInt)
    curCipertextHexString = hex(curCiphertextInt)

    curCiphertextHexClean = curCipertextHexString[2:-1]

    # Looking at the prints I noticed that one ciphertext was
    # "missing" one digit, meaning I need to do padding...
    l = len(curCiphertextHexClean)
    if(l != numHexDigitsPerBlock):
        padLen = numHexDigitsPerBlock-l
        j = 0
        while (j < padLen):
            curCiphertextHexClean = "0" + curCiphertextHexClean
            j += 1

    print("current ciphertext block:   " + curCiphertextHexClean)

    ciphertextHexClean += curCiphertextHexClean
  
    i += numHexDigitsPerBlock

print("FINAL ciphertext:           " + ciphertextHexClean)



############################ CBC DECRYPTION ############################
print("\nDECRYPTION")

# Reset IV
cipher.update_iv(iv)

decryptedHexClean = ""
i = 0
while (i < plaintextHexCleanNumChars):
    
    curCiphertextBlock = ciphertextHexClean[i:i+numHexDigitsPerBlock]
    print("current ciphertext block:   " + curCiphertextBlock)

    curCiphertextInt = int(curCiphertextBlock, 16)

    curDecryptedInt = cipher.decrypt(curCiphertextInt)
    curDecryptedHexString = hex(curDecryptedInt)

    curDecryptedHexClean = curDecryptedHexString[2:-1]

    # Padding
    l = len(curDecryptedHexClean)
    if(l != numHexDigitsPerBlock):
        padLen = numHexDigitsPerBlock-l
        j = 0
        while (j < padLen):
            curDecryptedHexClean = "0" + curDecryptedHexClean
            j += 1

    print("current decrypted block:    " + curDecryptedHexClean)

    decryptedHexClean += curDecryptedHexClean
  
    i += numHexDigitsPerBlock

print("FINAL decrypted:            " + decryptedHexClean)
print("ORIGINAL plaintext:         " + plaintextHexClean)