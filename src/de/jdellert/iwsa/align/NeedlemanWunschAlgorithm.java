package de.jdellert.iwsa.align;

import java.util.LinkedList;
import java.util.List;

import de.jdellert.iwsa.corrmodel.CorrespondenceModel;
import de.jdellert.iwsa.sequence.PhoneticString;

public class NeedlemanWunschAlgorithm {
	public static PhoneticStringAlignment constructAlignment(PhoneticString str1, PhoneticString str2, CorrespondenceModel corrModel) {
		int m = str1.getLength() + 1;
		int n = str2.getLength() + 1;

		double[][] mtx = new double[m][n];
		int[][] aSubst = new int[m][n];
		int[][] bSubst = new int[m][n];
		mtx[0][0] = 0;
		for (int i = 1; i < m; i++) {
			mtx[i][0] = mtx[i - 1][0] + corrModel.getScore(str1.segments[i - 1], 1);
			aSubst[i][0] = str1.segments[i - 1];
			bSubst[i][0] = 1; // corresponds to gap symbol
		}
		for (int j = 1; j < n; j++) {
			mtx[0][j] = mtx[0][j - 1] + corrModel.getScore(1, str2.segments[j - 1]);
			aSubst[0][j] = 1; // corresponds to gap symbol
			bSubst[0][j] = str2.segments[j - 1];
		}
		for (int i = 1; i < m; i++) {
			for (int j = 1; j < n; j++) {

				double matchValue = mtx[i - 1][j - 1] + corrModel.getScore(str1.segments[i - 1], str2.segments[j - 1]);
				if (str1.segments[i-1] != str2.segments[j-1])
					matchValue++;
				double insertionValue = mtx[i][j - 1] + corrModel.getScore(1, str2.segments[j - 1]);
				double deletionValue = mtx[i - 1][j] + corrModel.getScore(str1.segments[i - 1], 1);
				mtx[i][j] = Math.max(matchValue, Math.max(insertionValue, deletionValue));

				if (insertionValue > matchValue) {
					if (deletionValue > insertionValue) {
						mtx[i][j] = deletionValue;
						aSubst[i][j] = str1.segments[i - 1];
						bSubst[i][j] = 1;
					} else {
						mtx[i][j] = insertionValue;
						aSubst[i][j] = 1;
						bSubst[i][j] = str2.segments[j - 1];
					}
				} else {
					if (deletionValue > matchValue) {
						mtx[i][j] = deletionValue;
						aSubst[i][j] = str1.segments[i - 1];
						bSubst[i][j] = 1;
					} else {
						mtx[i][j] = matchValue;
						aSubst[i][j] = str1.segments[i - 1];
						bSubst[i][j] = str2.segments[j - 1];
					}
				}
			}
		}

		double similarityScore = mtx[m - 1][n - 1];
		double str1SelfSimilarity = 0.0;
		for (int segmentID : str1.segments)
		{
			str1SelfSimilarity += corrModel.getScore(segmentID, segmentID);
		}
		double str2SelfSimilarity = 0.0;
		for (int segmentID : str2.segments)
		{
			str2SelfSimilarity += corrModel.getScore(segmentID, segmentID);
		}
		double normalizedDistanceScore = 1 - (2 * similarityScore) / (str1SelfSimilarity + str2SelfSimilarity);

		// build the alignment from the backpointer substrings
		int i = m - 1;
		int j = n - 1;
		List<Integer> result1 = new LinkedList<Integer>();
		List<Integer> result2 = new LinkedList<Integer>();
		while (i > 0 || j > 0) {
			int aPart = aSubst[i][j];
			int bPart = bSubst[i][j];
			result1.add(0, aPart);
			result2.add(0, bPart);
			if (aPart != 1)
				i--;
			if (bPart != 1)
				j--;
			if (aPart == 1 && bPart == 1) {
				i--;
				j--;
			}
			if (i < 0 || j < 0)
				break;
		}

		PhoneticStringAlignment alignment = new PhoneticStringAlignment();
		alignment.str1 = new PhoneticString(result1.stream().mapToInt(Integer::intValue).toArray());
		alignment.str2 = new PhoneticString(result2.stream().mapToInt(Integer::intValue).toArray());
		alignment.alignmentScore =similarityScore;
		alignment.normalizedDistanceScore = normalizedDistanceScore;

		return alignment;
	}
}