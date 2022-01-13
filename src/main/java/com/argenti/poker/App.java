package com.argenti.poker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.summingInt;

/**
 * https://en.wikipedia.org/wiki/List_of_poker_hands
 */
public class App {

    static Map<Character, Integer> CARD_VALUES = new HashMap<Character, Integer>() {{
        put('2', 2);
        put('3', 3);
        put('4', 4);
        put('5', 5);
        put('6', 6);
        put('7', 7);
        put('8', 8);
        put('9', 9);
        put('T', 10);
        put('J', 11);
        put('Q', 12);
        put('K', 13);
        put('A', 14);
    }};

    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String input;
            String[] player1Hand;
            String[] player2Hand;
            int player1Count = 0;
            int player2Count = 0;
            int player1Rank;
            int player2Rank;

            while ((input = br.readLine()) != null) {
                player1Hand = input.substring(0, 15).split("\\s+");
                player2Hand = input.substring(15).split("\\s+");
                player1Rank = rankHand(player1Hand);
                player2Rank = rankHand(player2Hand);

                // ranks are different
                if (player1Rank > player2Rank) {
                    player1Count++;
                } else if (player1Rank < player2Rank) {
                    player2Count++;
                }
                // ranks are same search for the highest card in each hand
                else {
                    if ("Player1".equals(matchInDepth(player1Hand, player2Hand, player1Rank))) {
                        player1Count++;
                    } else {
                        player2Count++;
                    }
                }
            }
            System.out.println("Player 1: " + player1Count);
            System.out.println("Player 2: " + player2Count);

        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    /**
     * get the highest cards in each hand; if the highest cards tie then the next highest cards are compared.
     */
    private static String matchInDepth(String[] hand1, String[] hand2, int rank) {

        // get numeric values for the chr value
        List<Integer> values1 = getCharList(hand1, 0).stream().map(c -> CARD_VALUES.get(c)).sorted().collect(Collectors.toList());
        List<Integer> values2 = getCharList(hand2, 0).stream().map(c -> CARD_VALUES.get(c)).sorted().collect(Collectors.toList());

        // High Card, Straight, Flush and Straight Flush
        if (rank == 1 || rank == 5 || rank == 6 || rank == 9)
            return findBigCard(values1, values2);

        // One Pair
        if (rank == 2) {
            Integer b1 = lookFor(hand1, 2);
            Integer b2 = lookFor(hand2, 2);

            if (b1 > b2)
                return "Player1";
            else if (b1 < b2)
                return "Player2";
                // remove the equivalent card and move forward
            else {
                values1.remove(b1);
                values1.remove(b1);
                values2.remove(b2);
                values2.remove(b2);
                return findBigCard(values1, values2);
            }
        }
        // Two Pairs
        if (rank == 3) {

            LinkedHashMap<Integer, Integer> m1 = twoPairs(hand1);
            LinkedHashMap<Integer, Integer> m2 = twoPairs(hand1);

            List<Integer> pairs1 = new ArrayList<>();
            Integer s1 = null;
            for (Map.Entry<Integer, Integer> entry : m1.entrySet()) {
                if (entry.getValue() == 2) {
                    pairs1.add(entry.getKey());
                } else {
                    s1 = entry.getKey();
                }
            }
            Collections.sort(pairs1);

            List<Integer> pairs2 = new ArrayList<>();
            Integer s2 = null;
            for (Map.Entry<Integer, Integer> entry : m2.entrySet()) {
                if (entry.getValue() == 2) {
                    pairs2.add(entry.getKey());
                } else {
                    s2 = entry.getKey();
                }
            }
            Collections.sort(pairs2);

            // Start matching pairs
            if (pairs1.get(1) > pairs2.get(1))
                return "Player1";
            else if (pairs1.get(1) < pairs2.get(1))
                return "Player2";
            else if (pairs1.get(0) > pairs2.get(0))
                return "Player1";
            else if (pairs1.get(0) < pairs2.get(0))
                return "Player2";
                // then to singles
            else if (s1 > s2)
                return "Player1";
            else
                return "Player2";
        } else {
            return null;
        }
    }

    private static String findBigCard(List<Integer> values1, List<Integer> values2) {
        values1.sort(Collections.reverseOrder());
        values2.sort(Collections.reverseOrder());
        Integer v1;
        Integer v2;
        for (int i = 0; i < values1.size(); i++) {
            v1 = values1.get(i);
            v2 = values2.get(i);
            if (!Objects.equals(v1, v2)) {
                if (v1 > v2) {
                    return "Player1";
                } else {
                    return "Player2";
                }
            }
        }
        return null;
    }

    /**
     * checking for a combination of five playing cards and output a suitable rank. The default combination is HighCard
     * whose rank is 1
     */
    private static int rankHand(String[] hands) {
        if (royalFlush(hands)) {
            return 10;
        }
        if (straightFlush(hands)) {
            return 9;
        }
        // four of a kind
        if (lookFor(hands, 4) != null) {
            return 8;
        }
        if (fullHouse(hands)) {
            return 7;
        }
        if (flush(hands)) {
            return 6;
        }
        if (straight(hands)) {
            return 5;
        }
        // three of a kind
        if (lookFor(hands, 3) != null) {
            return 4;
        }
        if (twoPairs(hands) != null) {
            return 3;
        }
        // one pair
        if (lookFor(hands, 2) != null) {
            return 2;
        }
        return 1;
    }

    /**
     * @param hands
     * @return Map of entry { hand value, count}
     */
    private static LinkedHashMap<Integer, Integer> twoPairs(String[] hands) {
        Map<Character, Integer> counts = getCharList(hands, 0).stream().collect(Collectors.groupingBy(e -> e, summingInt(e -> 1)));
        LinkedHashMap<Integer, Integer> sortedCounts = new LinkedHashMap<>();

        counts.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEachOrdered(x -> sortedCounts.put(CARD_VALUES.get(x.getKey()), x.getValue()));
        List<Integer> values = new ArrayList<>(sortedCounts.values());
        if (Arrays.asList(1, 2, 2).equals(values)) {
            return sortedCounts;
        }
        return null;
    }

    private static boolean straight(String[] hands) {
        List<Integer> v = getCharList(hands, 0).stream().map(c -> CARD_VALUES.get(c)).sorted().collect(Collectors.toList());
        int start = v.get(0);
        for (Integer i : v) {
            if (i.equals(start)) {
                start++;
            } else {
                return false;
            }
        }
        return true;
    }

    private static boolean flush(String[] hands) {
        return new HashSet<>(getCharList(hands, 1)).size() == 1;
    }

    private static boolean fullHouse(String[] hands) {
        return (lookFor(hands, 2) != null) && (lookFor(hands, 3) != null);
    }

    /**
     * 4 for 4 of a kind
     * 3 for 3 of a kind
     * 2 for pair
     */

    private static Integer lookFor(String[] hands, Integer look) {
        Map<Character, Integer> counts = getCharList(hands, 0).stream().collect(Collectors.groupingBy(e -> e, summingInt(e -> 1)));
        for (Map.Entry<Character, Integer> pair : counts.entrySet()) {
            if (Objects.equals(pair.getValue(), look)) {
                return CARD_VALUES.get(pair.getKey());
            }
        }
        return null;
    }

    private static boolean straightFlush(String[] hands) {
        return straight(hands) && flush(hands);
    }

    /**
     * check whether a Royal Flush
     */
    private static boolean royalFlush(String[] hands) {
        List<Integer> v = getCharList(hands, 0).stream().map(c -> CARD_VALUES.get(c)).sorted().collect(Collectors.toList());
        if (straight(hands) && flush(hands)) {
            return v.get(0).equals(10);
        } else {
            return false;
        }
    }

    private static List<Character> getCharList(String[] hands, int p) {
        List<Character> s = new ArrayList<>();
        for (String hand : hands) {
            s.add(hand.charAt(p));
        }
        return s;
    }
}
