const EthCrypto = require("eth-crypto");

signerIdentity = EthCrypto.createIdentity();

// console.log(`privateKey: ${signerIdentity.privateKey}`);
// console.log(`publicKey:    ${signerIdentity.publicKey}`);
// console.log(`address:    ${signerIdentity.address}`);


// Set signer manually to object 1
// signerIdentity.privateKey = "0xbbb4e8d73d82d1ede667b0443c4ee1316c6c1f6273c358147072c5bcf5a8106f";
// signerIdentity.publicKey  = "da86265ce25a9d8172e89d810bd83b7c037874427e325a2dfad67c6421ff2bdf2f131428fcf72902650a4816288783e958de154ea237813f7185e9b69fc12d2b";
// signerIdentity.address    = "0xE5f0f1B84c3d7e0a9742da439BA74b91e1C40ca5";

// Set signer manually to object 2
signerIdentity.privateKey = "0x345ba243482403250de78f5e9a86a145151fc3509ec9acbb2f65cbb8018c980e";
signerIdentity.publicKey  = "5a541aa0f7920b76940abaaa9ec4c3d8fae4ca863ab0ae7f02cd475ae248026f87a596e9c8e02df2bb12886a284a290f841af3898cc15809931d4e49b289e521";
signerIdentity.address    = "0xC7117aa7c9Cb88025E97c8BbC094096Ad8601998";

// // Set signer manually to object 3
// signerIdentity.privateKey = "0xfcf63618b7230cc5d36bcbaac116af82bc5725f9ff0de57c45d2a708e5b584d0";
// signerIdentity.publicKey  = "3b8e64ad84a48e2e6bad45c57cf309d357d3146b1b704ba5ebdc9483bfeb53e193286ecb262a573bf49a39bf01fab418d8e042e271a94d80ed7f152dbdffa589";
// signerIdentity.address    = "0xf6A3c244edB03C8D5009b1d7462a0f49A3F3946F";

// Set signer manually to vendor
// signerIdentity.privateKey = "0x387805b4b108a79bce5ba21de955c0a698ff90fa85e28ce90d0db71009bbe801"; // taken from Ganache (vendor address)
// signerIdentity.publicKey  = EthCrypto.publicKeyByPrivateKey(signerIdentity.privateKey);
// signerIdentity.address    = "0x03974b2BD57D4971e6d4b02a8FbEc09B25DDE395";
// signerIdentity.publicKey  = EthCrypto.publicKey.toAddress(signerIdentity.publicKey); this (strangely) gives a different address: use above instead 


// Object signs update digest and parameter s
const message = EthCrypto.hash.keccak256([
    { type: "bytes32", value: "0x8457612244c5f5b7b2147b42ddbf859d68a78560d3f35ae4d411690cadd9a794" }, // update digest (see contract deployer)
    { type: "bytes32", value: "0xac9e59b4e3ca66a4cb1cfb633183de3f6b6cf244b5c70da45fda3228ce71a814" }, // parameter s = SHA256(r)
]);

// Object signs update digest and parameter ALTERNATIVE parameter s (see DeliveryBid6 on OneNote)
// const message = EthCrypto.hash.keccak256([
//     { type: "bytes32", value: "0x8457612244c5f5b7b2147b42ddbf859d68a78560d3f35ae4d411690cadd9a794" }, // update digest (see contract deployer)
//     { type: "bytes32", value: "0xf979fb35f744f470a42957cf001901ed30542d03d5f028118292b399a27b5a84" }, // parameter s = SHA256(r)
// ]);


// Object signs update digest and hub's address (see DeliveryBid7 on OneNote)
// const message = EthCrypto.hash.keccak256([
//     { type: "bytes32", value: "0x8457612244c5f5b7b2147b42ddbf859d68a78560d3f35ae4d411690cadd9a794" }, // update digest (see contract deployer)
//     { type: "address", value: "0x60BBFBF2FE64891Ae2215Dafeb3fc3E14FB7751B" },                         // address of the hub
// ]);


// The vendor signs the concatenation of the update digest and verification key
// const message = EthCrypto.hash.keccak256([
//     { type: "bytes32", value: "0x8457612244c5f5b7b2147b42ddbf859d68a78560d3f35ae4d411690cadd9a794" }, // update digest (see contract deployer)
// ]);


const signature = EthCrypto.sign(signerIdentity.privateKey, message);

// console.log(`message:        ${message}`);
console.log(`signature:      ${signature}`);
console.log(`signer address: ${signerIdentity.address}`);