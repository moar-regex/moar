package com.github.s4ke.moar;

import com.github.s4ke.moar.util.CharSeq;

/**
 * @author Martin Braun
 */
public interface MoaMatcher {

	MoaMatcher reuse(CharSequence str);

	MoaMatcher reuse(CharSeq seq);

	String replaceFirst(String replacement);

	String replaceAll(String replacement);

	int getStart();

	int getEnd();

	boolean nextMatch();

	boolean matches();

	String getVariableContent(int occurence);

	String getVariableContent(String name);

}
