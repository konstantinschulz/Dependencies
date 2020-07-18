package org.dependencies;

import static java.lang.String.format;

import org.dependencies.conllu.ConlluFileWriter;
import org.dependencies.model.DepAnalyzedText;
import org.dependencies.model.DepAnalyzedTextLoader;

/**
 * Exports a CONLLU file.
 * 
 * @author Daniel Couto-Vale
 */
public class ExportConlluFile {

	/**
	 * Main
	 * 
	 * @param args the script arguments
	 */
	public final static void main(String[] args) {
		if (args.length != 6) {
			System.err.println("USAGE:");
			System.err.println("java -jar export-conllu-file.jar [corpus] [language] [text] [description] [analysis] [file]");
			System.exit(-1);
		}
		String corpusName = args[0];
		String languageName = args[1];
		String textTitle = args[2];
		String descriptionName = args[3];
		String analysisName = args[4];
		String fileName = args[5];
		DepAnalyzedTextLoader loader = new DepAnalyzedTextLoader();
		ConlluFileWriter exporter = new ConlluFileWriter();
		try {
			System.out.println(format("Exporting %s to %s", textTitle, fileName));
			DepAnalyzedText text = loader.loadAnalyzedText(corpusName, languageName, descriptionName, analysisName, textTitle);
			exporter.writeConlluFile(text, fileName);
			System.out.println("Exported!");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("There is a bug in this script.");
		}
	}

}
