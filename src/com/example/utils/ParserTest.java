package com.example.utils;

import java.util.List;

public class ParserTest {
    public static void main(String[] args) {
        Parser parser = new Parser();
        String[] inputs = {
                "begin(T1)",
                "begin(T2)",
                "fail(3)",
                "fail(4)",
                "R(T1,x1)",
                "W(T2,x8,88)",
                "end(T1)",
                "recover(4)",
                "recover(3)",
                "R(T2,x3)",
                "end(T2)",
                "fail(1)",
                "fail(2)",
                "fail(5)",
                "fail(6)",
                "fail(7)",
                "fail(8)",
                "fail(9)",
                "fail(10)",
                "begin(T3)",
                "R(T3,x8)"
        };

        for (String input : inputs) {
            try {
                List<String> parsedCommand = parser.parse(input);
                System.out.println("Parsed Command: " + parsedCommand);
            } catch (ParserError e) {
                System.out.println("Error parsing command: " + e.getMessage());
            }
        }
    }
}
