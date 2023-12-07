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
            result.add(matcher.group());
        }

        if (!commands.contains(result.get(0))) {
            throw new ParserError("Unknown command " + result.get(0));
        }

        return result;
    }
}

