package solitaire;

import java.io.IOException;
import java.util.Scanner;
import java.util.Random;
import java.util.NoSuchElementException;

/**
 * This class implements a simplified version of Bruce Schneier's Solitaire Encryption algorithm.
 * 
 * @author RU NB CS112
 */
public class Solitaire {
	
	/**
	 * Circular linked list that is the deck of cards for encryption
	 */
	CardNode deckRear;
	
	/**
	 * Makes a shuffled deck of cards for encryption. The deck is stored in a circular
	 * linked list, whose last node is pointed to by the field deckRear
	 */
	public void makeDeck() {
		// start with an array of 1..28 for easy shuffling
		int[] cardValues = new int[28];
		// assign values from 1 to 28
		for (int i=0; i < cardValues.length; i++) {
			cardValues[i] = i+1;
		}
		
		// shuffle the cards
		Random randgen = new Random();
 	        for (int i = 0; i < cardValues.length; i++) {
	            int other = randgen.nextInt(28);
	            int temp = cardValues[i];
	            cardValues[i] = cardValues[other];
	            cardValues[other] = temp;
	        }
	     
	    // create a circular linked list from this deck and make deckRear point to its last node
	    CardNode cn = new CardNode();
	    cn.cardValue = cardValues[0];
	    cn.next = cn;
	    deckRear = cn;
	    for (int i=1; i < cardValues.length; i++) {
	    	cn = new CardNode();
	    	cn.cardValue = cardValues[i];
	    	cn.next = deckRear.next;
	    	deckRear.next = cn;
	    	deckRear = cn;
	    }
	}
	
	/**
	 * Makes a circular linked list deck out of values read from scanner.
	 */
	public void makeDeck(Scanner scanner) 
	throws IOException {
		CardNode cn = null;
		if (scanner.hasNextInt()) {
			cn = new CardNode();
		    cn.cardValue = scanner.nextInt();
		    cn.next = cn;
		    deckRear = cn;
		}
		while (scanner.hasNextInt()) {
			cn = new CardNode();
	    	cn.cardValue = scanner.nextInt();
	    	cn.next = deckRear.next;
	    	deckRear.next = cn;
	    	deckRear = cn;
		}
	}
	
	/**
	 * Implements Step 1 - Joker A - on the deck.
	 */
	void jokerA()
	{
	    if ((deckRear == null) || (deckRear.next == null))
		{
	    	throw new NoSuchElementException();
		}
		
		CardNode temp = deckRear.next;
		while ((temp.cardValue != 27) && (temp != deckRear))
		{
			temp = temp.next;
		}
		
		if (temp.cardValue == 27)
		{
			temp.cardValue = temp.next.cardValue;
			temp.next.cardValue = 27;
		}
		else throw new NoSuchElementException();
		
//		System.out.print("After Joker A: ");
//		printList(deckRear);
	}
	
	/**
	 * Implements Step 2 - Joker B - on the deck.
	 */
	void jokerB()
	{   
		CardNode temp = deckRear.next;
		while ((temp.cardValue != 28) && (temp != deckRear))
		{
			temp = temp.next;
		}
		
		if (temp.cardValue == 28)
		{
			temp.cardValue = temp.next.cardValue;
			temp.next.cardValue = temp.next.next.cardValue;
			temp.next.next.cardValue = 28;
		}
		else throw new NoSuchElementException();
		
//		System.out.print("After Joker B: ");
//		printList(deckRear);
	}
	
	/**
	 * Implements Step 3 - Triple Cut - on the deck.
	 */
	void tripleCut()
	{
		CardNode temp = deckRear;
		CardNode beforeA = new CardNode();
		CardNode a = new CardNode();
		CardNode afterA = new CardNode();
		CardNode beforeB = new CardNode();
		CardNode b = new CardNode();
		CardNode afterB = new CardNode();
		
		while ((temp.next.cardValue != 27) && (temp.next.cardValue != 28)) 
		{
			temp = temp.next;
		}
		beforeA = temp;
		a = beforeA.next;
		afterA = a.next;
		temp = afterA;
		
		while ((temp.next.cardValue!= 27) && (temp.next.cardValue != 28)) 
		{
			temp = temp.next;
		}
		beforeB = temp;
		b = beforeB.next;
		afterB = b.next;

		if ((deckRear.next == a) && (deckRear != b))
		{
			deckRear = b;
		}
		else if ((deckRear.next != a) && (deckRear == b))
		{
			deckRear = beforeA;
		}
		else if ((deckRear.next != a) && (deckRear != b))
		{
			b.next = deckRear.next;
			beforeA.next = afterB;
			deckRear.next = a;
			deckRear = beforeA;
		}
//		System.out.print("After TripleC: ");
//		printList(deckRear);
	}
	
	/**
	 * Implements Step 4 - Count Cut - on the deck.
	 */
	void countCut()
	{		
		CardNode temp = deckRear;
		CardNode front = temp.next;
		CardNode afterFront = new CardNode();
		int count = 0;
		int value = deckRear.cardValue;
		
		if (value == 28)
			value = 27;
		
		while (count < value)
		{
			temp = temp.next;
			count++;
		}
		afterFront = temp;
		
		while (temp.next != deckRear)
		{
			temp = temp.next;
			count++;
		}
		
		temp.next = front;
		deckRear.next = afterFront.next;
		afterFront.next = deckRear;
		
//		System.out.print("After Count C: ");
//		printList(deckRear);		
	}

	
	/**
	 * Gets a key. Calls the four steps - Joker A, Joker B, Triple Cut, Count Cut, then
	 * counts down based on the value of the first card and extracts the next card value 
	 * as key. But if that value is 27 or 28, repeats the whole process (Joker A through Count Cut)
	 * on the latest (current) deck, until a value less than or equal to 26 is found, which is then returned.
	 * 
	 * @return Key between 1 and 26
	 */
	int getKey()
	{
		int key = 28;
		while ((key == 27) || (key == 28))
		{
			jokerA();
			jokerB();
			tripleCut();
			countCut();
			
			int value = deckRear.next.cardValue;
			int count = 0;
			CardNode temp = deckRear;
			
			if (value == 28)
				value = 27;
		    
		    while (count < value)
			{
				temp = temp.next;
				count++;
			}
		    
		    key = temp.next.cardValue;
		}
	    return key;
	}
	
	/**
	 * Utility method that prints a circular linked list, given its rear pointer
	 * 
	 * @param rear Rear pointer
	 */
	private static void printList(CardNode rear) {
		if (rear == null) { 
			return;
		}
		System.out.print(rear.next.cardValue);
		CardNode ptr = rear.next;
		do {
			ptr = ptr.next;
			System.out.print(" " + ptr.cardValue);
		} while (ptr != rear);
		System.out.print("\n");
	}

	/**
	 * Encrypts a message, ignores all characters except upper case letters
	 * 
	 * @param message Message to be encrypted
	 * @return Encrypted message, a sequence of upper case letters only
	 */
	public String encrypt(String message)
	{
//		System.out.print("Initial List:  ");
//		printList(deckRear);
		message = message.replaceAll("[^a-zA-Z]", "");
		message = message.toUpperCase();
//		System.out.println(message);
		char c;
		int count = 0;
		int temp = 0;
		while (count < message.length())
		{
			c = message.charAt(count);
			temp = (c - 'A' + 1) + getKey();
			if (temp > 26)
				temp = temp - 26;
			c = (char)(temp - 1 + 'A');
			message = message.substring(0, count) + c + message.substring(count+1);
			count++;
		}
		return message;
	}
	
	/**
	 * Decrypts a message, which consists of upper case letters only
	 * 
	 * @param message Message to be decrypted
	 * @return Decrypted message, a sequence of upper case letters only
	 */
	public String decrypt(String message)
	{
		char c;
		int count = 0;
		int temp = 0;
		while (count < message.length())
		{
			c = message.charAt(count);
			temp = (c - 'A' + 1) - getKey();		
			if (temp < 0)
				temp = temp + 26;
			c = (char)(temp - 1 + 'A');
			message = message.substring(0, count) + c + message.substring(count+1);
			count++;
		}
		return message;
	}
}