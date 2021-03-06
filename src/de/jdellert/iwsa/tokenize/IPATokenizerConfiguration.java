package de.jdellert.iwsa.tokenize;

/**
 * Models a configuration for an IPATokenizer, essentially just a combination of
 * binary flags, with the default configuration tuned towards NorthEuraLex.
 *
 */

public class IPATokenizerConfiguration {
    public final boolean SEPARATE_EJECTIVITY;

    public final boolean SEPARATE_ASPIRATION;
    public final boolean SEPARATE_NASAL_RELEASE;
    public final boolean SEPARATE_LATERAL_RELEASE;

    public final boolean SEPARATE_LABIALIZATION;
    public final boolean SEPARATE_DENTALIZATION;
    public final boolean SEPARATE_PALATALIZATION;
    public final boolean SEPARATE_VELARIZATION;
    public final boolean SEPARATE_NASALIZATION;
    public final boolean SEPARATE_GLOTTALIZATION;
    public final boolean SEPARATE_PHARYNGEALIZATION;

    public final boolean SEPARATE_DEVOICING;

    public final boolean SINGLE_SEGMENT_AFFRICATES;
    public final boolean SINGLE_SEGMENT_DIPHTHONGS;
    public final boolean SINGLE_SEGMENT_LONG_VOWELS;
    public final boolean SINGLE_SEGMENT_GEMINATES;

    public final boolean IGNORE_TONES;
    public final boolean IGNORE_STRESS;
    public final boolean IGNORE_SYLLABICITY;
    public final boolean IGNORE_PHONATION_DIACRITICS;
    public final boolean IGNORE_ARTICULATION_DIACRITICS;

    public final boolean IGNORE_UNKNOWN_SYMBOLS;

    // default tokenization: as in my dissertation (Dellert 2017)
    public IPATokenizerConfiguration() {
        SEPARATE_EJECTIVITY = true;

        SEPARATE_ASPIRATION = true;
        SEPARATE_NASAL_RELEASE = true;
        SEPARATE_LATERAL_RELEASE = true;

        SEPARATE_LABIALIZATION = true;
        SEPARATE_PALATALIZATION = true;
        SEPARATE_DENTALIZATION = false;
        SEPARATE_VELARIZATION = true;
        SEPARATE_NASALIZATION = true;
        SEPARATE_GLOTTALIZATION = true;
        SEPARATE_PHARYNGEALIZATION = true;

        SEPARATE_DEVOICING = true;

        SINGLE_SEGMENT_AFFRICATES = true;
        SINGLE_SEGMENT_DIPHTHONGS = false;
        SINGLE_SEGMENT_LONG_VOWELS = false;
        SINGLE_SEGMENT_GEMINATES = false;

        IGNORE_TONES = true;
        IGNORE_STRESS = true;
        IGNORE_SYLLABICITY = true;
        IGNORE_PHONATION_DIACRITICS = true;
        IGNORE_ARTICULATION_DIACRITICS = true;

        IGNORE_UNKNOWN_SYMBOLS = true;
    }

    public IPATokenizerConfiguration(boolean SEPARATE_EJECTIVITY, boolean SEPARATE_ASPIRATION,
                                     boolean SEPARATE_NASAL_RELEASE, boolean SEPARATE_LATERAL_RELEASE, boolean SEPARATE_LABIALIZATION,
                                     boolean SEPARATE_DENTALIZATION, boolean SEPARATE_PALATALIZATION, boolean SEPARATE_VELARIZATION,
                                     boolean SEPARATE_NASALIZATION, boolean SEPARATE_GLOTTALIZATION, boolean SEPARATE_PHARYNGEALIZATION,
                                     boolean SEPARATE_DEVOICING, boolean SINGLE_SEGMENT_AFFRICATES, boolean SINGLE_SEGMENT_DIPHTHONGS,
                                     boolean SINGLE_SEGMENT_LONG_VOWELS, boolean SINGLE_SEGMENT_GEMINATES, boolean IGNORE_TONES,
                                     boolean IGNORE_STRESS, boolean IGNORE_SYLLABICITY, boolean IGNORE_PHONATION_DIACRITICS,
                                     boolean IGNORE_ARTICULATION_DIACRITICS, boolean IGNORE_UNKNOWN_SYMBOLS) {
        this.SEPARATE_EJECTIVITY = SEPARATE_EJECTIVITY;

        this.SEPARATE_ASPIRATION = SEPARATE_ASPIRATION;
        this.SEPARATE_NASAL_RELEASE = SEPARATE_NASAL_RELEASE;
        this.SEPARATE_LATERAL_RELEASE = SEPARATE_LATERAL_RELEASE;

        this.SEPARATE_LABIALIZATION = SEPARATE_LABIALIZATION;
        this.SEPARATE_DENTALIZATION = SEPARATE_DENTALIZATION;
        this.SEPARATE_PALATALIZATION = SEPARATE_PALATALIZATION;
        this.SEPARATE_VELARIZATION = SEPARATE_VELARIZATION;
        this.SEPARATE_NASALIZATION = SEPARATE_NASALIZATION;
        this.SEPARATE_GLOTTALIZATION = SEPARATE_GLOTTALIZATION;
        this.SEPARATE_PHARYNGEALIZATION = SEPARATE_PHARYNGEALIZATION;

        this.SEPARATE_DEVOICING = SEPARATE_DEVOICING;

        this.SINGLE_SEGMENT_AFFRICATES = SINGLE_SEGMENT_AFFRICATES;
        this.SINGLE_SEGMENT_DIPHTHONGS = SINGLE_SEGMENT_DIPHTHONGS;
        this.SINGLE_SEGMENT_LONG_VOWELS = SINGLE_SEGMENT_LONG_VOWELS;
        this.SINGLE_SEGMENT_GEMINATES = SINGLE_SEGMENT_GEMINATES;

        this.IGNORE_TONES = IGNORE_TONES;
        this.IGNORE_STRESS = IGNORE_STRESS;
        this.IGNORE_SYLLABICITY = IGNORE_SYLLABICITY;
        this.IGNORE_PHONATION_DIACRITICS = IGNORE_PHONATION_DIACRITICS;
        this.IGNORE_ARTICULATION_DIACRITICS = IGNORE_ARTICULATION_DIACRITICS;

        this.IGNORE_UNKNOWN_SYMBOLS = IGNORE_UNKNOWN_SYMBOLS;
    }
}