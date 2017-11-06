package de.jdellert.iwsa;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;

import de.jdellert.iwsa.align.LevenshteinAlignmentAlgorithm;
import de.jdellert.iwsa.align.NeedlemanWunschAlgorithm;
import de.jdellert.iwsa.align.PhoneticStringAlignment;
import de.jdellert.iwsa.corrmodel.CorrespondenceModel;
import de.jdellert.iwsa.corrmodel.CorrespondenceModelInference;
import de.jdellert.iwsa.corrmodel.CorrespondenceModelStorage;
import de.jdellert.iwsa.data.CLDFImport;
import de.jdellert.iwsa.data.LexicalDatabase;
import de.jdellert.iwsa.sequence.PhoneticString;
import de.jdellert.iwsa.sequence.PhoneticSymbolTable;
import de.jdellert.iwsa.stat.CategoricalDistribution;
import de.jdellert.iwsa.stat.SmoothingMethod;

public class ConceptLevelWeightedEditDistanceOutput {
	public static void main(String[] args) {
		try {
			LexicalDatabase database = CLDFImport.loadDatabase(args[0], true);
			PhoneticSymbolTable symbolTable = database.getSymbolTable();

			CorrespondenceModel globalCorrModel = null;
			try {
				System.err.print("Attempting to load existing global correspondence model from " + args[0]
						+ "-global.corr ... ");
				globalCorrModel = CorrespondenceModelStorage
						.loadCorrespondenceModel(new ObjectInputStream(new FileInputStream(args[0] + "-global.corr")));
				System.err.print("done.");
			} catch (FileNotFoundException e) {
				System.err.print(" failed, need to infer global model first.\n");
			} catch (IOException e) {
				System.err.print(" failed, need to infer global model first.\n");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				System.exit(0);
			}
			if (globalCorrModel == null)
			{
				globalCorrModel = CorrespondenceModelInference.inferGlobalCorrespondenceModel(database, symbolTable);
				CorrespondenceModelStorage.writeGlobalModelToFile(globalCorrModel, args[0] + "-global.corr");
			}

			System.err.print("Stage 2: Inference of sound correspondence matrices for each language pair\n");
			CorrespondenceModel[][] localCorrModels = new CorrespondenceModel[database.getNumLanguages()][database
					.getNumLanguages()];
			// estimation of language-specific sound correspondences;
			// use global correspondences only in first iteration
			for (int lang1ID = 0; lang1ID < database.getNumLanguages(); lang1ID++) {
				for (int lang2ID = 0; lang2ID < database.getNumLanguages(); lang2ID++) {
					System.err.print("  Pair " + database.getLanguageCode(lang1ID) + "/"
							+ database.getLanguageCode(lang2ID) + ":\n");
					CategoricalDistribution cognateCorrespondenceDistForPair = new CategoricalDistribution(
							symbolTable.getSize() * symbolTable.getSize(), SmoothingMethod.LAPLACE);
					int numPairs = 0;
					int numCognatePairs = 0;
					System.err.print("    Step 1: Finding cognate candidates based on global WED ...");
					for (int conceptID = 0; conceptID < database.getNumConcepts(); conceptID++) {
						List<List<Integer>> formsPerLang = database.getFormIDsForConceptPerLanguage(conceptID);
						for (int lang1FormID : formsPerLang.get(lang1ID)) {
							PhoneticString lang1Form = database.getForm(lang1FormID);
							for (int lang2FormID : formsPerLang.get(lang2ID)) {
								PhoneticString lang2Form = database.getForm(lang2FormID);
								PhoneticStringAlignment alignment = NeedlemanWunschAlgorithm
										.constructAlignment(lang1Form, lang2Form, globalCorrModel);
								numPairs++;
								if (alignment.normalizedDistanceScore <= 0.7) {
									for (int pos = 0; pos < alignment.getLength(); pos++) {
										cognateCorrespondenceDistForPair
												.addObservation(alignment.getSymbolPairIDAtPos(pos, symbolTable));
									}
									numCognatePairs++;
								}
							}
						}
					}

					System.err.print(" done. Aligned " + numPairs + " form pairs, of which " + numCognatePairs
							+ " look like cognates (normalized edit distance < 0.7)\n");
					CategoricalDistribution randomCorrespondenceDistForPair = new CategoricalDistribution(
							symbolTable.getSize() * symbolTable.getSize(), SmoothingMethod.LAPLACE);
					System.err.print("            Creating " + (numCognatePairs * 50)
							+ " random alignments to model the distribution in absence of correspondences ...");
					for (int i = 0; i < numCognatePairs * 50; i++) {
						PhoneticString form1 = database.getRandomFormForLanguage(lang1ID);
						PhoneticString form2 = database.getRandomFormForLanguage(lang2ID);
						PhoneticStringAlignment alignment = NeedlemanWunschAlgorithm.constructAlignment(form1, form2,
								globalCorrModel);
						for (int pos = 0; pos < alignment.getLength(); pos++) {
							randomCorrespondenceDistForPair
									.addObservation(alignment.getSymbolPairIDAtPos(pos, symbolTable));
						}
					}
					System.err.print(" done.\n");

					System.err.print(
							"          Comparing the distributions of symbol pairs to reestimate PMI scores ...");
					CorrespondenceModel localCorr = new CorrespondenceModel(symbolTable);
					for (int symbolPairID = 0; symbolPairID < symbolTable.getSize()
							* symbolTable.getSize(); symbolPairID++) {
						double cognateSymbolPairProbability = cognateCorrespondenceDistForPair.getProb(symbolPairID);
						double randomSymbolPairProbability = randomCorrespondenceDistForPair.getProb(symbolPairID);
						double pmiScore = Math.log(cognateSymbolPairProbability / randomSymbolPairProbability);
						localCorr.setScore(symbolPairID, pmiScore);
					}
					System.err.print(" done.\n");

					int numLocalInferenceIterations = 3;
					System.err.print("    Step 2: Reestimation based on Needleman-Wunsch ("
							+ numLocalInferenceIterations + " iterations)\n");

					for (int iteration = 0; iteration < numLocalInferenceIterations; iteration++) {
						System.err.print(
								"    Iteration 0" + (iteration + 1) + ": Finding WED-based cognate candidates ...");
						numPairs = 0;
						numCognatePairs = 0;
						cognateCorrespondenceDistForPair = new CategoricalDistribution(
								symbolTable.getSize() * symbolTable.getSize(), SmoothingMethod.LAPLACE);
						for (int conceptID = 0; conceptID < database.getNumConcepts(); conceptID++) {
							List<List<Integer>> formsPerLang = database.getFormIDsForConceptPerLanguage(conceptID);
							for (int lang1FormID : formsPerLang.get(lang1ID)) {
								PhoneticString lang1Form = database.getForm(lang1FormID);
								for (int lang2FormID : formsPerLang.get(lang2ID)) {
									PhoneticString lang2Form = database.getForm(lang2FormID);
									PhoneticStringAlignment alignment = NeedlemanWunschAlgorithm
											.constructAlignment(lang1Form, lang2Form, localCorr);
									numPairs++;
									if (alignment.normalizedDistanceScore <= 0.7) {
										for (int pos = 0; pos < alignment.getLength(); pos++) {
											cognateCorrespondenceDistForPair
													.addObservation(alignment.getSymbolPairIDAtPos(pos, symbolTable));
										}
										numCognatePairs++;
									}
								}
							}
						}

						System.err.print(" done. " + numCognatePairs
								+ " form pairs look like cognates (normalized aligment score < 0.7)\n");
						randomCorrespondenceDistForPair = new CategoricalDistribution(
								symbolTable.getSize() * symbolTable.getSize(), SmoothingMethod.LAPLACE);
						System.err.print("          Creating " + (numCognatePairs * 10)
								+ " random alignments to model the distribution in absence of correspondences ...");
						for (int i = 0; i < numCognatePairs * 10; i++) {
							PhoneticString form1 = database.getRandomFormForLanguage(lang1ID);
							PhoneticString form2 = database.getRandomFormForLanguage(lang2ID);
							PhoneticStringAlignment alignment = NeedlemanWunschAlgorithm.constructAlignment(form1,
									form2, localCorr);
							for (int pos = 0; pos < alignment.getLength(); pos++) {
								randomCorrespondenceDistForPair
										.addObservation(alignment.getSymbolPairIDAtPos(pos, symbolTable));
							}
						}
						System.err.print(" done.\n");

						System.err.print(
								"          Comparing the distributions of symbol pairs to reestimate PMI scores ...");
						localCorr = new CorrespondenceModel(symbolTable);
						for (int symbolPairID = 0; symbolPairID < symbolTable.getSize()
								* symbolTable.getSize(); symbolPairID++) {
							double cognateSymbolPairProbability = cognateCorrespondenceDistForPair
									.getProb(symbolPairID);
							double randomSymbolPairProbability = randomCorrespondenceDistForPair.getProb(symbolPairID);
							double pmiScore = Math.log(cognateSymbolPairProbability / randomSymbolPairProbability);
							localCorr.setScore(symbolPairID, pmiScore);
						}
						System.err.print(" done.\n");
					}
					localCorrModels[lang1ID][lang2ID] = localCorr;
				}
			}

			// finally: output of distances
			for (int conceptID = 0; conceptID < database.getNumConcepts(); conceptID++) {
				List<List<Integer>> formsPerLang = database.getFormIDsForConceptPerLanguage(conceptID);
				for (int lang1ID = 0; lang1ID < database.getNumLanguages(); lang1ID++) {
					for (int lang2ID = 0; lang2ID < database.getNumLanguages(); lang2ID++) {
						for (int lang1FormID : formsPerLang.get(lang1ID)) {
							PhoneticString lang1Form = database.getForm(lang1FormID);
							for (int lang2FormID : formsPerLang.get(lang2ID)) {
								PhoneticString lang2Form = database.getForm(lang2FormID);
								PhoneticStringAlignment globalWeightsAlignment = NeedlemanWunschAlgorithm
										.constructAlignment(lang1Form, lang2Form, globalCorrModel);
								double globalWeightDistance = globalWeightsAlignment.normalizedDistanceScore;
								PhoneticStringAlignment localWeightsAlignment = NeedlemanWunschAlgorithm
										.constructAlignment(lang1Form, lang2Form, localCorrModels[lang1ID][lang2ID]);
								double localWeightDistance = localWeightsAlignment.normalizedDistanceScore;
								double minDistance = Math.min(globalWeightDistance, localWeightDistance);
								System.out.print(database.getConceptName(conceptID) + "\t");
								System.out.print(database.getLanguageCode(lang1ID) + "\t"
										+ database.getLanguageCode(lang2ID) + "\t");
								System.out.print(database.getAnnotation("Word_Form", lang1FormID) + "\t"
										+ database.getAnnotation("Word_Form", lang2FormID) + "\t");
								System.out.print(lang1Form.toString(symbolTable) + "\t"
										+ lang2Form.toString(symbolTable) + "\t");
								System.out.println(
										globalWeightDistance + "\t" + localWeightDistance + "\t" + minDistance);
							}
						}
					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}