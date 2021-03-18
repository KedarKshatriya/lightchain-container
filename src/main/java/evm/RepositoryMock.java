package evm;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.client.Repository;
import org.ethereum.vm.util.ByteArrayWrapper;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

    /**
	 * This class is used to make a repository of the different blocks formed 
     * when the EVM functions. It contains various functions like addBalance(), getBalance() 
     * which are used to add amount to the different addresses or get their current balance information respectively.
     * 
	 */
    public class RepositoryMock implements Repository {

        
        private Map<ByteArrayWrapper, Account> accounts = new HashMap<>(); // We use this hashmap to store the different addresses and relate them to their accounts which contain their respective data.
        private RepositoryMock parent;

        public RepositoryMock() {
            this(null);
        }

        public RepositoryMock(RepositoryMock parent) {
            this.parent = parent;
        }

        /**
        * This method is used to get the account details by passing an address. 
        *
        * @param address the account address
        */
        protected Account getAccount(byte[] address) {
            ByteArrayWrapper key = new ByteArrayWrapper(address);

            if (accounts.containsKey(key)) {
                return accounts.get(key);
            } else if (parent != null && parent.exists(address)) {
                Account account = parent.getAccount(address);
                Account accountTrack = new Account(account);
                accounts.put(key, accountTrack);
                return accountTrack;
            } 
            return null;
        }

        /**
        * Returns an account if exists.
        *
        * @param address the account address
        * @return an account if exists, NULL otherwise
        */
        @Override
        public boolean exists(byte[] address) {
            ByteArrayWrapper key = new ByteArrayWrapper(address);

            if (accounts.containsKey(key)) {
                return true;
            } else if (parent != null) {
                return parent.exists(address);
            } else {
                return false;
            }
        }

        @Override
        public void createAccount(byte[] address) {
            if (!exists(address)) {
                accounts.put(new ByteArrayWrapper(address), new Account());
            }
        }

        @Override
        public void delete(byte[] address) {
            accounts.remove(new ByteArrayWrapper(address));
        }

        @Override
        public long increaseNonce(byte[] address) {
            createAccount(address);
            return getAccount(address).nonce += 1;
        }

        @Override
        public long setNonce(byte[] address, long nonce) {
            createAccount(address);
            return (getAccount(address).nonce = nonce);
        }

        @Override
        public long getNonce(byte[] address) {
            Account account = getAccount(address);
            return account == null ? 0 : account.nonce;
        }

        @Override
        public void saveCode(byte[] address, byte[] code) {
            createAccount(address);
            getAccount(address).code = code;
        }

        @Override
        public byte[] getCode(byte[] address) {
            Account account = getAccount(address);
            return account == null ? null : account.code;
        }

        @Override
        public void putStorageRow(byte[] address, DataWord key, DataWord value) {
            createAccount(address);
            getAccount(address).storage.put(key, value);
        }

        @Override
        public DataWord getStorageRow(byte[] address, DataWord key) {
            Account account = getAccount(address);
            return account == null ? null : account.storage.get(key);
        }

        @Override
        public BigInteger getBalance(byte[] address) {
            Account account = getAccount(address);
            return account == null ? BigInteger.ZERO : account.balance;
        }

        @Override
        public BigInteger addBalance(byte[] address, BigInteger value) {
            createAccount(address);
            Account account = getAccount(address);
            return account.balance = account.balance.add(value);
        }

        @Override
        public RepositoryMock startTracking() {
            return new RepositoryMock(this);
        }

        @Override
        public Repository clone() {
            RepositoryMock copy = new RepositoryMock(parent);
            for (Map.Entry<ByteArrayWrapper, Account> entry : accounts.entrySet()) {
                copy.accounts.put(entry.getKey(), entry.getValue().clone());
            }
            return copy;
        }

        @Override
        public void commit() {
            if (parent != null) {
                parent.accounts.putAll(accounts);
            }
        }

        @Override
        public void rollback() {
            accounts.clear();
        }
    }
