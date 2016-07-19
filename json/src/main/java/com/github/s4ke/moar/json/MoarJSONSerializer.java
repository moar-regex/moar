package com.github.s4ke.moar.json;

import com.github.s4ke.moar.MoaPattern;
import com.github.s4ke.moar.moa.Moa;

/**
 * @author Martin Braun
 */
public final class MoarJSONSerializer {

	private MoarJSONSerializer() {
		//can't touch this!
	}

	public String toJSON(MoaPattern moaPattern) {
		String[] json = new String[0];
		moaPattern.accessMoa(
				(moa) -> json[0] = toJSON( moa )
		);
		return json[0];
	}

	private static String toJSON(Moa moa) {
		return "";
	}

}
