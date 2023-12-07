package com.example.transaction;
import com.example.data.DataManager;
import com.example.utils.Parser;
import com.example.utils.ParserError;

import java.lang.reflect.WildcardType;
import java.util.*;

public class TransactionManager {
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

        this.sites = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            this.sites.add(new DataManager(i));
        }
    }

    public void process(String s) throws ParserError {
        List<String> arguments = parser.parse(s);
        if (arguments == null || arguments.isEmpty()) {
            return;
        }

//        if (detectDeadlock()) {
//            executeOperations();
//            System.out.println();
//        }

        System.out.println("------- Time " + timestamp + " -------");
        // processCommand(arguments);
        // executeOperations();
        timestamp++;
        System.out.println();
    }

    public void processCommand(List<String> arguments) {
        String cmd = arguments.get(0);

        switch (cmd) {
            case "begin":
                assert arguments.size() == 2;
                begin(arguments);
                break;

            case "beginRO":
                assert arguments.size() == 2;
                beginRO(arguments);
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
                addWriteOperation(tid, vid, value);
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

    public void executeOperations() {
        Iterator<Operation> iterator = operations.iterator();

        while (iterator.hasNext()) {
            Operation operation = iterator.next();
            String tid = operation.getTid();
            String vid = operation.getVid();

            boolean isSuccess;
            if (operation.getOperationType() == OperationType.R) {
                Transaction transaction = transactions.get(tid);
                isSuccess = transaction.isReadOnly() ? snapshotRead(tid, vid) : read(tid, vid);
            } else { // OperationType.WRITE
                WriteOperation writeOperation = (WriteOperation) operation;
                isSuccess = write(tid, vid, writeOperation.getValue());
            }

            if (isSuccess) {
                iterator.remove(); // Safely remove the operation from the queue
            }
        }
    }

}
