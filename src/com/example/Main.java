package com.example;

import com.example.data.DataError;
import com.example.lock.LockError;
import com.example.transaction.TransactionError;
import com.example.transaction.TransactionManager;
import com.example.utils.ParserError;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        while (true) {
            TransactionManager manager = new TransactionManager();
            System.out.println("Please input file path (or 'exit' to quit):");
            String inputFilePath = scanner.nextLine();

            if (inputFilePath.equalsIgnoreCase("exit")) {
                break;
            }

            try {
                System.out.println("Getting inputs from " + inputFilePath);
                BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
                String line;
                while ((line = reader.readLine()) != null) {
                    manager.process(line);
                }
                reader.close();

                System.out.print("Process another file [y/n]? ");
                String isContinue = scanner.nextLine();
                if (isContinue.equalsIgnoreCase("n")) {
                    break;
                }
            } catch (IOException e) {
                System.out.println("Error, cannot open " + inputFilePath);
            } catch (DataError e) {
                throw new RuntimeException(e);
            } catch (ParserError e) {
                throw new RuntimeException(e);
            } catch (TransactionError e) {
                throw new RuntimeException(e);
            } catch (LockError e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Bye");
        scanner.close();
    }
}
