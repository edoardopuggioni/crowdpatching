// Use Truffle with compiler version 0.5.16
pragma solidity = 0.5.16;

contract ESC
{
    bytes32 s;
    address public owner; // address of owner (second-gen distributor)
    address public parentContract; // address of factory contract
    uint public expiration;
    bool rReceived;
    bytes32 rStored;

    event KeyRevealed(bytes32 r);

    constructor(address _owner, address _parentContract, bytes32 _s, uint expWeeks, uint expDays) /*public*/ public payable
    {
        // owner = msg.sender;
        owner = _owner;
        parentContract = _parentContract;

        s = _s;

        expiration = /*now*/ block.timestamp + expWeeks * 1 weeks + expDays * 1 days;
        // expiration = now + 100; // test expiration with very short time
        // expiration = now + 10 * 1 minutes; // test expiration with relatively short time: 10 minutes
    }

    function getBalance() public view returns (uint256)
    {
        return address(this).balance;
    }

    function validateExchange(bytes16 t, bytes32 r) public returns (string memory)
    {
        if (/*now*/ block.timestamp > expiration)
            return "ESC expired";

        if (rReceived == true)
            return "Key already received";

        if ( r != sha256(abi.encodePacked(t, msg.sender)) )
            return "Invalid r: must be SHA256(t || msg.sender)";

        if ( s != sha256(abi.encodePacked(r)) )
            return "Invalid s: must be SHA256(r)";


        // Transfer the cryptocurrency reward to the first-gen distributor
        msg.sender.transfer(address(this).balance);

        // Save the key into the variable state
        rReceived = true;
        rStored = r;

        // Emit event to announce the key publication
        emit KeyRevealed(r);

        return "Exchange was successfully validated";
    }

    function getKey() public view returns (bytes32)
    {
        return rStored;
    }

    function withdrawFunds() public
    {
        if (/*now*/ block.timestamp < expiration)
            return;

        if (msg.sender != owner)
            return;

        msg.sender.transfer(address(this).balance);

        return;
    }
}