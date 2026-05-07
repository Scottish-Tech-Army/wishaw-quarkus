package org.scottishtecharmy.wishaw.quarkus.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.scottishtecharmy.wishaw.quarkus.dto.BadgeDto;
import org.scottishtecharmy.wishaw.quarkus.model.BadgeCategory;
import org.scottishtecharmy.wishaw.quarkus.model.ChallengeSubmission;
import org.scottishtecharmy.wishaw.quarkus.model.LevelDefinition;
import org.scottishtecharmy.wishaw.quarkus.repository.BadgeCategoryRepository;
import org.scottishtecharmy.wishaw.quarkus.repository.ChallengeSubmissionRepository;
import org.scottishtecharmy.wishaw.quarkus.repository.LevelDefinitionRepository;

/**
 * Calculates XP as a derived value from approved submissions.
 * XP totals are deterministic — always computed, never cached.
 */
@ApplicationScoped
public class XpService {

    @Inject
    ChallengeSubmissionRepository submissionRepository;

    @Inject
    BadgeCategoryRepository badgeCategoryRepository;

    @Inject
    LevelDefinitionRepository levelDefinitionRepository;

    /**
     * Calculate badge progress for a user across all badge categories.
     * Level definitions are global (not per-category).
     */
    public List<BadgeDto> getBadgesForUser(UUID userId) {
        List<ChallengeSubmission> approved = submissionRepository.findApprovedByUserId(userId);

        // Sum XP per badge category
        Map<UUID, Integer> xpByCategory = new HashMap<>();
        for (ChallengeSubmission sub : approved) {
            UUID categoryId = sub.challenge.badgeCategory.id;
            xpByCategory.merge(categoryId, sub.challenge.xpValue, Integer::sum);
        }

        List<BadgeCategory> allCategories = badgeCategoryRepository.listAll();
        List<LevelDefinition> levels = levelDefinitionRepository.findAllOrderedByMinXp();
        List<BadgeDto> badges = new ArrayList<>();

        for (BadgeCategory category : allCategories) {
            int xp = xpByCategory.getOrDefault(category.id, 0);

            BadgeDto dto = new BadgeDto();
            dto.badgeCategory = category.displayName;
            dto.xp = xp;
            dto.level = resolveLevel(xp, levels);
            dto.nextLevelAtXp = resolveNextLevelMinXp(xp, levels);
            badges.add(dto);
        }

        return badges;
    }

    /**
     * Calculate total XP for a user (sum of all approved submission XP values).
     */
    public int getTotalXpForUser(UUID userId) {
        List<ChallengeSubmission> approved = submissionRepository.findApprovedByUserId(userId);
        int total = 0;
        for (ChallengeSubmission sub : approved) {
            total += sub.challenge.xpValue;
        }
        return total;
    }

    private String resolveLevel(int xp, List<LevelDefinition> levels) {
        for (LevelDefinition level : levels) {
            if (xp >= level.minXp && xp <= level.maxXp) {
                return level.name;
            }
        }
        if (!levels.isEmpty()) {
            return levels.get(levels.size() - 1).name;
        }
        return "Unranked";
    }

    private int resolveNextLevelMinXp(int xp, List<LevelDefinition> levels) {
        for (int i = 0; i < levels.size(); i++) {
            LevelDefinition level = levels.get(i);
            if (xp >= level.minXp && xp <= level.maxXp) {
                if (i + 1 < levels.size()) {
                    return levels.get(i + 1).minXp;
                }
                return -1;
            }
        }
        return -1;
    }
}
