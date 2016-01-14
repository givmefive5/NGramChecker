package revised.test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import revised.model.NGram;
import revised.model.Suggestion;
import revised.model.SuggestionToken;
import revised.model.SuggestionType;

public class SubstitutionService {

	CandidateNGramsService candidateNGramService;

	public SubstitutionService() {
		candidateNGramService = CandidateNGramsService.getInstance();
	}

	public List<Suggestion> computeSubstitution(String[] wArr, String[] lArr, String[] pArr) throws SQLException {
		List<NGram> candidateSubNGrams = candidateNGramService.getCandidateNGrams(pArr, pArr.length);

		List<Suggestion> suggestions = new ArrayList<>();
		for (NGram n : candidateSubNGrams) {
			String[] nPOS = n.getPos();
			String[] nWords = n.getWords();
			String[] nLemmas = n.getLemmas();
			Boolean[] isPOSGeneralized = n.getIsPOSGeneralized();

			double editDistance = 0;
			List<SuggestionToken> replacements = new ArrayList<>();
			for (int i = 0; i < nPOS.length; i++) {
				if (isPOSGeneralized != null && isPOSGeneralized[i] == true && nPOS[i].equals(pArr[i]))
					;
				else if (nWords[i].equals(wArr[i]))
					;
				else {
					if (nLemmas[i].equals(lArr[i]))
						editDistance += 0.1;
					else if (withinSpellingEditDistance(nWords[i], wArr[i]))
						editDistance += 0.2;
					else if (isPOSGeneralized != null && isPOSGeneralized[i]
							&& hasCloseWordFromDictionary(wArr[i], nPOS[i])) {
						editDistance += 0.3;
					} else if (nPOS[i].equals(pArr[i]))
						editDistance += 0.9;
					else
						editDistance += 1;

					if (isPOSGeneralized != null && isPOSGeneralized[i] == true)
						replacements.add(new SuggestionToken(nWords[i], i, editDistance, nPOS[i]));
					else
						replacements.add(new SuggestionToken(nWords[i], i, editDistance));
				}
			}
			SuggestionToken[] replacementWords = replacements.toArray(new SuggestionToken[replacements.size()]);
			suggestions.add(new Suggestion(replacementWords, SuggestionType.SUBSTITUTION, editDistance));

			if (editDistance == 0) {
				suggestions.add(new Suggestion(0));
				return suggestions;
			}
		}
		return suggestions;

	}

	private boolean hasCloseWordFromDictionary(String mispelledWord, String expectedPOS) {
		// TODO Auto-generated method stub
		return false;
	}

	private static boolean withinSpellingEditDistance(String corpusWord, String input) {
		int distance = EditDistanceService.computeLevenshteinDistance(corpusWord, input);
		if (distance <= 2) {
			return true;
		} else
			return false;
	}
}
