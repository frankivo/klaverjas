package com.keemerz.klaverjas.comparator;

import com.keemerz.klaverjas.domain.Card;
import com.keemerz.klaverjas.domain.Suit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.keemerz.klaverjas.domain.Suit.*;

public class HighestCardInTrickComparator implements Comparator<Card> {
    private static final List<Suit> SUIT_NATURAL_ORDER = Arrays.asList(
            CLUBS, DIAMONDS, SPADES, HEARTS);

    private Suit startColor;
    private Suit trump;


    public HighestCardInTrickComparator(Suit trump, Suit startColor) {
        this.trump = trump;
        this.startColor = startColor;
    }

    @Override
    public int compare(Card c1, Card c2) {
        List<Suit> reOrderedSuits = new ArrayList<>(SUIT_NATURAL_ORDER);
        reOrderedSuits.remove(startColor);
        reOrderedSuits.remove(trump);
        reOrderedSuits.add(startColor); // makes sure startColor is in next-to-last position
        reOrderedSuits.add(trump); // makes sure trump is in last position

        if (c1.getSuit() != c2.getSuit()) {
            return Integer.compare(reOrderedSuits.indexOf(c1.getSuit()), reOrderedSuits.indexOf(c2.getSuit()));
        }
        if (c1.getSuit() == trump) {
            return new TrumpOrderComparator().compare(c1, c2);
        }
        return new NonTrumpOrderComparator().compare(c1, c2);

    }
}
