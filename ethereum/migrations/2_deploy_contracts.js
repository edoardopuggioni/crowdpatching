var SSC = artifacts.require("./SSC.sol");


module.exports = function(deployer, networks, accounts)
{	
	var distributorResetWeeks = "4";
	var distributorResetDays = "3";

	// Deploy SSC using account 6 (referring to the index indicated on Ganache)
	deployer.deploy(SSC, distributorResetWeeks, distributorResetDays, {from: accounts[6]});
};