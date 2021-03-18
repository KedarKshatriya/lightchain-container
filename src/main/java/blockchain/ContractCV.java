package blockchain;

import evm.Contract;
import evm.ContractTransaction;
import skipGraph.NodeInfo;
import java.rmi.RemoteException;

/** 
* ContractCV contains the isCorrect() function which is used to 
* interact with the wrapper function TransctSol() 
*/
class ContractCV extends CorrectnessVerifier {

    Contract ct = new Contract();
    ContractTransaction tesq = new ContractTransaction();

    public ContractCV(LightChainNode owner) throws RemoteException {
        super(owner);
    }
    
    /** 
    * Checks correctness of transaction by passing values to smart contracts.
    * Returns true if the conditions are met.
    * @param t transaction whose correctness is to be verified
    * @return true if transaction is correct, or false if not
    */
    @Override
    public boolean isCorrect(Transaction t) {
        try {
            NodeInfo ndowner = owner.searchByNumID(t.getOwner());
            LightChainRMIInterface ownerRMI = owner.getLightChainRMI(ndowner.getAddress());
            int token1 = ownerRMI.getToken();
            tesq.setup();
            boolean value = tesq.TransctSol(token1, ct.contractName1, ct.functname1);
            return value; 
        } 
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
    
    // Code for 2nd smart contract
    /*
        @Override
        public boolean isCorrect(Transaction t) {
            try {
                NodeInfo ndowner = owner.searchByNumID(t.getOwner());
                LightChainRMIInterface ownerRMI = owner.getLightChainRMI(ndowner.getAddress());
                int token1 = ownerRMI.getToken();
                int Nmode = owner.Tmode;
                owner.logger.debug("mode: "+Nmode);
                int Psell = 10;
                tesq.setup();
                int value = tesq.basicTransfer(token1, ct.contractName2, ct.functname2, Nmode, Psell);
                owner.logger.debug(value); 
                owner.view.updateToken(t.getOwner(),value);
                int newToken = owner.view.getToken(t.getOwner());
                owner.logger.debug("updated value "+ newToken);
                if (Nmode == 1) {
                    if (newToken < token1) return true;
                    else return false;
                }
                else {
                    if (newToken > token1) return true;
                    else return false;
                }
            } 
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }
    */
}

