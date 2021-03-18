pragma solidity ^0.4.24;

contract basicTransfer {
    function tnkDeduct(uint256 Nmode ,uint256 Tkn,uint256 Psell) public pure returns(uint256 val)  {
        if (Nmode == 1) {
        Tkn = Tkn - Psell;
        return(Tkn); 
       }
    else {
        Tkn = Tkn + Psell;
        return(Tkn);
        }   
    }
    
}