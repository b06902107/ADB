package com.example;

import com.example.transaction.TransactionManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        TransactionManager manager = new TransactionManager();
        Scanner scanner = new Scanner(System.in);

        while (true) {
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
                    // manager.process(line);
                }
                reader.close();

                System.out.print("Process another file [y/n]? ");
                String isContinue = scanner.nextLine();
                if (isContinue.equalsIgnoreCase("n")) {
                    break;
                }
            } catch (IOException e) {
                System.out.println("Error, cannot open " + inputFilePath);
            }
        }

        System.out.println("Bye");
        scanner.close();
    }
}
