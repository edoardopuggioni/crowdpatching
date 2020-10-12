const Migrations = artifacts.require("Migrations");

module.exports = function(deployer, networks, accounts) {
  deployer.deploy(Migrations, {from: accounts[1]});
};
