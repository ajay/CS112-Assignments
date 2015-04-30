package structures;

import java.util.*;

/**
 * This class implements an HTML DOM Tree. Each node of the tree is a TagNode, with fields for
 * tag/text, first child and sibling.
 * 
 */
public class Tree {
	
	/**
	 * Root node
	 */
	TagNode root=null;
	
	/**
	 * Scanner used to read input HTML file when building the tree
	 */
	Scanner sc;
	
	/**
	 * Initializes this tree object with scanner for input HTML file
	 * 
	 * @param sc Scanner for input HTML file
	 */
	public Tree(Scanner sc) {
		this.sc = sc;
		root = null;
	}
	
	///////////////////////////////////////////////////////////////////////////////
	///// HELPER METHODS \\\\\
	private String noBrackets(String brackets)
	{
		return brackets.substring(1, brackets.length()-1);
	}
	
	private boolean hasTag(String tag, TagNode localRoot) 
	{
		if (localRoot == null)
			return false;

		else if (localRoot.tag.equals(tag))
			return true;

		boolean hasTagInTree = (hasTag(tag, localRoot.firstChild) || hasTag(tag, localRoot.sibling));

		return hasTagInTree;
	}

	private boolean checkPunc(char c)
	{
		return ((c == '!') || (c == '?') || (c == '.') || (c == ',') || (c == ';')|| (c == ':'));
	}
	
	///// REPLACE \\\\\
	private void replace(String oldTag, String newTag, TagNode localRoot)
	{
		if (localRoot == null)
			return;
		
		else if (localRoot.tag.equals(oldTag))
			localRoot.tag = newTag;
			
		replace(oldTag, newTag, localRoot.firstChild);
		replace(oldTag, newTag, localRoot.sibling);
	}

	///// BOLD \\\\\
	private void bold(int row, TagNode localRoot)
	{
		if (localRoot == null)
			return;

		if (localRoot.tag.equals("table"))
		{
			TagNode tableRow = localRoot.firstChild;
			int counter = 0;
			while (counter < row-1)
			{
				if (tableRow.sibling != null)
					tableRow = tableRow.sibling;
				else throw new IllegalArgumentException();
				counter++;
			}
			TagNode col = tableRow.firstChild;
			while (col != null)
			{
				TagNode b = new TagNode("b", col.firstChild, null);
				col.firstChild = b;
				col = col.sibling;
			}
		}
		bold(row, localRoot.firstChild);
		bold(row, localRoot.sibling);
	}

	///// REMOVE \\\\\
	private void remove(String tag, TagNode parent, TagNode localRoot) 
	{
		if ((localRoot == null) || (parent == null))
			return;
		
		else if (localRoot.tag.equals(tag))
		{
			if (tag.equals("ol") || tag.equals("ul"))
			{
				TagNode childNode = localRoot.firstChild;
				while (childNode != null)
				{
					if (childNode.tag.equals("li"))
						childNode.tag = "p";
					
					childNode = childNode.sibling;
				}
			}

			if (parent.firstChild == localRoot) 
			{
				parent.firstChild = localRoot.firstChild;
				TagNode childNode = localRoot.firstChild;
				
				while (childNode.sibling != null)
					childNode = childNode.sibling;
				
				childNode.sibling = localRoot.sibling;
			}

			else if (parent.sibling == localRoot) 
			{
				TagNode childNode = localRoot.firstChild;
				
				while (childNode.sibling != null)
					childNode = childNode.sibling;
				
				childNode.sibling = localRoot.sibling;
				parent.sibling = localRoot.firstChild;
			}

			return;
		}

		parent = localRoot;
		remove(tag, parent, localRoot.firstChild);
		remove(tag, parent, localRoot.sibling);
	}
	
	///// ADD \\\\\
	private void add(String word, String tag, TagNode localRoot) 
	{
		if (localRoot == null)
			return;
		
		String lowerTag = localRoot.tag.toLowerCase();
		String lowerWord = word.toLowerCase();
		
		if (lowerTag.contains(lowerWord))
		{
			if (lowerTag.equals(word))
			{
				String oldWord = localRoot.tag;
				localRoot.tag = tag;
				localRoot.firstChild = new TagNode(oldWord, localRoot.firstChild, null);
			}
			
			else
			{
				TagNode sibling = localRoot.sibling;
				int index = lowerTag.indexOf(lowerWord);
				
				String[] split = {localRoot.tag.substring(0, index), localRoot.tag.substring(index, index + word.length()), localRoot.tag.substring(index + word.length(), lowerTag.length()), ""};
				
				if ((split[2].length() > 1) && (checkPunc(split[2].charAt(0))) && (!checkPunc(split[2].charAt(1))))
				{
					split[3] = "" + split[2].charAt(0);
					split[2] = split[2].substring(1);
				}

				if ((split[2].length() == 0) || (split[2].length() >= 1 && (split[2].charAt(0) == ' ' || checkPunc(split[2].charAt(0)))))
				{
					if ((split[2].length() == 1) && (checkPunc(split[2].charAt(0))))
					{
						split[1] = split[1] + split[2];
						split[2] = "";
					}
					
					localRoot.tag = split[0];
					TagNode newChild = new TagNode(split[1] + split[3], null, null);
					localRoot.sibling = new TagNode(tag, newChild, null);

					if (split[2].length() > 0) 
					{
						if (sibling != null)
							localRoot.sibling.sibling = new TagNode(split[2], null, sibling);
						else
							localRoot.sibling.sibling = new TagNode(split[2], null, null);
					}
					
					else if (sibling != null)
					{
						localRoot.sibling.sibling = sibling;
					}
					
				}
			}
			
			if (localRoot.sibling != null)
				add(word, tag, localRoot.sibling.sibling);
		}
		
		else
		{
			add(word, tag, localRoot.firstChild);
			add(word, tag, localRoot.sibling);
		}
	}
	///////////////////////////////////////////////////////////////////////////////

	/**
	 * Builds the DOM tree from input HTML file. The root of the 
	 * tree is stored in the root field.
	 */
	public void build()
	{
		if (!sc.hasNextLine())
			return;
		
		root = new TagNode(noBrackets(sc.nextLine()), null, null);
		
		Stack<TagNode> stack = new Stack<TagNode>();
		stack.push(root);
		
		while(sc.hasNextLine())
		{
			String line = sc.nextLine();
			boolean isTag = false;
			
			if (line.charAt(0) == '<')
			{
				isTag = true;
				line = noBrackets(line);
			}
				
			if ((isTag) && (line.charAt(0) == '/'))
				stack.pop();

			else
			{
				TagNode temp = new TagNode(line, null, null);
				if (stack.peek().firstChild == null)
					stack.peek().firstChild = temp;
				else 
				{
					TagNode childNode = stack.peek().firstChild;
					while (childNode.sibling != null)
						childNode = childNode.sibling;
					childNode.sibling = temp;
				}
	
				if (isTag)
					stack.push(temp);
			}
		}
	}
	
	
	/**
	 * Replaces all occurrences of an old tag in the DOM tree with a new tag
	 * 
	 * @param oldTag Old tag
	 * @param newTag Replacement tag
	 */
	public void replaceTag(String oldTag, String newTag)
	{
		replace(oldTag, newTag, root);
	}
	
	/**
	 * Boldfaces every column of the given row of the table in the DOM tree. The boldface (b)
	 * tag appears directly under the td tag of every column of this row.
	 * 
	 * @param row Row to bold, first row is numbered 1 (not 0).
	 */
	public void boldRow(int row) 
	{
		bold(row, root);
	}
	
	/**
	 * Remove all occurrences of a tag from the DOM tree. If the tag is p, em, or b, all occurrences of the tag
	 * are removed. If the tag is ol or ul, then All occurrences of such a tag are removed from the tree, and, 
	 * in addition, all the li tags immediately under the removed tag are converted to p tags. 
	 * 
	 * @param tag Tag to be removed, can be p, em, b, ol, or ul
	 */
	public void removeTag(String tag)
	{
		if ((root == null) || (tag == null))
			return;

		else
		while (hasTag(tag, root))
			remove(tag, root, root.firstChild);
	}

	
	/**
	 * Adds a tag around all occurrences of a word in the DOM tree.
	 * 
	 * @param word Word around which tag is to be added
	 * @param tag Tag to be added
	 */
	public void addTag(String word, String tag)
	{
		if ((root == null) || (word == null) || (tag == null))
			return;
		
		else if ((tag.equals("em")) || (tag.equals("b")))
			add(word, tag, root);
	}
	
	/**
	 * Gets the HTML represented by this DOM tree. The returned string includes
	 * new lines, so that when it is printed, it will be identical to the
	 * input file from which the DOM tree was built.
	 * 
	 * @return HTML string, including new lines. 
	 */
	public String getHTML() {
		StringBuilder sb = new StringBuilder();
		getHTML(root, sb);
		return sb.toString();
	}
	
	private void getHTML(TagNode root, StringBuilder sb) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			if (ptr.firstChild == null) {
				sb.append(ptr.tag);
				sb.append("\n");
			} else {
				sb.append("<");
				sb.append(ptr.tag);
				sb.append(">\n");
				getHTML(ptr.firstChild, sb);
				sb.append("</");
				sb.append(ptr.tag);
				sb.append(">\n");	
			}
		}
	}
	
}
