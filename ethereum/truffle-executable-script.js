var SSC = artifacts.require("./SSC.sol");
var DSC = artifacts.require("./DSC.sol");

// Print balance if using Ganache CLI (instead of GUI)
async function printBalance(accounts, i)
{
    let balance;
    var f;

    balance = await web3.eth.getBalance(accounts[i]);
    f = parseFloat(web3.utils.fromWei(balance, 'ether').toString());
    console.log("$ Account A" + i + ": " + f.toFixed(2));
}

/*
    IMPORTANT: here we assume SSC was already deployed by account with
    index 6 (check configuration in migrations/2_deploy_contracts.js)
*/
module.exports = async function(callback)
{
    let ret;

    let accounts = await web3.eth.getAccounts();

    // Get reference to deployed SSC instance
    let ssc = await SSC.deployed();

    // Read and print reset period from SSC
    ret = await ssc.distributorResetPeriod.call();
    console.log("> Distributors reset period: " + ret.toString());

    // Update reset period
    ret = await ssc.updateDistributorPeriod("0", "1", {from: accounts[2]});

    // Check that reset period was indeed updated
    ret = await ssc.distributorResetPeriod.call();
    console.log("> Distributors reset period: " + ret.toString());


    // console.log("");
    process.exit(0);
}