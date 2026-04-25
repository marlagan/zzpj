package com.zzpj.purrsuit.perservice.service;

import com.zzpj.purrsuit.perservice.client.NoticeServiceClient;
import com.zzpj.purrsuit.perservice.client.NotificationServiceClient;
import com.zzpj.purrsuit.perservice.dto.NoticeDto;
import com.zzpj.purrsuit.perservice.model.MatchResult;
import com.zzpj.purrsuit.perservice.enums.MatchStatus;
import com.zzpj.purrsuit.perservice.repository.MatchResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {
    private final NoticeServiceClient noticeServiceClient;
    private final SemanticMatchService semanticMatchService;
    private final MatchResultRepository matchResultRepository;
    private final NotificationServiceClient notificationServiceClient;

    @Value("${matching.similarity.threshold:0.75}")
    private double threshold;

    public List<MatchResult> findMatches(UUID noticeId){
        var notice = noticeServiceClient.getNotice(noticeId);
        var oppositeType = notice.type().equals("LOST") ? "SEEN" : "LOST";
        var candidates = noticeServiceClient.getConfirmedNoticesByType(oppositeType);

        return candidates.stream()
                .filter(c ->c.species().equals(notice.species()))
                .map(candidate -> scoreCandidate(notice,candidate))
                .filter(result -> result.getSimilarityScore() >= threshold)
                .peek(notificationServiceClient::sendMatchNotification)
                .toList();
    }

    private MatchResult scoreCandidate(NoticeDto query, NoticeDto candidate){
        double score = semanticMatchService.comparePetDescription(query.description(), candidate.description());

        log.info("Similarity {} <-> {}: {}", query.id(), candidate.id(), score);

        var result = MatchResult.builder()
                .lostNoticeId(query.type().equals("LOST") ? query.id() : candidate.id())
                .seenNoticeId(query.type().equals("SEEN") ? query.id() : candidate.id())
                .similarityScore(score)
                .status(MatchStatus.PENDING)
                .build();
        return matchResultRepository.save(result);
    }

    public List<MatchResult> getMatchesForNotice(UUID noticeID){
        return matchResultRepository.findByLostNotice(noticeID);
    }



}
