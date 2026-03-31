package com.azox.watch.swear;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SwearFilterService {

    private static final Map<Character, Character> LEET_MAP = Map.ofEntries(
            Map.entry('@', 'a'),
            Map.entry('4', 'a'),
            Map.entry('8', 'b'),
            Map.entry('3', 'e'),
            Map.entry('1', 'i'),
            Map.entry('!', 'i'),
            Map.entry('|', 'i'),
            Map.entry('0', 'o'),
            Map.entry('5', 's'),
            Map.entry('$', 's'),
            Map.entry('7', 't')
    );

    private final List<Pattern> blockedPatterns;

    public static SwearFilterService fromSettings(final FilterSettings filterSettings) {
        final List<String> blockedWords = filterSettings == null ? Collections.emptyList() : filterSettings.getBlockedWords();
        return new SwearFilterService(buildPatterns(blockedWords));
    }

    public boolean isMessageBlocked(final String message) {
        if (message == null || message.isBlank() || this.blockedPatterns == null || this.blockedPatterns.isEmpty()) {
            return false;
        }

        final String normalized = normalizeMessage(message);
        for (final Pattern pattern : this.blockedPatterns) {
            if (pattern != null && pattern.matcher(normalized).find()) {
                return true;
            }
        }
        return false;
    }

    private static List<Pattern> buildPatterns(final List<String> blockedWords) {
        if (blockedWords == null || blockedWords.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Pattern> patterns = new ArrayList<>();
        for (final String blockedWord : blockedWords) {
            if (blockedWord == null || blockedWord.isBlank()) {
                continue;
            }

            final String cleanedWord = cleanWord(blockedWord);
            if (cleanedWord.isEmpty()) {
                continue;
            }

            patterns.add(Pattern.compile(buildRegex(cleanedWord)));
        }

        return Collections.unmodifiableList(patterns);
    }

    private static String cleanWord(final String blockedWord) {
        final String lowered = blockedWord.toLowerCase();
        final StringBuilder out = new StringBuilder();

        for (int i = 0; i < lowered.length(); i++) {
            final char c = lowered.charAt(i);
            final Character mapped = LEET_MAP.get(c);
            if (mapped != null) {
                out.append(mapped);
            } else if (c >= 'a' && c <= 'z') {
                out.append(c);
            }
        }

        return out.toString();
    }

    private static String normalizeMessage(final String message) {
        final String lowered = message.toLowerCase();
        final StringBuilder out = new StringBuilder();

        for (int i = 0; i < lowered.length(); i++) {
            final char c = lowered.charAt(i);
            final Character mapped = LEET_MAP.get(c);
            if (mapped != null) {
                out.append(mapped);
            } else if (c >= 'a' && c <= 'z') {
                out.append(c);
            } else {
                out.append(' ');
            }
        }

        return out.toString();
    }

    private static String buildRegex(final String cleanedWord) {
        final StringBuilder regexBuilder = new StringBuilder("(?<![a-z])");
        for (int i = 0; i < cleanedWord.length(); i++) {
            regexBuilder.append(cleanedWord.charAt(i)).append('+');
            if (i < cleanedWord.length() - 1) {
                regexBuilder.append("[^a-z]*");
            }
        }
        regexBuilder.append("(?![a-z])");
        return regexBuilder.toString();
    }
}
