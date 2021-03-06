package evm;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.bouncycastle.util.encoders.Hex;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.client.*;
import org.ethereum.vm.util.ByteArrayUtil;
import org.ethereum.vm.util.HashUtil;
import org.ethereum.vm.util.HexUtil;
import static org.ethereum.vm.util.ByteArrayUtil.merge;
import org.apache.log4j.Logger;

/**
* This class is used by the lightchain to interact with the Ethereum Virtuam Machine (EVM) 
* It extends vmbase class for importing all the core functionality related to the EVM. 
*/
public class ContractTransaction extends vmbase {

    public final BigInteger premine = BigInteger.valueOf(1L).multiply(Unit.ETH); // each account has 1 ether
    public Transaction transaction;
    public Block block;
    final static Logger logger = Logger.getLogger(ContractTransaction.class);

    public void setup() {
        super.setup();
        transaction = new TransactionMock(false, caller, address, 0, value, data, gas, gasPrice);
        block = new BlockMock(number, prevHash, coinbase, timestamp, gasLimit);
        repository.addBalance(origin, premine);
        repository.addBalance(caller, premine);
        repository.addBalance(address, premine);
        repository.addBalance(coinbase, premine);
    }

     /**
	 * This function reads a contract by taking the name of contract
     *
	 * @param contractLocation is the location of the stored contract
     * @param address represents the account address
     * @param nonce is a random, one-time, whole number
     * @param gas refers to the cost necessary to perform a transaction
	 */
    public byte[] createContract(String contractLocation, byte[] address, long nonce, long gas) throws IOException {
        return createContract(readContract(contractLocation), new byte[0], address, nonce, gas);
    }

    
    /**
	 * This function reads a contract by taking the name of contract
     *
	 * @param fileName is the contract name 
	 */
    public byte[] readContract(String fileName) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
        return HexUtil.fromHexString(lines.get(0));
    }

    /**
	 * This function creates a contract and also returns the address of created contract
	 * 
	 * @param code is the compiled solidity contract code in byte format 
     * @param args is the location of the stored contract
     * @param address represents the account address
     * @param nonce is a random, one-time, whole number
     * @param gas refers to the cost necessary to perform a transaction
	 */
    public byte[] createContract(byte[] code, byte[] args, byte[] address, long nonce, long gas)  {
        byte[] data = merge(code, args);
        byte[] contractAddress = HashUtil.calcNewAddress(address, nonce);
        Transaction transaction = new TransactionMock(true, address, contractAddress, nonce, value, data, gas, gasPrice);
        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore);
        TransactionReceipt receipt = executor.run();
        return contractAddress;
    }

    /**
	 * Wrapper function for the smart contract function code.
	 * This function is used to interact EVM with Lightchain.
     *
	 * @param token is the asset value
     * @param contractloc location / name of the contract
     * @param functname is the name of the function in contract which we want to interact with
	 */
    public boolean TransctSol(int token, String contractloc,String functname) throws IOException {
            long nonce = 0;
            long nonce1 = 1;
            BigInteger toSend = new BigInteger(String.valueOf(token));
            byte[] contractAddress1 = createContract(contractloc, origin, nonce, gas);

            repository.addBalance(contractAddress1, premine);
            byte[] method = HashUtil.keccak256(functname.getBytes(StandardCharsets.UTF_8));
            byte[] data = ByteArrayUtil.merge(Arrays.copyOf(method, 4), DataWord.of(toSend).getData());

            Transaction transaction = new TransactionMock(false, caller, contractAddress1, nonce1, value, data, gas, gasPrice);
            TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore);
            TransactionReceipt receipt = executor.run();
            int res = Integer.parseInt(Hex.toHexString(receipt.getReturnData()));
            logger.debug("output from contract: "+res);
        return res == 1; // returns the value true or false
    }
     
    
    /**
	 * Wrapper function for the basicTransfer smart contract function code.
	 * This function is used to interact EVM with Lightchain.
     *
	 * @param token is the asset value
     * @param contractloc location / name of the contract
     * @param functname is the name of the function in contract which we want to interact with
     * @param Nmode the mode to identify the node
     * @param Psell amount the producer will sell
	 */
    public int basicTransfer(int token, String contractloc,String functname,int nodeMode, int amtSell) throws IOException {
    
            long nonce = 0;
            long nonce1 = 1;
            BigInteger _Tkn = new BigInteger(String.valueOf(token));
            BigInteger _Nmode = new BigInteger(String.valueOf(nodeMode));
            BigInteger _Psell = new BigInteger(String.valueOf(amtSell));
            byte[] contractAddress2 = createContract(contractloc, origin, nonce, gas);
            repository.addBalance(contractAddress2, premine);

            byte[] method = HashUtil.keccak256(functname.getBytes(StandardCharsets.UTF_8));
            byte[] data = ByteArrayUtil.merge(Arrays.copyOf(method, 4),DataWord.of(_Nmode).getData() ,DataWord.of(_Tkn).getData(), DataWord.of(_Psell).getData());

            Transaction transaction = new TransactionMock(false, caller, contractAddress2, nonce1, value, data, gas, gasPrice);
            TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore);
            TransactionReceipt receipt = executor.run();
            logger.debug("\n reciept string: "+receipt.toString());
            String res = Hex.toHexString(receipt.getReturnData());
            int opt = Integer.parseInt(res, 16);
            logger.debug("output from contract: "+opt); 
        return opt;
    }
}

