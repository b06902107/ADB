package com.example.transaction;
import com.example.data.DataError;
import com.example.data.DataManager;
import com.example.data.ResultValue;
import com.example.lock.LockError;

import com.example.utils.DeadlockDetector;
import com.example.utils.Parser;
import com.example.utils.ParserError;

import javax.xml.crypto.Data;
import java.util.*;

public class TransactionManager {
    private DeadlockDetector deadlockDetector;
    private Parser parser;
    private Map<String, Transaction> transactions;
    private int timestamp;
    private Deque<Operation> operations;
    private List<DataManager> sites;

    public TransactionManager() {
        this.parser = new Parser();
        this.transactions = new HashMap<>();
        this.timestamp = 0;
        this.operations = new ArrayDeque<>();
        this.deadlockDetector = new DeadlockDetector();

        this.sites = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            this.sites.add(new DataManager(i));
        }
    }

    public void process(String s) throws ParserError, LockError, DataError, TransactionError {
        List<String> arguments = parser.parse(s);
        if (arguments == null || arguments.isEmpty()) {
            return;
        }

//        if (detectDeadlock()) {
//            executeOperations();
//            System.out.println();
//        }
        System.out.println("------- Time " + timestamp + " -------");
        processCommand(arguments);
        executeOperations();
        timestamp++;
        System.out.println();
    }

    public void processCommand(List<String> arguments) throws TransactionError, LockError, DataError {
        String cmd = arguments.get(0);

        switch (cmd) {
            case "begin":
                assert arguments.size() == 2;
                begin(arguments);
                break;

            case "end":
                assert arguments.size() == 2;
                end(arguments);
                break;

            case "W":
                assert arguments.size() == 4;
                String tid = arguments.get(1);
                String vid = arguments.get(2);
                String value = arguments.get(3);
                addWriteOperation(tid, vid, Integer.parseInt(value));
                break;

            case "R":
                assert arguments.size() == 3;
                tid = arguments.get(1);
                vid = arguments.get(2);
                addReadOperation(tid, vid);
                break;

            case "dump":
                assert arguments.size() == 1;
                dump();
                break;

            case "fail":
                assert arguments.size() == 2;
                fail(arguments);
                break;

            case "recover":
                assert arguments.size() == 2;
                recover(arguments);
                break;

            default:
                throw new IllegalArgumentException("Unknown command: " + cmd);
        }
    }

    public void executeOperations() throws TransactionError, DataError, LockError {
        Iterator<Operation> iterator = operations.iterator();

        while (iterator.hasNext()) {
            Operation operation = iterator.next();
            String tid = operation.getTid();
            String vid = operation.getVid();

            boolean isSuccess;
            if (operation.getOperationType() == OperationType.R) {
                Transaction transaction = transactions.get(tid);
                isSuccess = read(tid, vid);
            } else { // OperationType.WRITE
                WriteOperation writeOperation = (WriteOperation) operation;
                isSuccess = write(tid, vid, writeOperation.getValue());
            }

            if (isSuccess) {
                iterator.remove(); // Safely remove the operation from the queue
            }
        }
    }

    public void begin(List<String> arguments) throws TransactionError {
        String tid = arguments.get(1);
        if (transactions.containsKey(tid)) {
            throw new TransactionError("Transaction " + tid + " has already begun.");
        }

        Transaction transaction = new Transaction(tid, timestamp, false);
        transactions.put(tid, transaction);
        System.out.println("Transaction " + tid + " begins");
    }

    public void end(List<String> arguments) throws LockError, DataError {
        String tid = arguments.get(1);
        Transaction transaction = transactions.get(tid);

        if (transaction == null) {
            throw new IllegalStateException("Transaction " + tid + " doesn't exist.");
        }

        if (transaction.isAborted()) {
            abort(tid, true, timestamp);
        } else {
            commit(tid, timestamp);
        }
    }

    public void abort(String tid, boolean siteFail, int commitTime) throws LockError, DataError {
        // Abort the transaction on each site
        for (DataManager site : sites) {
            site.abort(tid);
        }

        // Remove the transaction from the transactions map
        transactions.remove(tid);

        // Determine the abort reason
        // String abortReason = siteFail ? "Site Failed" : "Deadlock";
        System.out.println(tid + " aborts at time " + commitTime + ".");

        // Remove all operations related to the aborted transaction from the queue
        operations.removeIf(operation -> operation.getTid().equals(tid));
    }

    public void commit(String tid, int commitTime) throws LockError, DataError {
        // Commit the transaction on each site
        for (DataManager site : sites) {
            if ( !site.checkCommit(tid) ){
                abort(tid, false, timestamp);
                return;
            }
        }
        for (DataManager site : sites) {
            site.commit(tid, commitTime);
        }
        // Remove the transaction from the transactions map
        transactions.remove(tid);

        // Print commit information
        System.out.println(tid + " commits at time " + commitTime + ".");
    }

//    public boolean snapshotRead(String tid, String vid) throws TransactionError {
//        Transaction transaction = transactions.get(tid);
//        if (transaction == null) {
//            throw new TransactionError("Transaction " + tid + " doesn't exist.");
//        }
//
//        int timestamp = transaction.getTimestamp();
//        for (DataManager site : sites) {
//            if (site.isUp() && site.hasVariable(vid)) {
//                ResultValue resultValue = site.snapshotRead(vid, timestamp);
//                if (resultValue.isSuccess()) {
//                    System.out.println("Read-only transaction " + tid + " reads " + vid + "." + site.getSid() + ": " + resultValue.getValue());
//                    return true;
//                }
//            }
//        }
//
//        System.out.println("Read-only transaction " + tid + " failed to read " + vid + ": no suitable site.");
//        return false;
//    }

    public void addReadOperation(String tid, String vid) throws TransactionError {
        Transaction transaction = transactions.get(tid);
        if (transaction == null) {
            throw new TransactionError("Transaction " + tid + " doesn't exist, can't add read operation.");
        }

        operations.add(new ReadOperation(tid, vid));
    }

    public void addWriteOperation(String tid, String vid, int value) throws TransactionError {
        Transaction transaction = transactions.get(tid);
        if (transaction == null) {
            throw new TransactionError("Transaction " + tid + " doesn't exist, can't add write operation.");
        }

        operations.add(new WriteOperation(tid, vid, value));
    }

    public boolean read(String tid, String vid) throws TransactionError, DataError, LockError {
        Transaction transaction = transactions.get(tid);
        if (transaction == null) {
            throw new TransactionError("Transaction " + tid + " hasn't begun, read operation fails.");
        }

        for (DataManager site : sites) {
            if (site.isUp() && site.hasVariable(vid)) {
                ResultValue resultValue = site.read(tid, vid, transaction.getTimestamp());
                if (resultValue.isSuccess()) {
                    transaction.getVisitedSites().add(site.getSid());
                    System.out.println("Transaction " + tid + " reads " + vid + "." + site.getSid() + ": " + resultValue.getValue());
                    return true;
                }
            }
        }

        return false;
    }
    public boolean write(String tid, String vid, int value) throws TransactionError, LockError {
        Transaction transaction = transactions.get(tid);
        if (transaction == null) {
            throw new TransactionError("Transaction " + tid + " doesn't exist, write operation fails.");
        }

        List<Integer> targetSites = new ArrayList<>();
        for (DataManager site : sites) {
            if (site.hasVariable(vid) && site.isUp()) {
//                boolean writeLock = site.getWriteLock(tid, vid);
//                if (!writeLock) {
//                    System.out.println(tid + " waits due to write lock conflict. Current lock: " + site.getLockTable().get(vid).getCurrentLock());
//                    return false;
//                }
                targetSites.add(site.getSid());
            }
        }

        if (targetSites.isEmpty()) {
            System.out.println("No suitable sites found for writing.");
            return false;
        }

        for (int sid : targetSites) {
            DataManager targetSite = sites.get(sid - 1);  // Assuming site IDs start from 1
            targetSite.write(tid, vid, value);
            transaction.getVisitedSites().add(sid);
        }

        System.out.println("Transaction " + tid + " writes variable " + vid + " with value " + value + " to sites " + targetSites);
        return true;
    }

    public void dump(){
        System.out.println("Dump all sites:");
        for (DataManager site : sites) {
            site.dump();  // Assuming DataManager class has a dump method
        }
    }

    public void fail(List<String> arguments) throws TransactionError {
        int sid = Integer.parseInt(arguments.get(1));
        DataManager site = sites.get(sid - 1);  // Adjust for zero-based indexing

        if (!site.isUp()) {
            throw new TransactionError("Site " + sid + " is already down.");
        }

        site.fail(sid);
        System.out.println("Site " + sid + " fails.");

        for (Transaction trans : transactions.values()) {
            if (trans.isReadOnly() || trans.isAborted() || !trans.getVisitedSites().contains(sid)) {
                continue;
            }
            trans.setAborted(true);
        }
    }
    public void recover(List<String> arguments) {
        int sid = Integer.parseInt(arguments.get(1));
        DataManager site = this.sites.get(sid - 1);

        if (site.isUp()) {
            System.out.println("Site " + sid + " is up, no need to recover.");
            return;
        }

        site.recover(this.timestamp);
        System.out.println("Site " + sid + " recovers.");
    }

//    public boolean detectDeadlock() throws LockError, DataError {
//        // Generate the blocking graph
//        Map<String, Set<String>> blockingGraph = deadlockDetector.generateBlockingGraph(sites);
//
//        // Detect deadlocks
//        String victimTid = deadlockDetector.detect(transactions, blockingGraph);
//
//        if (victimTid != null) {
//            System.out.println("Found deadlock, aborts the youngest transaction " + victimTid);
//            abort(victimTid, false);  // false indicating it's due to deadlock, not site failure
//            return true;
//        }
//        return false;
//    }

}
