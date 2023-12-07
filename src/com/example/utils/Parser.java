package com.example.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private Set<String> commands;

    public Parser() {
        commands = new HashSet<>(Arrays.asList("begin", "end", "W", "R", "dump", "beginRO", "fail", "recover"));
    }

    public List<String> parse(String line) throws ParserError {
        if (line == null || line.trim().isEmpty()) {
            return null;  // Ignore empty lines
        }

        Pattern pattern = Pattern.compile("\\w+|\\(\\w+,\\w+,\\d+\\)|\\(\\w+,\\w+\\)");
        Matcher matcher = pattern.matcher(line);
        List<String> result = new ArrayList<>();

        while (matcher.find()) {
            String temp = matcher.group();
            if(temp.charAt(0) == '(' && temp.charAt(temp.length() - 1) == ')'){
                String[] query = temp.substring(1, temp.length() - 1).split(",");
                for (String a : query) {
                    result.add(a);
                }
            }
            else {
                result.add(temp);
            }
        }

        if (!commands.contains(result.get(0))) {
            throw new ParserError("Unknown command " + result.get(0));
        }

        return result;
    }
}

