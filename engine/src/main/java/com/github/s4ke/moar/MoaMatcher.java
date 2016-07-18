package com.github.s4ke.moar;

/**
 * @author Martin Braun
 */
public interface MoaMatcher {

	void reuse(String str);

	String replaceFirst(String replacement);

	String replaceAll(String replacement);

	int getStart();

	int getEnd();

	boolean nextMatch();

	boolean matches();

	String getVariableContent(int occurence);

	String getVariableContent(String name);

}
