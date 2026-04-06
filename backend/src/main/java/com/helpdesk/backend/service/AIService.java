package com.helpdesk.backend.service;

import com.helpdesk.backend.model.IssueType;
import com.helpdesk.backend.model.TicketPriority;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class AIService {

    private static final Set<String> STOP_WORDS = Set.of(
        "a", "an", "and", "are", "as", "at", "be", "been", "for", "from", "has", "have",
        "i", "in", "is", "it", "my", "of", "on", "or", "that", "the", "this", "to", "with"
    );

    private final Map<IssueType, Map<String, Integer>> tokenFrequencyByType = new EnumMap<>(IssueType.class);
    private final Map<IssueType, Integer> documentCountByType = new EnumMap<>(IssueType.class);
    private final Map<IssueType, Integer> tokenCountByType = new EnumMap<>(IssueType.class);
    private final Map<String, Integer> vocabulary = new HashMap<>();

    @PostConstruct
    void trainModel() {
        List<TrainingExample> samples = List.of(
            new TrainingExample(IssueType.NETWORK, "wifi disconnects every few minutes in office"),
            new TrainingExample(IssueType.NETWORK, "internet is down and websites are not loading"),
            new TrainingExample(IssueType.NETWORK, "vpn connection fails when working from home"),
            new TrainingExample(IssueType.NETWORK, "network cable connection keeps dropping"),
            new TrainingExample(IssueType.NETWORK, "cannot connect to server because of timeout"),
            new TrainingExample(IssueType.NETWORK, "slow internet speed on company laptop"),
            new TrainingExample(IssueType.SOFTWARE, "email application crashes when opening inbox"),
            new TrainingExample(IssueType.SOFTWARE, "software installation failed with error message"),
            new TrainingExample(IssueType.SOFTWARE, "cannot log in to application after update"),
            new TrainingExample(IssueType.SOFTWARE, "browser page shows error while submitting form"),
            new TrainingExample(IssueType.SOFTWARE, "outlook email sync is not working"),
            new TrainingExample(IssueType.SOFTWARE, "password reset page is not opening correctly"),
            new TrainingExample(IssueType.HARDWARE, "printer is jammed and not printing"),
            new TrainingExample(IssueType.HARDWARE, "laptop screen is flickering after startup"),
            new TrainingExample(IssueType.HARDWARE, "keyboard keys are not working properly"),
            new TrainingExample(IssueType.HARDWARE, "mouse stopped responding on desktop"),
            new TrainingExample(IssueType.HARDWARE, "monitor has no display signal"),
            new TrainingExample(IssueType.HARDWARE, "device is overheating and shuts down"),
            new TrainingExample(IssueType.SECURITY, "phishing email reported by employee"),
            new TrainingExample(IssueType.SECURITY, "suspicious login attempt detected on account"),
            new TrainingExample(IssueType.SECURITY, "access to secure system denied after policy change"),
            new TrainingExample(IssueType.HR, "payroll portal not showing salary slip"),
            new TrainingExample(IssueType.HR, "leave approval workflow not working in hr system"),
            new TrainingExample(IssueType.HR, "unable to complete employee onboarding form")
        );

        for (IssueType issueType : IssueType.values()) {
            tokenFrequencyByType.put(issueType, new HashMap<>());
            documentCountByType.put(issueType, 0);
            tokenCountByType.put(issueType, 0);
        }

        for (TrainingExample sample : samples) {
            documentCountByType.compute(sample.issueType(), (key, count) -> count == null ? 1 : count + 1);

            for (String token : tokenize(sample.text())) {
                vocabulary.merge(token, 1, (oldValue, newValue) -> oldValue == null ? newValue : oldValue + newValue);
                tokenFrequencyByType.get(sample.issueType()).merge(token, 1, (oldValue, newValue) -> oldValue == null ? newValue : oldValue + newValue);
                tokenCountByType.compute(sample.issueType(), (key, count) -> count == null ? 1 : count + 1);
            }
        }
    }

    public IssueType classifyTicket(String description) {
        return analyze(description).issueType();
    }

    public TicketAnalysis analyze(String description) {
        List<String> tokens = tokenize(description);
        if (tokens.isEmpty()) {
            return new TicketAnalysis(IssueType.SOFTWARE, TicketPriority.LOW, 0.34d, suggestSolution(description));
        }

        int totalDocuments = documentCountByType.values().stream().mapToInt(Integer::intValue).sum();
        int vocabularySize = Math.max(vocabulary.size(), 1);
        Map<IssueType, Double> logScores = new EnumMap<>(IssueType.class);

        for (IssueType issueType : IssueType.values()) {
            int documentCount = documentCountByType.getOrDefault(issueType, 0);
            double logScore = Math.log((documentCount + 1.0d) / (totalDocuments + IssueType.values().length));
            Map<String, Integer> tokenCounts = tokenFrequencyByType.get(issueType);
            int totalTypeTokens = tokenCountByType.getOrDefault(issueType, 0);

            for (String token : tokens) {
                int frequency = tokenCounts.getOrDefault(token, 0);
                double likelihood = (frequency + 1.0d) / (totalTypeTokens + vocabularySize);
                logScore += Math.log(likelihood);
            }

            logScores.put(issueType, logScore);
        }

        IssueType predictedType = IssueType.SOFTWARE;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Map.Entry<IssueType, Double> entry : logScores.entrySet()) {
            if (entry.getValue() > bestScore) {
                bestScore = entry.getValue();
                predictedType = entry.getKey();
            }
        }

        double confidence = calculateConfidence(logScores, bestScore);
        TicketPriority priority = predictPriority(description);
        String suggestedSolution = suggestSolution(description);
        return new TicketAnalysis(predictedType, priority, confidence, suggestedSolution);
    }

    public TicketPriority predictPriority(String description) {
        String lower = description.toLowerCase(Locale.ENGLISH);
        if (containsAny(lower, "critical", "sev1", "security breach", "system outage", "production down")) {
            return TicketPriority.CRITICAL;
        }
        if (containsAny(lower, "urgent", "down", "asap", "immediately", "cannot work", "blocked")) {
            return TicketPriority.HIGH;
        }
        if (containsAny(lower, "slow", "issue", "problem", "error", "fail", "not working", "unable")) {
            return TicketPriority.MEDIUM;
        }
        return TicketPriority.LOW;
    }

    public String suggestSolution(String description) {
        String lower = description.toLowerCase(Locale.ENGLISH);
        if (lower.contains("password")) return "Try resetting your password from the login page. If the issue persists, contact your admin.";
        if (lower.contains("internet") || lower.contains("network")) return "Try restarting your router/modem. Check if other devices are affected.";
        if (lower.contains("printer")) return "Check printer connection and paper. Restart the print spooler service.";
        if (lower.contains("email")) return "Verify your email credentials and check internet connection. Try clearing browser cache.";
        if (lower.contains("vpn")) return "Ensure VPN client is up to date. Check your credentials and try reconnecting.";
        if (lower.contains("phishing") || lower.contains("security")) return "Do not share credentials. Isolate the affected device and contact the security team immediately.";
        if (lower.contains("payroll") || lower.contains("leave") || lower.contains("onboarding")) return "Verify your HR portal access and check whether the request requires HR approval.";
        if (lower.contains("slow")) return "Clear browser cache, close unused applications, and restart your computer.";
        if (lower.contains("screen") || lower.contains("monitor")) return "Check display cable connections. Try adjusting screen resolution settings.";
        return "Please provide more details about your issue. Our support team will assist you shortly.";
    }

    private boolean containsAny(String value, String... keywords) {
        return Arrays.stream(keywords).anyMatch(value::contains);
    }

    private List<String> tokenize(String text) {
        String normalized = text == null ? "" : text.toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9\\s]", " ");
        List<String> tokens = new ArrayList<>();
        for (String token : normalized.split("\\s+")) {
            if (token.length() > 2 && !STOP_WORDS.contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private double calculateConfidence(Map<IssueType, Double> logScores, double bestScore) {
        double denominator = 0.0d;
        for (double score : logScores.values()) {
            denominator += Math.exp(score - bestScore);
        }
        return Math.round((1.0d / denominator) * 1000.0d) / 1000.0d;
    }

    private record TrainingExample(IssueType issueType, String text) {}

    public record TicketAnalysis(
        IssueType issueType,
        TicketPriority priority,
        double confidence,
        String suggestedSolution
    ) {}
}
