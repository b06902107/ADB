package com.example.utils;
import com.example.data.DataManager;
import com.example.transaction.Transaction;

import java.util.*;


public class DeadlockDetector {
//    public Map<String, Set<String>> generateBlockingGraph(List<DataManager> sites) {
//        Map<String, Set<String>> blockingGraph = new HashMap<>();
//
//        for (DataManager site : sites) {
//            if (site.isUp()) {
//                Map<String, Set<String>> siteGraph = site.generateBlockingGraph();
//                for (Map.Entry<String, Set<String>> entry : siteGraph.entrySet()) {
//                    blockingGraph.computeIfAbsent(entry.getKey(), k -> new HashSet<>()).addAll(entry.getValue());
//                }
//            }
//        }
//
//        return blockingGraph;
//    }
//    private boolean hasCycle(String start, String end, Map<String, Boolean> visited, Map<String, Set<String>> blockingGraph) {
//        visited.put(start, true);
//        for (String adjacentTid : blockingGraph.getOrDefault(start, Collections.emptySet())) {
//            if (adjacentTid.equals(end)) {
//                return true;
//            }
//            if (!visited.getOrDefault(adjacentTid, false)) {
//                if (hasCycle(adjacentTid, end, visited, blockingGraph)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//    public String detect(Map<String, Transaction> transactions, Map<String, Set<String>> blockingGraph) {
//        long victimTimestamp = Long.MIN_VALUE;
//        String victimTid = null;
//
//        for (String tid : new ArrayList<>(blockingGraph.keySet())) {
//            Map<String, Boolean> visited = new HashMap<>();
//            if (hasCycle(tid, tid, visited, blockingGraph)) {
//                Transaction tx = transactions.get(tid);
//                if (tx.getTimestamp() > victimTimestamp) {
//                    victimTimestamp = tx.getTimestamp();
//                    victimTid = tid;
//                }
//            }
//        }
//        return victimTid;
//    }
}
