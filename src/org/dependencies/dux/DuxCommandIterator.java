package org.dependencies.dux;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * A DUX command iterator
 * 
 * @author Daniel Couto-Vale
 */
public class DuxCommandIterator implements Iterator<DuxCommand> {
	
	private DuxQueryParser queryParser = new DuxQueryParser();

	/**
	 * The iterator builder
	 */
	private DuxCommandIteratorBuilder builder;

	/**
	 * The buffered reader
	 */
	private BufferedReader br;

	/**
	 * The next line
	 */
	private String line;
	
	/**
	 * A subiterator
	 */
	private DuxCommandIterator subiterator;

	/**
	 * Constructor
	 * 
	 * @param builder 
	 * @param file the file
	 */
	public DuxCommandIterator(DuxCommandIteratorBuilder builder, File file) {
		this.builder = builder;
		FileInputStream stream;
		try {
			stream = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		}
		InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
		br = new BufferedReader(reader);
		this.advance();
	}

	/**
	 * Advance to the next
	 */
	private final void advance() {
		while (true) {
			try {
				line = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				line = null;
			}
			if (line == null) {
				break;
			}
			line = line.trim();
			if (line.length() != 0 & !line.startsWith("#")) {
				break;
			}
			if (line.startsWith("##")) {
				break;
			}
		}
	}

	@Override
	public final boolean hasNext() {
		if (subiterator != null && subiterator.hasNext()) {
			return true;
		}
		return line != null;
	}

	@Override
	public final DuxCommand next() {
		if (!this.hasNext())
			return null;
		if (this.subiterator != null) {
			if (this.subiterator.hasNext()) {
				return this.subiterator.next();
			} else {
				this.subiterator = null;
			}
		}
		if (line.equals("STOP")) {
			this.advance();
			return new DuxStop();
		}
		if (line.startsWith("##")) {
			DuxPrint print = new DuxPrint(line.substring(2).trim());
			this.advance();
			return print;
		}
		if (line.startsWith("IMPORT")) {
			String[] A = line.split(" ");
			if (A.length != 2) {
				System.err.println("Error: " + line);
				this.advance();
				return null;
			}
			this.advance();
			this.subiterator = builder.build(A[1]);
			if (this.subiterator.hasNext()) {
				return this.subiterator.next();
			} else {
				this.subiterator = null;
			}
		}
		DuxTranslate translate = new DuxTranslate();
		String[] A = line.split("=>");
		if (A.length != 2) {
			System.err.println("Error: " + line);
			this.advance();
			return null;
		}
		String A0 = A[0].trim();
		String A1 = A[1].trim();

		// Parse query
		try {
			for (DuxPattern pattern : queryParser.parseQuery(A0)) {
				translate.addPattern(pattern);
			}
		} catch (DuxQueryParseError e) {
			System.err.println("Error: " + line);
			this.advance();
			return null;
		}

		// Parse update
		for (String token : A1.split(" ")) {
			token = token.trim();
			if (token.length() == 0)
				continue;
			if (token.startsWith("+")) {
				token = token.substring(1);
				if (DuxFeature.matches(token)) {
					translate.addMagisTag(new DuxFeature(token));
				}
				if (DuxFunction.matches(token)) {
					translate.addMagisTag(new DuxFunction(token));
				}
			}
			if (token.startsWith("-")) {
				token = token.substring(1);
				if (DuxFeature.matches(token)) {
					translate.addMinusTag(new DuxFeature(token));
				}
				if (DuxFunction.matches(token)) {
					translate.addMinusTag(new DuxFunction(token));
				}
			}
		}
		this.advance();
		return translate;
	}

}
