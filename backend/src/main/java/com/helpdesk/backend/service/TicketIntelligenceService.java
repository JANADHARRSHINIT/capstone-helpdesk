package com.helpdesk.backend.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import com.helpdesk.backend.model.IssueType;
import com.helpdesk.backend.model.Role;
import com.helpdesk.backend.model.Team;
import com.helpdesk.backend.model.TicketEntity;
import com.helpdesk.backend.model.TicketPriority;
import com.helpdesk.backend.model.TicketStatus;
import com.helpdesk.backend.model.UserEntity;
import com.helpdesk.backend.repository.TicketRepository;
import com.helpdesk.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketIntelligenceService {

    private static final int SVM_EPOCHS = 80;
    private static final double LEARNING_RATE = 0.08;
    private static final double REGULARIZATION = 0.0005;
    private static final List<TicketStatus> ACTIVE_STATUSES = List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS);

    private static final List<TrainingExample> BOOTSTRAP_DATA = List.of(
            training("email authentication failed and user cannot sign in to outlook mailbox", IssueType.SOFTWARE, TicketPriority.HIGH),
            training("vpn disconnects and wifi drops every few minutes during remote work", IssueType.NETWORK, TicketPriority.HIGH),
            training("printer not printing documents and showing paper jam warning", IssueType.HARDWARE, TicketPriority.MEDIUM),
            training("laptop screen flickers and external monitor is blank", IssueType.HARDWARE, TicketPriority.HIGH),
            training("need software installation for office suite and developer tools", IssueType.SOFTWARE, TicketPriority.MEDIUM),
            training("system update failed during software update installation", IssueType.SOFTWARE, TicketPriority.MEDIUM),
            training("windows update stuck and application update cannot be completed", IssueType.SOFTWARE, TicketPriority.MEDIUM),
            training("internet is slow on office network and teams calls are unstable", IssueType.NETWORK, TicketPriority.MEDIUM),
            training("mouse stopped working and keyboard keys are unresponsive", IssueType.HARDWARE, TicketPriority.MEDIUM),
            training("touchpad is not working and laptop cursor does not move", IssueType.HARDWARE, TicketPriority.MEDIUM),
            training("trackpad click is stuck and touchpad gestures stopped responding", IssueType.HARDWARE, TicketPriority.MEDIUM),
            training("keyboard not typing and some keys are broken", IssueType.HARDWARE, TicketPriority.MEDIUM),
            training("password reset request and account access blocked in application", IssueType.SOFTWARE, TicketPriority.HIGH),
            training("vpn access request for new employee onboarding", IssueType.NETWORK, TicketPriority.LOW),
            training("replace damaged docking station and charger", IssueType.HARDWARE, TicketPriority.LOW),
            training("software bug causes repeated crash but workaround exists", IssueType.SOFTWARE, TicketPriority.MEDIUM),
            training("shared drive unavailable and network printer offline", IssueType.NETWORK, TicketPriority.HIGH),
            training("request installation of browser extension and pdf tool", IssueType.SOFTWARE, TicketPriority.LOW),
            training("keyboard replacement request for broken hardware", IssueType.HARDWARE, TicketPriority.LOW),
            training("cannot connect to company wifi or intranet after office move", IssueType.NETWORK, TicketPriority.HIGH)
    );

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private volatile ModelSnapshot modelSnapshot;

    public TicketDecision analyze(String description) {
        String normalizedDescription = description == null ? "" : description;
        List<String> tokens = tokenize(normalizedDescription);
        if (tokens.isEmpty()) {
            tokens = List.of("general", "support");
        }

        TicketDecision forcedDecision = ruleBasedDecision(tokens);
        if (forcedDecision != null) {
            return forcedDecision;
        }

        ModelSnapshot snapshot = currentModelSnapshot();
        double[] features = snapshot.vectorizer().transform(tokens);
        Prediction<IssueType> issuePrediction = snapshot.issueClassifier().predict(features);
        Prediction<TicketPriority> priorityPrediction = snapshot.priorityClassifier().predict(features);
        IssueType predictedIssueType = issuePrediction.label() != null ? issuePrediction.label() : inferFallbackIssueType(tokens);
        TicketPriority predictedPriority = priorityPrediction.label() != null ? priorityPrediction.label() : TicketPriority.MEDIUM;
        Team routingTeam = mapTeam(predictedIssueType);
        UserEntity assignee = chooseAssignee(routingTeam);
        double confidence = Math.round(((issuePrediction.confidence() + priorityPrediction.confidence()) / 2.0) * 1000.0) / 1000.0;

        return new TicketDecision(
                predictedIssueType,
                predictedPriority,
                routingTeam,
                confidence,
                assignee
        );
    }

    private TicketDecision ruleBasedDecision(List<String> tokens) {
        if (containsAny(tokens, "touchpad", "keyboard", "cursor", "trackpad", "pad")) {
            return buildForcedDecision(IssueType.HARDWARE, TicketPriority.MEDIUM);
        }
        if (containsAll(tokens, "laptop", "pad")) {
            return buildForcedDecision(IssueType.HARDWARE, TicketPriority.MEDIUM);
        }
        if (containsAll(tokens, "cursor", "move")) {
            return buildForcedDecision(IssueType.HARDWARE, TicketPriority.MEDIUM);
        }
        if (containsAny(tokens, "update")) {
            return buildForcedDecision(IssueType.SOFTWARE, TicketPriority.MEDIUM);
        }
        return null;
    }

    private TicketDecision buildForcedDecision(IssueType issueType, TicketPriority priority) {
        Team routingTeam = mapTeam(issueType);
        UserEntity assignee = chooseAssignee(routingTeam);
        return new TicketDecision(issueType, priority, routingTeam, 0.99, assignee);
    }

    private synchronized ModelSnapshot currentModelSnapshot() {
        long ticketCount = ticketRepository.count();
        if (modelSnapshot != null && modelSnapshot.ticketCount() == ticketCount) {
            return modelSnapshot;
        }

        List<TrainingDocument> trainingDocuments = new ArrayList<>();
        BOOTSTRAP_DATA.forEach(example ->
                trainingDocuments.add(new TrainingDocument(tokenize(example.text()), example.issueType(), example.priority()))
        );

        ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getDescription() != null && !ticket.getDescription().isBlank())
                .filter(ticket -> ticket.getIssueType() != null && ticket.getPriority() != null)
                .forEach(ticket -> trainingDocuments.add(
                        new TrainingDocument(tokenize(ticket.getDescription()), ticket.getIssueType(), ticket.getPriority())
                ));

        TfIdfVectorizer vectorizer = TfIdfVectorizer.fit(trainingDocuments);
        List<double[]> vectors = trainingDocuments.stream()
                .map(document -> vectorizer.transform(document.tokens()))
                .toList();

        modelSnapshot = new ModelSnapshot(
                ticketCount,
                vectorizer,
                LinearSvmClassifier.train(IssueType.values(), trainingDocuments, vectors, TrainingDocument::issueType),
                LinearSvmClassifier.train(TicketPriority.values(), trainingDocuments, vectors, TrainingDocument::priority)
        );
        return modelSnapshot;
    }

    private UserEntity chooseAssignee(Team team) {
        List<UserEntity> candidates = userRepository.findByRoleAndTeam(Role.EMPLOYEE, team);
        if (candidates.isEmpty()) {
            candidates = userRepository.findByRole(Role.EMPLOYEE);
        }

        return candidates.stream()
                .min(Comparator
                        .comparingLong((UserEntity user) -> ticketRepository.countByAssignedEmployeeIdAndStatusIn(user.getId(), ACTIVE_STATUSES))
                        .thenComparing(UserEntity::getId))
                .orElse(null);
    }

    private List<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(token -> token.length() > 2)
                .map(token -> switch (token) {
                    case "touchpad", "trackpad" -> "touchpad";
                    case "wifi", "internet" -> "network";
                    case "keyboard", "keys" -> "keyboard";
                    case "update", "updates", "updating", "upgrade", "upgrades" -> "update";
                    default -> token;
                })
                .toList();
    }

    private Team mapTeam(IssueType issueType) {
        return switch (issueType) {
            case HARDWARE -> Team.HARDWARE;
            case NETWORK -> Team.NETWORK;
            case SOFTWARE -> Team.SOFTWARE;
        };
    }

    private IssueType inferFallbackIssueType(List<String> tokens) {
        if (containsAny(tokens, "network", "vpn", "router", "lan")) {
            return IssueType.NETWORK;
        }
        if (containsAny(tokens, "printer", "laptop", "screen", "keyboard", "mouse", "charger", "hardware", "touchpad")) {
            return IssueType.HARDWARE;
        }
        if (containsAny(tokens, "software", "application", "update", "install", "installation", "crash")) {
            return IssueType.SOFTWARE;
        }
        return IssueType.SOFTWARE;
    }

    private boolean containsAny(List<String> tokens, String... keywords) {
        for (String keyword : keywords) {
            if (tokens.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAll(List<String> tokens, String... keywords) {
        for (String keyword : keywords) {
            if (!tokens.contains(keyword)) {
                return false;
            }
        }
        return true;
    }

    private static TrainingExample training(String text, IssueType issueType, TicketPriority priority) {
        return new TrainingExample(text, issueType, priority);
    }

    private record TrainingExample(String text, IssueType issueType, TicketPriority priority) {}

    private record TrainingDocument(
            List<String> tokens,
            IssueType issueType,
            TicketPriority priority
    ) {}

    private record Prediction<L>(L label, double confidence) {}

    private record ModelSnapshot(
            long ticketCount,
            TfIdfVectorizer vectorizer,
            LinearSvmClassifier<IssueType> issueClassifier,
            LinearSvmClassifier<TicketPriority> priorityClassifier
    ) {}

    private static final class TfIdfVectorizer {
        private final Map<String, Integer> vocabulary;
        private final double[] inverseDocumentFrequency;

        private TfIdfVectorizer(Map<String, Integer> vocabulary, double[] inverseDocumentFrequency) {
            this.vocabulary = vocabulary;
            this.inverseDocumentFrequency = inverseDocumentFrequency;
        }

        static TfIdfVectorizer fit(List<TrainingDocument> documents) {
            Map<String, Integer> vocabulary = new HashMap<>();
            Map<String, Integer> documentFrequency = new HashMap<>();
            int nextIndex = 0;

            for (TrainingDocument document : documents) {
                Set<String> seen = new HashSet<>();
                for (String token : document.tokens()) {
                    if (!vocabulary.containsKey(token)) {
                        vocabulary.put(token, nextIndex++);
                    }
                    if (seen.add(token)) {
                        documentFrequency.merge(token, 1, Integer::sum);
                    }
                }
            }

            double[] idf = new double[vocabulary.size()];
            int documentCount = Math.max(documents.size(), 1);
            vocabulary.forEach((token, index) -> {
                int df = documentFrequency.getOrDefault(token, 0);
                idf[index] = Math.log((documentCount + 1.0) / (df + 1.0)) + 1.0;
            });

            return new TfIdfVectorizer(vocabulary, idf);
        }

        double[] transform(List<String> tokens) {
            double[] vector = new double[inverseDocumentFrequency.length];
            if (tokens.isEmpty()) {
                return vector;
            }

            Map<Integer, Integer> termFrequency = new HashMap<>();
            for (String token : tokens) {
                Integer index = vocabulary.get(token);
                if (index != null) {
                    termFrequency.merge(index, 1, Integer::sum);
                }
            }

            int totalTerms = Math.max(termFrequency.values().stream().mapToInt(Integer::intValue).sum(), 1);
            double norm = 0.0;
            for (Map.Entry<Integer, Integer> entry : termFrequency.entrySet()) {
                double tf = entry.getValue() / (double) totalTerms;
                double value = tf * inverseDocumentFrequency[entry.getKey()];
                vector[entry.getKey()] = value;
                norm += value * value;
            }

            if (norm > 0.0) {
                double magnitude = Math.sqrt(norm);
                for (int i = 0; i < vector.length; i++) {
                    vector[i] /= magnitude;
                }
            }
            return vector;
        }
    }

    private static final class LinearSvmClassifier<L extends Enum<L>> {
        private final L[] labels;
        private final double[][] weights;
        private final double[] biases;

        private LinearSvmClassifier(L[] labels, double[][] weights, double[] biases) {
            this.labels = labels;
            this.weights = weights;
            this.biases = biases;
        }

        static <L extends Enum<L>> LinearSvmClassifier<L> train(
                L[] labels,
                List<TrainingDocument> documents,
                List<double[]> vectors,
                java.util.function.Function<TrainingDocument, L> labelExtractor
        ) {
            int featureCount = vectors.isEmpty() ? 0 : vectors.get(0).length;
            double[][] weights = new double[labels.length][featureCount];
            double[] biases = new double[labels.length];

            for (int labelIndex = 0; labelIndex < labels.length; labelIndex++) {
                L label = labels[labelIndex];
                for (int epoch = 0; epoch < SVM_EPOCHS; epoch++) {
                    for (int docIndex = 0; docIndex < documents.size(); docIndex++) {
                        double[] features = vectors.get(docIndex);
                        double target = labelExtractor.apply(documents.get(docIndex)) == label ? 1.0 : -1.0;
                        double score = dot(weights[labelIndex], features) + biases[labelIndex];
                        double margin = target * score;
                        double shrink = 1.0 - (LEARNING_RATE * REGULARIZATION);

                        for (int featureIndex = 0; featureIndex < weights[labelIndex].length; featureIndex++) {
                            weights[labelIndex][featureIndex] *= shrink;
                        }

                        if (margin < 1.0) {
                            for (int featureIndex = 0; featureIndex < weights[labelIndex].length; featureIndex++) {
                                weights[labelIndex][featureIndex] += LEARNING_RATE * target * features[featureIndex];
                            }
                            biases[labelIndex] += LEARNING_RATE * target;
                        }
                    }
                }
            }

            return new LinearSvmClassifier<>(labels, weights, biases);
        }

        Prediction<L> predict(double[] features) {
            double[] scores = new double[labels.length];
            int bestIndex = 0;
            double bestScore = Double.NEGATIVE_INFINITY;

            for (int i = 0; i < labels.length; i++) {
                scores[i] = dot(weights[i], features) + biases[i];
                if (scores[i] > bestScore) {
                    bestScore = scores[i];
                    bestIndex = i;
                }
            }

            double denominator = 0.0;
            for (double score : scores) {
                denominator += Math.exp(score - bestScore);
            }
            double confidence = denominator == 0.0 ? 1.0 : 1.0 / denominator;
            return new Prediction<>(labels[bestIndex], confidence);
        }

        private static double dot(double[] weights, double[] features) {
            double score = 0.0;
            for (int i = 0; i < weights.length; i++) {
                score += weights[i] * features[i];
            }
            return score;
        }
    }

    public record TicketDecision(
            IssueType issueType,
            TicketPriority priority,
            Team routingTeam,
            Double classificationConfidence,
            UserEntity assignedEmployee
    ) {}
}
