// Use Truffle with compiler version 0.5.16
pragma solidity = 0.5.16;

import "./SSC.sol";

contract DSC
{
    struct objectStruct
    {
        bytes32 r;
        bool isMember; // set to false by default
        bool rSet;     // set to false by default
        bool finalDelivery;
    }

    // State variables
    address public owner;          // address of owner (manufacturer)
    address public parentContract; // address of SSC
    uint public expiration;
    uint256 public singleRewardAmount;
    uint256 public singleFinalRewardAmount;
    bytes32 public updateHash;
    mapping (address => objectStruct) private objectsMap;
    bytes32 public pkgHash;
    bytes32 public vkHash;

    event KeyRevealed(address objectAddress, bytes32 r);

    constructor (address _owner, address _parentContract, uint expWeeks, uint expDays, bytes32 _updateHash, address[] memory objectsAddresses,
        bytes32 _pkgHash, bytes32 _vkHash, uint256 _singleRewardAmount, uint256 _singleFinalRewardAmount) public payable
    {
        owner = _owner;
        parentContract = _parentContract;

        if ( expWeeks + expDays == 0 )
        {
            expiration = 0;
        }
        else
        {
            expiration = now + expWeeks * 1 weeks + expDays * 1 days;
            expiration = now + 10 * 1 minutes; // test expiration with short time: 10 minutes
        }

        updateHash = _updateHash;

        uint numObjects = objectsAddresses.length;

        for (uint i = 0; i < numObjects; i++)
        {
            objectsMap[objectsAddresses[uint(i)]].isMember = true;
        }

        singleRewardAmount = _singleRewardAmount;
        singleFinalRewardAmount = _singleFinalRewardAmount;

        pkgHash = _pkgHash;

        vkHash = _vkHash;
    }

    function getBalance() public view returns (uint256)
    {
        return address(this).balance;
    }

    function checkObjectExistence(address objectAddress) public view returns (bool)
    {
        return objectsMap[objectAddress].isMember;
    }

    function splitSignature(bytes memory sig) public pure returns (uint8, bytes32, bytes32)
    {
        bytes32 r;
        bytes32 s;
        uint8   v;

        assembly
        {
           // first 32 bytes, after the length prefix
           r := mload(add(sig, 32))

           // second 32 bytes
           s := mload(add(sig, 64))

           // final byte (first byte of the next 32 bytes)
           v := byte(0, mload(add(sig, 96)))
        }

        return (v, r, s);
    }

    function validateDelivery(address objectAddress, bytes16 t, bytes32 r, bytes32 s, bytes memory sig) public returns (string memory)
    {
        if (now > expiration)
            return "DSC expired";

        if (checkObjectExistence(objectAddress) == false)
            return "Object not in the list";

        if (objectsMap[objectAddress].rSet == true )
            return "Update already delivered to this object";

        if ( r != sha256(abi.encodePacked(t, objectAddress, msg.sender)) )
            return "Invalid r: must be SHA256(t || msg.sender)";

        if ( s != sha256(abi.encodePacked(r)) )
            return "Invalid s: must be SHA256(r)";

        if ( sig.length != 65 )
            return "Invalid signature length";

        uint8 vSig;
        bytes32 rSig;
        bytes32 sSig;

        (vSig, rSig, sSig) = splitSignature(sig);
        bytes32 message = keccak256(abi.encodePacked(updateHash, s));
        address signerAddress = ecrecover(message, vSig, rSig, sSig);

        if (signerAddress != objectAddress)
            return "Invalid signature";

        // At this point the delivery is validated completely

        // Transfer the cryptocurrency reward to the distributor
        msg.sender.transfer(singleRewardAmount);

        // Save the key into the variable state mapping
        objectsMap[objectAddress].r = r;
        objectsMap[objectAddress].rSet = true;

        // Call the parent function to update the distributor's score
        SSC ssc = SSC(parentContract);
        ssc.incrementDistributorScore(msg.sender);

        // Emit event to announce the key publication
        emit KeyRevealed(objectAddress, r);

        return "Delivery was successfully validated";
    }

    function keyWasRevealed(address objectAddress) public view returns (bool)
    {
        return objectsMap[objectAddress].rSet;
    }

    function getKey(address objectAddress) public view returns (bytes32)
    {
        return objectsMap[objectAddress].r;
    }

    function validateFinalDelivery(address objectAddress, bytes memory sig) public returns (string memory)
    {
        if (now > expiration)
            return "DSC expired";

        if (checkObjectExistence(objectAddress) == false)
            return "Object not in the list";

        if ( objectsMap[objectAddress].finalDelivery == true )
            return "Final delivery already happened for this object";

        if ( sig.length != 65 )
            return "Invalid signature length";

        uint8 vSig;
        bytes32 rSig;
        bytes32 sSig;

        (vSig, rSig, sSig) = splitSignature(sig);
        bytes32 message = keccak256(abi.encodePacked(updateHash, msg.sender));
        address signerAddress = ecrecover(message, vSig, rSig, sSig);

        if (signerAddress != objectAddress)
            return "Invalid signature";

        // At this point the final delivery is validated completely

        if ( objectsMap[objectAddress].rSet == false )
        {
            msg.sender.transfer(singleRewardAmount);
            objectsMap[objectAddress].rSet = true;
        }

        msg.sender.transfer(singleFinalRewardAmount);

        // Update the final delivery statys for this object
        objectsMap[objectAddress].finalDelivery = true;

        return "Final delivery was successfully validated";
    }

    function withdrawFunds() public
    {
        if (now < expiration)
            return;

        if (msg.sender != owner)
            return;

        msg.sender.transfer(address(this).balance);

        return;
    }
}