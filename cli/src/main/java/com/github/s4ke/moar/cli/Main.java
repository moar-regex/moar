package com.github.s4ke.moar.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.github.s4ke.moar.MoaMatcher;
import com.github.s4ke.moar.MoaPattern;
import com.github.s4ke.moar.json.MoarJSONSerializer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author Martin Braun
 */
public class Main {

	public static void main(String[] args) throws ParseException, IOException {
		// create Options object
		Options options = new Options();

		options.addOption( "rf", true, "regexFile" );
		options.addOption( "r", true, "regex" );

		options.addOption( "mf", true, "moaFile" );
		options.addOption( "mo", true, "moaOutputFolder (overwrites if existent)" );

		options.addOption( "sf", true, "stringFile" );
		options.addOption( "s", true, "string" );

		options.addOption( "m", false, "multiline" );

		options.addOption( "help", false, "prints this dialog" );

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse( options, args );

		if ( args.length == 0 || cmd.hasOption( "help" ) ) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "moar-cli", options );
			return;
		}

		List<String> patternNames = new ArrayList<>();
		List<MoaPattern> patterns = new ArrayList<>();
		List<String> stringsToCheck = new ArrayList<>();

		if ( cmd.hasOption( "r" ) ) {
			String regexStr = cmd.getOptionValue( "r" );
			try {
				patterns.add( MoaPattern.compile( regexStr ) );
				patternNames.add( regexStr );
			}
			catch (Exception e) {
				System.out.println( e.getMessage() );
				return;
			}
		}

		if ( cmd.hasOption( "rf" ) ) {
			String fileName = cmd.getOptionValue( "rf" );
			List<String> regexFileContents = readFileContents( new File( fileName ) );
			int emptyLineCountAfterRegex = 0;
			StringBuilder regexStr = new StringBuilder();
			for ( String line : regexFileContents ) {
				if ( emptyLineCountAfterRegex >= 1 ) {
					if ( regexStr.length() > 0 ) {
						patterns.add( MoaPattern.compile( regexStr.toString() ) );
						patternNames.add(regexStr.toString());
					}
					regexStr.setLength( 0 );
					emptyLineCountAfterRegex = 0;
				}
				if ( line.trim().equals( "" ) ) {
					if ( regexStr.length() > 0 ) {
						++emptyLineCountAfterRegex;
					}
				}
				else {
					regexStr.append( line );
				}
			}
			if ( regexStr.length() > 0 ) {
				try {
					patterns.add( MoaPattern.compile( regexStr.toString() ) );
					patternNames.add(regexStr.toString());
				}
				catch (Exception e) {
					System.out.println( e.getMessage() );
					return;
				}
				regexStr.setLength( 0 );
			}
		}

		if ( cmd.hasOption( "mf" ) ) {
			String fileName = cmd.getOptionValue( "mf" );
			File file = new File( fileName );
			if ( file.isDirectory() ) {
				System.out.println( fileName + " is a directory, using all *.moar files as patterns" );
				File[] moarFiles = file.listFiles(
						pathname -> pathname.getName().endsWith( ".moar" )
				);
				for ( File moar : moarFiles ) {
					String jsonString = readWholeFile( moar );
					patterns.add( MoarJSONSerializer.fromJSON( jsonString ) );
					patternNames.add(moar.getAbsolutePath());
				}
			}
			else {
				System.out.println( fileName + " is a single file. using it directly (no check for *.moar suffix)" );
				String jsonString = readWholeFile( file );
				patterns.add( MoarJSONSerializer.fromJSON( jsonString ) );
				patternNames.add(fileName);
			}
		}

		if ( cmd.hasOption( "s" ) ) {
			String str = cmd.getOptionValue( "s" );
			stringsToCheck.add( str );
		}

		if ( cmd.hasOption( "sf" ) ) {
			String fileName = cmd.getOptionValue( "sf" );
			StringBuilder stringBuilder = new StringBuilder();
			boolean firstLine = true;
			for ( String str : readFileContents( new File( fileName ) ) ) {
				if ( !firstLine ) {
					stringBuilder.append( "\n" );
				}
				if ( firstLine ) {
					firstLine = false;
				}
				stringBuilder.append( str );
			}
			stringsToCheck.add( stringBuilder.toString() );
		}

		boolean multiLine = cmd.hasOption( "m" );

		if ( patterns.size() == 0 ) {
			System.out.println( "no patterns to check" );
			return;
		}

		if ( cmd.hasOption( "mo" ) ) {
			String folder = cmd.getOptionValue( "mo" );
			File folderFile = new File( folder );
			if ( !folderFile.exists() ) {
				System.out.println( folder + " does not exist. creating..." );
				if ( !folderFile.mkdirs() ) {
					System.out.println( "folder " + folder + " could not be created" );
				}
			}
			int cnt = 0;
			for ( MoaPattern pattern : patterns ) {
				String patternAsJSON = MoarJSONSerializer.toJSON( pattern );
				try (BufferedWriter writer = new BufferedWriter(
						new FileWriter(
								new File(
										folderFile,
										"pattern" + ++cnt + ".moar"
								)
						)
				)) {
					writer.write( patternAsJSON );
				}
			}
			System.out.println( "stored " + cnt + " patterns in " + folder );
		}

		if ( stringsToCheck.size() == 0 ) {
			System.out.println( "no strings to check" );
			return;
		}

		for ( String string : stringsToCheck ) {
			int curPattern = 0;
			for ( MoaPattern pattern : patterns ) {
				MoaMatcher matcher = pattern.matcher( string );
				if ( !multiLine ) {
					if ( matcher.matches() ) {
						System.out.println( "\"" + patternNames.get(curPattern) + "\" matches \"" + string + "\"" );
					}
					else {
						System.out.println( "\"" + patternNames.get(curPattern) + "\" does not match \"" + string + "\"" );
					}
				}
				else {
					StringBuilder buffer = new StringBuilder( string );
					int additionalCharsPerMatch = ("<match>" + "</match>").length();
					int matchCount = 0;
					while ( matcher.nextMatch() ) {
						buffer.replace(
								matcher.getStart() + matchCount * additionalCharsPerMatch,
								matcher.getEnd() + matchCount * additionalCharsPerMatch,
								"<match>" + string.substring(
										matcher.getStart(),
										matcher.getEnd()
								) + "</match>"
						);
						++matchCount;
					}
					System.out.println( buffer.toString() );
				}
			}
			++curPattern;
		}
	}

	private static String readWholeFile(File file) throws IOException {
		StringBuilder ret = new StringBuilder();
		try (FileInputStream fis = new FileInputStream( file );
			 BufferedReader reader = new BufferedReader( new InputStreamReader( fis ) )) {
			String str;
			while ( (str = reader.readLine()) != null ) {
				ret.append( str ).append( "\n" );
			}
		}
		return ret.toString();
	}

	private static List<String> readFileContents(File file) throws IOException {
		List<String> ret = new ArrayList<>();
		try (FileInputStream fis = new FileInputStream( file );
			 BufferedReader reader = new BufferedReader( new InputStreamReader( fis ) )) {
			String str;
			while ( (str = reader.readLine()) != null ) {
				ret.add( str );
			}
		}
		return ret;
	}

}
