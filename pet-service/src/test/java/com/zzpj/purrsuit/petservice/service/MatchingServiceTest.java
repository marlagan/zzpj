package com.zzpj.purrsuit.petservice.service;

import com.zzpj.purrsuit.common.events.NoticeCreatedEvent;
import com.zzpj.purrsuit.petservice.entity.PetNotice;
import com.zzpj.purrsuit.petservice.enums.MatchStatus;
import com.zzpj.purrsuit.petservice.kafka.MatchResultProducer;
import com.zzpj.purrsuit.petservice.model.MatchResult;
import com.zzpj.purrsuit.petservice.repository.MatchResultRepository;
import com.zzpj.purrsuit.petservice.repository.PetNoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
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
    private PetNoticeRepository petNoticeRepository;

    @Mock
    private SemanticMatchService semanticMatchService;

    @Mock
    private MatchResultRepository matchResultRepository;

    @Mock
    private MatchResultProducer matchResultProducer;

    @InjectMocks
    private MatchingService matchingService;

    private final UUID newNoticeId = UUID.randomUUID();
    private final UUID candidateId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(matchingService, "threshold", 0.75);
        when(matchResultRepository.save(any(MatchResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void handleIncomingNotice_ShouldSaveLocallyAndFindMatch_WhenScoreIsAboveThreshold() {
        NoticeCreatedEvent event = new NoticeCreatedEvent(
                newNoticeId, "Dog", "Czarny mops", "LOST"
        );

        PetNotice candidate = PetNotice.builder()
                .noticeId(candidateId)
                .type("FOUND")
                .species("Dog")
                .description("Czarny pies mops")
                .build();

        when(petNoticeRepository.findByTypeAndSpecies("FOUND", "Dog"))
                .thenReturn(List.of(candidate));

        when(semanticMatchService.comparePetDescription("Czarny mops", "Czarny pies mops"))
                .thenReturn(0.85);


        matchingService.handleIncomingNotice(event);

        ArgumentCaptor<PetNotice> noticeCaptor = ArgumentCaptor.forClass(PetNotice.class);
        verify(petNoticeRepository, times(1)).save(noticeCaptor.capture());
        assertEquals(newNoticeId, noticeCaptor.getValue().getNoticeId());
        assertEquals("LOST", noticeCaptor.getValue().getType());

        ArgumentCaptor<MatchResult> resultCaptor = ArgumentCaptor.forClass(MatchResult.class);
        verify(matchResultRepository, times(1)).save(resultCaptor.capture());
        MatchResult savedResult = resultCaptor.getValue();
        assertEquals(newNoticeId, savedResult.getLostNoticeId());
        assertEquals(candidateId, savedResult.getSeenNoticeId());
        assertEquals(0.85, savedResult.getSimilarityScore());
        assertEquals(MatchStatus.PENDING, savedResult.getStatus());

        verify(matchResultProducer, times(1)).sendMatchNotification(any(MatchResult.class));
    }

    @Test
    void handleIncomingNotice_ShouldNotPublishMatch_WhenScoreIsBelowThreshold() {
        NoticeCreatedEvent event = new NoticeCreatedEvent(
                newNoticeId, "Dog", "Czarny mops", "LOST"
        );

        PetNotice candidate = PetNotice.builder()
                .noticeId(candidateId)
                .type("FOUND")
                .species("Dog")
                .description("Biały owczarek")
                .build();

        when(petNoticeRepository.findByTypeAndSpecies("FOUND", "Dog"))
                .thenReturn(List.of(candidate));

        when(semanticMatchService.comparePetDescription("Czarny mops", "Biały owczarek"))
                .thenReturn(0.40); // Score poniżej 0.75

        matchingService.handleIncomingNotice(event);

        verify(petNoticeRepository, times(1)).save(any(PetNotice.class));

        verify(matchResultRepository, never()).save(any(MatchResult.class));

        verify(matchResultProducer, never()).sendMatchNotification(any(MatchResult.class));
    }

    @Test
    void handleIncomingNotice_ShouldStopProcessing_WhenNoCandidatesFound() {
        NoticeCreatedEvent event = new NoticeCreatedEvent(
                newNoticeId, "Cat", "Biały kot", "FOUND"
        );

        when(petNoticeRepository.findByTypeAndSpecies("LOST", "Cat"))
                .thenReturn(Collections.emptyList());

        matchingService.handleIncomingNotice(event);

        verify(petNoticeRepository, times(1)).save(any(PetNotice.class));

        verify(semanticMatchService, never()).comparePetDescription(anyString(), anyString());

        verify(matchResultRepository, never()).save(any(MatchResult.class));
        verify(matchResultProducer, never()).sendMatchNotification(any(MatchResult.class));
    }

    @Test
    void getMatchesForNotice_ShouldReturnResultsFromRepository() {
        List<MatchResult> expectedResults = List.of(new MatchResult());
        when(matchResultRepository.findByLostNoticeId(newNoticeId)).thenReturn(expectedResults);

        List<MatchResult> actualResults = matchingService.getMatchesForNotice(newNoticeId);

        assertEquals(expectedResults, actualResults);
    }

    @Test
    void getMatchDetail_ShouldReturnOptionalResultFromRepository() {
        MatchResult matchResult = new MatchResult();
        when(matchResultRepository.findByLostNoticeIdAndSeenNoticeId(newNoticeId, candidateId))
                .thenReturn(Optional.of(matchResult));

        Optional<MatchResult> actualResult = matchingService.getMatchDetail(newNoticeId, candidateId);

        assertTrue(actualResult.isPresent());
        assertEquals(matchResult, actualResult.get());
    }
}