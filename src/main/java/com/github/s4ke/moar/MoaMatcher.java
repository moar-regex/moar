package com.github.s4ke.moar;

/**
 * @author Martin Braun
 */
public interface MoaMatcher {

	void reuse(String str);

	String replaceFirst(String replacement);

	boolean nextMatch();

	boolean matches();

	String getVariableContent(int occurence);

	String getVariableContent(String name);

}
