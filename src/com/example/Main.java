package com.example;

import com.example.data.DataError;
import com.example.lock.LockError;
import com.example.transaction.TransactionError;
import com.example.transaction.TransactionManager;
import com.example.utils.ParserError;
import java.io.*;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Using jar file: java -jar ADB.jar <input_file>");
            System.err.println("Using IDE: specify argument <input_file>");
            System.exit(1);
        }

        String inputFilePath = args[0];
        TransactionManager manager = new TransactionManager();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                manager.process(line);
            }
        } catch (IOException e) {
            System.err.println("Error, cannot open " + inputFilePath);
            e.printStackTrace();
            System.exit(1);
        } catch (DataError | ParserError | TransactionError | LockError e) {
            e.printStackTrace();
        }

        System.out.println("Processing completed.");
    }
}
