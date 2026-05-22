package com.zzpj.purrsuit.petservice.service;

import com.zzpj.purrsuit.petservice.client.NoticeServiceClient;
import com.zzpj.purrsuit.petservice.dto.NoticeDto;
import com.zzpj.purrsuit.petservice.enums.MatchStatus;
import com.zzpj.purrsuit.petservice.kafka.MatchResultProducer;
import com.zzpj.purrsuit.petservice.model.MatchResult;
import com.zzpj.purrsuit.petservice.repository.MatchResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MatchingServiceTest {

    @Mock
    private NoticeServiceClient noticeServiceClient;

    @Mock
    private SemanticMatchService semanticMatchService;

    @Mock
    private MatchResultRepository matchResultRepository;

    @Mock
    private MatchResultProducer matchResultProducer;

    @InjectMocks
    private MatchingService matchingService;

    private final UUID lostNoticeId = UUID.randomUUID();
    private final UUID seenNoticeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(matchingService, "threshold", 0.75);
        when(matchResultRepository.save(any(MatchResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void findMatches_ShouldReturnMatches_WhenScoreIsAboveThreshold() {
        NoticeDto lostNotice = mock(NoticeDto.class);
        when(lostNotice.id()).thenReturn(lostNoticeId);
        when(lostNotice.type()).thenReturn("LOST");
        when(lostNotice.species()).thenReturn("Dog");
        when(lostNotice.description()).thenReturn("Czarny mops");

        NoticeDto seenNotice = mock(NoticeDto.class);
        when(seenNotice.id()).thenReturn(seenNoticeId);
        when(seenNotice.type()).thenReturn("SEEN");
        when(seenNotice.species()).thenReturn("Dog");
        when(seenNotice.description()).thenReturn("Czarny pies mops");

        when(noticeServiceClient.getNotice(lostNoticeId)).thenReturn(lostNotice);
        when(noticeServiceClient.getConfirmedNoticesByType("SEEN")).thenReturn(List.of(seenNotice));
        when(semanticMatchService.comparePetDescription("Czarny mops", "Czarny pies mops")).thenReturn(0.85);

        List<MatchResult> results = matchingService.findMatches(lostNoticeId);

        assertEquals(1, results.size());
        MatchResult result = results.get(0);
        assertEquals(0.85, result.getSimilarityScore());

        verify(matchResultRepository, atLeastOnce()).save(any(MatchResult.class));
        verify(matchResultProducer, times(1)).sendMatchNotification(any(MatchResult.class));
    }

    @Test
    void findMatches_ShouldNotReturnMatches_WhenScoreIsBelowThreshold() {

        NoticeDto lostNotice = mock(NoticeDto.class);
        when(lostNotice.id()).thenReturn(lostNoticeId);
        when(lostNotice.type()).thenReturn("LOST");
        when(lostNotice.species()).thenReturn("Dog");
        when(lostNotice.description()).thenReturn("Czarny mops");

        NoticeDto seenNotice = mock(NoticeDto.class);
        when(seenNotice.id()).thenReturn(seenNoticeId);
        when(seenNotice.type()).thenReturn("SEEN");
        when(seenNotice.species()).thenReturn("Dog");
        when(seenNotice.description()).thenReturn("Biały kot");

        when(noticeServiceClient.getNotice(lostNoticeId)).thenReturn(lostNotice);
        when(noticeServiceClient.getConfirmedNoticesByType("SEEN")).thenReturn(List.of(seenNotice));
        when(semanticMatchService.comparePetDescription("Czarny mops", "Biały kot")).thenReturn(0.40);

        List<MatchResult> results = matchingService.findMatches(lostNoticeId);

        assertTrue(results.isEmpty());
        verify(matchResultRepository, atLeastOnce()).save(any(MatchResult.class));
        verify(matchResultProducer, never()).sendMatchNotification(any(MatchResult.class));
    }

    @Test
    void findMatches_ShouldIgnoreCandidatesWithDifferentSpecies() {
        NoticeDto lostNotice = mock(NoticeDto.class);
        when(lostNotice.id()).thenReturn(lostNoticeId);
        when(lostNotice.type()).thenReturn("LOST");
        when(lostNotice.species()).thenReturn("Dog");

        NoticeDto seenNotice = mock(NoticeDto.class);
        when(seenNotice.species()).thenReturn("Cat");

        when(noticeServiceClient.getNotice(lostNoticeId)).thenReturn(lostNotice);
        when(noticeServiceClient.getConfirmedNoticesByType("SEEN")).thenReturn(List.of(seenNotice));

        List<MatchResult> results = matchingService.findMatches(lostNoticeId);

        assertTrue(results.isEmpty());
        verify(semanticMatchService, never()).comparePetDescription(anyString(), anyString());
    }

    @Test
    void processLocationMatchEvent_ShouldProcessCaseInsensitiveSpeciesAndNotifyIfAboveThreshold() {
        NoticeDto lostNotice = mock(NoticeDto.class);
        when(lostNotice.id()).thenReturn(lostNoticeId);
        when(lostNotice.type()).thenReturn("LOST"); // FIX: Zapobiega rzucaniu NullPointerException w scoreCandidate
        when(lostNotice.species()).thenReturn("DOG");
        when(lostNotice.description()).thenReturn("Czarny mops");

        NoticeDto seenNotice = mock(NoticeDto.class);
        when(seenNotice.id()).thenReturn(seenNoticeId);
        when(seenNotice.type()).thenReturn("SEEN");
        when(seenNotice.species()).thenReturn("dog");
        when(seenNotice.description()).thenReturn("Mops znaleziony");

        when(noticeServiceClient.getNotice(lostNoticeId)).thenReturn(lostNotice);
        when(semanticMatchService.comparePetDescription("Czarny mops", "Mops znaleziony")).thenReturn(0.90);

        matchingService.processLocationMatchEvent(lostNoticeId, List.of(seenNotice));

        verify(matchResultRepository, atLeastOnce()).save(any(MatchResult.class));
        verify(matchResultProducer, times(1)).sendMatchNotification(any(MatchResult.class));
    }

    @Test
    void getMatchesForNotice_ShouldReturnResultsFromRepository() {
        List<MatchResult> expectedResults = List.of(new MatchResult());
        when(matchResultRepository.findByLostNoticeId(lostNoticeId)).thenReturn(expectedResults);

        List<MatchResult> actualResults = matchingService.getMatchesForNotice(lostNoticeId);

        assertEquals(expectedResults, actualResults);
    }

    @Test
    void getMatchDetail_ShouldReturnOptionalResultFromRepository() {
        MatchResult matchResult = new MatchResult();
        when(matchResultRepository.findByLostNoticeIdAndSeenNoticeId(lostNoticeId, seenNoticeId))
                .thenReturn(Optional.of(matchResult));
        Optional<MatchResult> actualResult = matchingService.getMatchDetail(lostNoticeId, seenNoticeId);
        assertTrue(actualResult.isPresent());
        assertEquals(matchResult, actualResult.get());
    }
}