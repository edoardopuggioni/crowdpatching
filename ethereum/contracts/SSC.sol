// Use Truffle with compiler version 0.5.16
pragma solidity = 0.5.16;

import "./DSC.sol";
import "./ESC.sol";

contract SSC
{
    struct distributorStruct
    {
        uint256 score;  // set to 0 by default
        uint lastReset; // set to 0 by default
    }

    address public owner; // address of owner (vendor or maybe third-party organization managing more vendors)
    mapping (address => bool) private childrenMap;
    mapping (address => distributorStruct) private distributorsMap;
    uint public distributorResetPeriod;

    event NewDSC(address addressDSC);
    event BadDSC(address addressDSC, address owner);
    event NewESC(address addressESC);

    constructor (uint distributorResetWeeks, uint distributorResetDays) public
    {
        owner = msg.sender;

        distributorResetPeriod = distributorResetWeeks * 1 weeks + distributorResetDays * 1 days;

        // DEBUG: use a short reset period for testing
        // distributorResetPeriod = 10 * 1 minutes;
    }

    function updateDistributorPeriod(uint distributorResetWeeks, uint distributorResetDays) public
    {
        if ( msg.sender == owner )
            distributorResetPeriod = distributorResetWeeks * 1 weeks + distributorResetDays * 1 days;
    }

    function getDistributorScore(address distributorAddress) public view returns (uint256)
    {
        if ( block.timestamp - distributorsMap[distributorAddress].lastReset > distributorResetPeriod )
            return 0;

        return distributorsMap[distributorAddress].score;
    }

    function incrementDistributorScore(address distributorAddress) public
    {
        if ( childrenMap[msg.sender] == false )
            return;

        // Increment distributor's score
        if ( block.timestamp - distributorsMap[distributorAddress].lastReset > distributorResetPeriod && distributorsMap[distributorAddress].score != 0)
        {
            distributorsMap[distributorAddress].score = 0;
            distributorsMap[distributorAddress].lastReset = block.timestamp;
        }
        if ( distributorsMap[distributorAddress].lastReset == 0 )
        {
            distributorsMap[distributorAddress].lastReset = block.timestamp;
        }
        distributorsMap[distributorAddress].score++;
    }

    function deployDSC(uint _expWeeks, uint _expDays, bytes32 updateHash,
        address[] memory objectsAddresses, bytes32 pkgHash, bytes32 vkHash,
        uint256 singleRewardAmount, uint256 singleFinalRewardAmount) public payable
    {
        if ( msg.sender != owner )
            return;

        uint expWeeks = _expWeeks;
        uint expDays = _expDays;

        // Check if funds are enough compared to the rewards and the number of objects
        bool badDSC = (singleRewardAmount + singleFinalRewardAmount) * objectsAddresses.length > address(this).balance;

        if (badDSC)
        {
            // Bid will be created as already expired and the sender can withdraw funds right away
            expWeeks = 0;
            expDays = 0;

            // Even though it doesn't seem very practical, this should be the easiest way to send the
            // amount back to the sender: sending it back at this stage is not trivial
        }

        DSC db = (new DSC).value(msg.value)(msg.sender, address(this), expWeeks, expDays, updateHash, objectsAddresses, pkgHash, vkHash, singleRewardAmount, singleFinalRewardAmount);

        childrenMap[address(db)] = true;

        if (badDSC)
            emit BadDSC(address(db), msg.sender);
        else
            emit NewDSC(address(db));

    }

    function deployESC(bytes32 s, uint expWeeks, uint expDays) public payable
    {
        ESC eb = (new ESC).value(msg.value)(msg.sender, address(this), s, expWeeks, expDays);

        emit NewESC(address(eb));
    }
}