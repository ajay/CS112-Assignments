package search;

import java.io.*;
import java.util.*;

/**
 * This class encapsulates an occurrence of a keyword in a document. It stores the
 * document name, and the frequency of occurrence in that document. Occurrences are
 * associated with keywords in an index hash table.
 * 
 * @author Sesh Venugopal
 * 
 */
class Occurrence {
	/**
	 * Document in which a keyword occurs.
	 */
	String document;
	
	/**
	 * The frequency (number of times) the keyword occurs in the above document.
	 */
	int frequency;
	
	/**
	 * Initializes this occurrence with the given document,frequency pair.
	 * 
	 * @param doc Document name
	 * @param freq Frequency
	 */
	public Occurrence(String doc, int freq) {
		document = doc;
		frequency = freq;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + document + "," + frequency + ")";
	}
}

/**
 * This class builds an index of keywords. Each keyword maps to a set of documents in
 * which it occurs, with frequency of occurrence in each document. Once the index is built,
 * the documents can searched on for keywords.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in descending
	 * order of occurrence frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash table of all noise words - mapping is from word to itself.
	 */
	HashMap<String,String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashMap<String,String>(100,2.0f);
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.put(word,word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeyWords(docFile);
			mergeKeyWords(kws);
		}
	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeyWords(String docFile) 
	throws FileNotFoundException
	{
		if (docFile == null)
			throw new FileNotFoundException();

		HashMap<String, Occurrence> map = new HashMap<String, Occurrence>();

		Scanner sc = new Scanner(new File(docFile));

		while (sc.hasNext())
		{
			String key = getKeyWord(sc.next());
			if (key != null)
			{
				if (map.containsKey(key))
				{
					Occurrence oc = map.get(key);
					oc.frequency++;
				}
				else
				{
					Occurrence oc = new Occurrence(docFile, 1);
					map.put(key, oc);
				}
			}
		}
		return map;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeyWords(HashMap<String,Occurrence> kws)
	{
		for (String key : kws.keySet())
		{
			ArrayList<Occurrence> oc = new ArrayList<Occurrence>();

			if (keywordsIndex.containsKey(key))
				oc = keywordsIndex.get(key);
			
			oc.add(kws.get(key));
			insertLastOccurrence(oc);
			keywordsIndex.put(key, oc);
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * TRAILING punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyWord(String word)
	{
		if ((word == null) || (word.equals(null)))
			return null;
		
		word = word.toLowerCase();

		if (containsACharacter(word))
			return null;

		word = removeTrailingPunc(word);

		if (noiseWords.containsKey(word))
			return null;
		
		if (word.length() <= 0)
			return null;
		
		return word;
	}

	private boolean containsACharacter(String word)
	{
		int count = 0;
		boolean first = false;
		while (count < word.length())
		{
			char c = word.charAt(count);
			
			if (!(Character.isLetter(c)))
				first = true;

			if ((first) && (Character.isLetter(c)))
				return true;

			count++;
		}
		return false;
	}

	private String removeTrailingPunc(String word)
	{
		int count = 0;
		while (count < word.length())
		{
			char c = word.charAt(count);
			if (!(Character.isLetter(c)))
				break;
			count++;
		}
		return word.substring(0, count);
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * same list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion of the last element
	 * (the one at index n-1) is done by first finding the correct spot using binary search, 
	 * then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs)
	{
		if (occs.size() < 2)
			return null;

		int low = 0;
		int high = occs.size()-2;
		int target = occs.get(occs.size()-1).frequency;
		int mid = 0;
		ArrayList<Integer> midpoints = new ArrayList<Integer>();

		while (high >= low)
		{
			mid = ((low + high) / 2);
			int data = occs.get(mid).frequency;
			midpoints.add(mid);

			if (data == target)
				break;

			else if (data < target)
			{
				high = mid - 1;
			}

			else if (data > target)
			{
				low = mid + 1;
				if (high <= mid)
					mid = mid + 1;
			}
		}
		
		midpoints.add(mid);

		Occurrence temp = occs.remove(occs.size()-1);
		occs.add(midpoints.get(midpoints.size()-1), temp);

		return midpoints;
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of occurrence frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will appear before doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matching documents, the result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of NAMES of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matching documents,
	 *         the result is null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2)
	{
		ArrayList<String> results = new ArrayList<String>();
		ArrayList<Occurrence> ocArr1 = new ArrayList<Occurrence>();
		ArrayList<Occurrence> ocArr2 = new ArrayList<Occurrence>();
		ArrayList<Occurrence> combined = new ArrayList<Occurrence>();
		
		if (keywordsIndex.containsKey(kw1))
			ocArr1 = keywordsIndex.get(kw1);
		
		if (keywordsIndex.containsKey(kw2))
			ocArr2 = keywordsIndex.get(kw2);
		
		combined.addAll(ocArr1);
		combined.addAll(ocArr2);
		
		if (!(ocArr1.isEmpty()) && !(ocArr2.isEmpty()))
		{
			// Sort with preference for ocArr1
			for (int x = 0; x < combined.size()-1; x++)
			{
				for (int y = 1; y < combined.size()-x; y++)
				{
					if (combined.get(y-1).frequency < combined.get(y).frequency)
					{
						Occurrence temp = combined.get(y-1);
						combined.set(y-1, combined.get(y));
						combined.set(y,  temp);
					}
				}
			}

			// Remove duplicates
			for (int x = 0; x < combined.size()-1; x++)
			{
				for (int y = x + 1; y < combined.size(); y++)
				{
					if (combined.get(x).document == combined.get(y).document)
						combined.remove(y);
				}
			}
		}

		// Top 5
		while (combined.size() > 5)
			combined.remove(combined.size()-1);
		
		System.out.println(combined);
		
		for (Occurrence oc : combined)
			results.add(oc.document);

		return results;
	}
}