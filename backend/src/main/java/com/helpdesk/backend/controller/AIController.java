package com.helpdesk.backend.controller;

import com.helpdesk.backend.model.IssueType;
import com.helpdesk.backend.model.TicketPriority;
import com.helpdesk.backend.repository.TicketRepository;
import com.helpdesk.backend.service.AIService;
import com.helpdesk.backend.service.TicketIntelligenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;
    private final TicketIntelligenceService ticketIntelligenceService;
    private final TicketRepository ticketRepository;

    @PostMapping("/analyze")
    public AnalyzeResponse analyze(@RequestBody Map<String, String> body) {
        String description = body.getOrDefault("description", "");
        TicketIntelligenceService.TicketDecision analysis = ticketIntelligenceService.analyze(description);
        IssueType category = analysis.issueType();
        TicketPriority priority = analysis.priority();
        String suggestion = aiService.suggestSolution(description);
        List<DuplicateTicket> duplicates = findDuplicates(description, category);

        return new AnalyzeResponse(category, priority, analysis.classificationConfidence(), suggestion, duplicates);
    }

    private List<DuplicateTicket> findDuplicates(String description, IssueType category) {
        String[] keywords = description.toLowerCase().split("\\s+");
        return ticketRepository.findAll().stream()
                .filter(t -> t.getIssueType() == category)
                .filter(t -> {
                    String ticketDesc = t.getDescription().toLowerCase();
                    int matches = 0;
                    for (String keyword : keywords) {
                        if (keyword.length() > 4 && ticketDesc.contains(keyword)) matches++;
                    }
                    return matches >= 2;
                })
                .limit(3)
                .map(t -> new DuplicateTicket(t.getId(), t.getDescription(), t.getStatus().toString()))
                .collect(Collectors.toList());
    }

    record AnalyzeResponse(
        IssueType suggestedCategory,
        TicketPriority suggestedPriority,
        double confidence,
        String suggestedSolution,
        List<DuplicateTicket> possibleDuplicates
    ) {}

    record DuplicateTicket(Long id, String description, String status) {}
}
