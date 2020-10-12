const EthCrypto = require("eth-crypto");


// Check signature of the update hash by the vendor

const expectedAddress = "0x03974b2BD57D4971e6d4b02a8FbEc09B25DDE395";

const message = EthCrypto.hash.keccak256([
    { type: "bytes32", value: "0x8457612244c5f5b7b2147b42ddbf859d68a78560d3f35ae4d411690cadd9a794" }, // update digest (see contract deployer)
]);

const signature = "0xf86dc1c196b49749c7863314704d4e3adacfb8b722228c6bfba2eff6eed71aa14f57776180c2fde6fd7d4140a5f4b977f1766c0941eee556a70630df07e853b71c";

const signerAddress = EthCrypto.recover(signature, message);


if( signerAddress == expectedAddress )
    console.log(`Signature is valid!`);
else
    console.log(`Signature is NOT valid`);

console.log(`message:          ${message}`);
console.log(`signature:        ${signature}`);
console.log(`expected signer:  ${expectedAddress}`);
console.log(`recovered signer: ${expectedAddress}`);