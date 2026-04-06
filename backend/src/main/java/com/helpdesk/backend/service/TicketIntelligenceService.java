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
            training("cannot connect to company wifi or intranet after office move", IssueType.NETWORK, TicketPriority.HIGH),
            training("phishing email reported and suspicious login alert needs investigation", IssueType.SECURITY, TicketPriority.HIGH),
            training("account access blocked by security policy or mfa verification issue", IssueType.SECURITY, TicketPriority.HIGH),
            training("payroll portal not loading and salary slip unavailable in hr system", IssueType.HR, TicketPriority.MEDIUM),
            training("leave approval workflow failed during employee onboarding request", IssueType.HR, TicketPriority.LOW),
            training("desktop computer does not power on after electrical fluctuation", IssueType.HARDWARE, TicketPriority.HIGH),
            training("usb docking station stopped detecting monitor and keyboard", IssueType.HARDWARE, TicketPriority.MEDIUM),
            training("webcam is blurry and microphone is not detected during meetings", IssueType.HARDWARE, TicketPriority.MEDIUM),
            training("headset audio crackles and speakers stop working intermittently", IssueType.HARDWARE, TicketPriority.MEDIUM),
            training("laptop hinge is damaged and screen cannot stay upright", IssueType.HARDWARE, TicketPriority.HIGH),
            training("printer toner is leaking and printed pages are smudged", IssueType.HARDWARE, TicketPriority.MEDIUM),
            training("scanner glass is cracked and scans are unreadable", IssueType.HARDWARE, TicketPriority.HIGH),
            training("battery drains very fast and laptop shuts off unexpectedly", IssueType.HARDWARE, TicketPriority.HIGH),
            training("hard disk clicking noise and system fails to boot", IssueType.HARDWARE, TicketPriority.HIGH),
            training("keyboard keys stuck after spill and typing is impossible", IssueType.HARDWARE, TicketPriority.HIGH),

            training("erp application throws null pointer error after latest release", IssueType.SOFTWARE, TicketPriority.HIGH),
            training("crm page loads blank after login in browser", IssueType.SOFTWARE, TicketPriority.MEDIUM),
            training("excel macro stopped working after office update", IssueType.SOFTWARE, TicketPriority.MEDIUM),
            training("browser extension fails to save passwords on company portal", IssueType.SOFTWARE, TicketPriority.MEDIUM),
            training("invoice application freezes while generating reports", IssueType.SOFTWARE, TicketPriority.HIGH),
            training("single sign on redirect loops continuously and user cannot login", IssueType.SOFTWARE, TicketPriority.HIGH),
            training("database client installation request for new developer machine", IssueType.SOFTWARE, TicketPriority.LOW),
            training("teams desktop app crashes immediately after startup", IssueType.SOFTWARE, TicketPriority.HIGH),
            training("mobile app notifications stopped after recent update", IssueType.SOFTWARE, TicketPriority.MEDIUM),
            training("browser shows certificate error while opening internal application", IssueType.SOFTWARE, TicketPriority.MEDIUM),

            training("office wifi is connected but there is no internet access", IssueType.NETWORK, TicketPriority.HIGH),
            training("vpn tunnel connects but internal apps remain unreachable", IssueType.NETWORK, TicketPriority.HIGH),
            training("dns resolution fails for multiple internal hostnames", IssueType.NETWORK, TicketPriority.HIGH),
            training("router reboot caused complete network outage on floor two", IssueType.NETWORK, TicketPriority.CRITICAL),
            training("packet loss makes video meetings drop every few minutes", IssueType.NETWORK, TicketPriority.MEDIUM),
            training("ethernet port in conference room is not active", IssueType.NETWORK, TicketPriority.MEDIUM),
            training("branch office users cannot access shared drive over wan link", IssueType.NETWORK, TicketPriority.HIGH),
            training("firewall change blocked access to vendor website", IssueType.NETWORK, TicketPriority.MEDIUM),
            training("dns server latency causing slow intranet and login delays", IssueType.NETWORK, TicketPriority.MEDIUM),
            training("new employee laptop needs vpn profile and wireless certificate", IssueType.NETWORK, TicketPriority.LOW),

            training("employee received ransomware popup and suspicious file attachment", IssueType.SECURITY, TicketPriority.CRITICAL),
            training("multi factor authentication code not accepted for secure portal", IssueType.SECURITY, TicketPriority.HIGH),
            training("possible credential theft after fake login page reported", IssueType.SECURITY, TicketPriority.CRITICAL),
            training("endpoint antivirus detected trojan on finance workstation", IssueType.SECURITY, TicketPriority.CRITICAL),
            training("privileged account locked after repeated suspicious sign in attempts", IssueType.SECURITY, TicketPriority.HIGH),
            training("security awareness report about suspicious usb device found", IssueType.SECURITY, TicketPriority.MEDIUM),
            training("access request denied because compliance policy not updated", IssueType.SECURITY, TicketPriority.MEDIUM),
            training("unknown device joined network and triggered security alert", IssueType.SECURITY, TicketPriority.HIGH),
            training("password manager indicates account may be compromised", IssueType.SECURITY, TicketPriority.HIGH),
            training("employee clicked phishing link and needs immediate isolation help", IssueType.SECURITY, TicketPriority.CRITICAL),

            training("employee cannot submit leave request in hr portal", IssueType.HR, TicketPriority.MEDIUM),
            training("attendance regularization form is missing for this month", IssueType.HR, TicketPriority.LOW),
            training("new joiner onboarding checklist not assigned in hr system", IssueType.HR, TicketPriority.MEDIUM),
            training("salary slip download failed from payroll portal", IssueType.HR, TicketPriority.MEDIUM),
            training("benefits enrollment page shows incomplete employee details", IssueType.HR, TicketPriority.LOW),
            training("manager approval workflow for leave is stuck pending", IssueType.HR, TicketPriority.MEDIUM),
            training("employee id not generated after hr onboarding completion", IssueType.HR, TicketPriority.MEDIUM),
            training("reimbursement claim rejected because hr policy data is missing", IssueType.HR, TicketPriority.LOW),
            training("attendance device sync failed and work hours are incorrect", IssueType.HR, TicketPriority.MEDIUM),
            training("probation confirmation form unavailable in hr portal", IssueType.HR, TicketPriority.LOW)
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
        if (containsAny(tokens, "printer", "keyboard", "mouse", "monitor", "screen", "laptop", "desktop", "charger", "battery", "scanner", "display", "touchpad", "trackpad", "dock")) {
            return buildForcedDecision(IssueType.HARDWARE, containsAny(tokens, "broken", "damaged", "dead", "blank") ? TicketPriority.HIGH : TicketPriority.MEDIUM);
        }
        if (containsAny(tokens, "vpn", "router", "wifi", "network", "internet", "lan", "dns", "intranet", "connectivity", "bandwidth")) {
            return buildForcedDecision(IssueType.NETWORK, containsAny(tokens, "down", "disconnect", "unstable", "offline") ? TicketPriority.HIGH : TicketPriority.MEDIUM);
        }
        if (containsAny(tokens, "payroll", "leave", "attendance", "onboarding", "salary", "hr", "policy")) {
            return buildForcedDecision(IssueType.HR, TicketPriority.MEDIUM);
        }
        if (containsAny(tokens, "phishing", "mfa", "breach", "malware", "security", "fraud", "suspicious")) {
            return buildForcedDecision(IssueType.SECURITY, TicketPriority.HIGH);
        }
        if (containsAny(tokens, "password", "login", "outlook", "email", "software", "application", "install", "installation", "browser", "crash", "update")) {
            return buildForcedDecision(IssueType.SOFTWARE, containsAny(tokens, "blocked", "urgent", "crash", "failed") ? TicketPriority.HIGH : TicketPriority.MEDIUM);
        }
        if (containsAny(tokens, "touchpad", "keyboard", "cursor", "trackpad", "pad")) {
            return buildForcedDecision(IssueType.HARDWARE, TicketPriority.MEDIUM);
        }
        if (containsAll(tokens, "laptop", "pad")) {
            return buildForcedDecision(IssueType.HARDWARE, TicketPriority.MEDIUM);
        }
        if (containsAll(tokens, "cursor", "move")) {
            return buildForcedDecision(IssueType.HARDWARE, TicketPriority.MEDIUM);
        }
        if (containsAny(tokens, "slow", "hang", "lag", "freeze", "startup", "performance")
                && !containsAny(tokens, "touchpad", "keyboard", "screen", "battery", "charger", "printer", "mouse")) {
            return buildForcedDecision(IssueType.SOFTWARE, TicketPriority.MEDIUM);
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
                    case "printers" -> "printer";
                    case "monitors" -> "monitor";
                    case "emails" -> "email";
                    case "routers" -> "router";
                    case "salary" -> "payroll";
                    case "phishing", "breach", "malware" -> "security";
                    default -> token;
                })
                .toList();
    }

    private Team mapTeam(IssueType issueType) {
        return switch (issueType) {
            case HARDWARE -> Team.HARDWARE;
            case SOFTWARE -> Team.SOFTWARE;
            case NETWORK -> Team.NETWORK;
            case SECURITY -> Team.SECURITY;
            case HR -> Team.HR;
            default -> throw new IllegalStateException("Unexpected value: " + issueType);
        };
    }

    private IssueType inferFallbackIssueType(List<String> tokens) {
        if (containsAny(tokens, "security", "phishing", "mfa", "access", "breach", "fraud", "malware")) {
            return IssueType.SECURITY;
        }
        if (containsAny(tokens, "payroll", "leave", "attendance", "onboarding", "salary", "hr")) {
            return IssueType.HR;
        }
        if (containsAny(tokens, "network", "vpn", "router", "lan")) {
            return IssueType.NETWORK;
        }
        if (containsAny(tokens, "printer", "screen", "keyboard", "mouse", "charger", "hardware", "touchpad", "battery")) {
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
                        documentFrequency.merge(token, 1, (oldValue, newValue) -> oldValue == null ? newValue : oldValue + newValue);
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
                    termFrequency.merge(index, 1, (oldValue, newValue) -> oldValue == null ? newValue : oldValue + newValue);
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
