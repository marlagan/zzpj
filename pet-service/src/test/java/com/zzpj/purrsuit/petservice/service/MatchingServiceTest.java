package com.zzpj.purrsuit.petservice.service;

import com.zzpj.purrsuit.common.events.NoticeCreatedEvent;
import com.zzpj.purrsuit.common.events.NoticeStatus;
import com.zzpj.purrsuit.common.events.NoticeStatusUpdateEvent;
import com.zzpj.purrsuit.petservice.entity.PetNotice;
import com.zzpj.purrsuit.petservice.enums.MatchStatus;
import com.zzpj.purrsuit.petservice.kafka.MatchFoundNoticeProducer;
import com.zzpj.purrsuit.petservice.kafka.MatchResultProducer;
import com.zzpj.purrsuit.petservice.entity.MatchResult;
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

    @Mock
    private MatchFoundNoticeProducer matchFoundNoticeProducer;

    private final UUID newNoticeId = UUID.randomUUID();
    private final UUID candidateId = UUID.randomUUID();
    private final UUID userID = UUID.randomUUID();


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(matchingService, "threshold", 0.75);
        when(matchResultRepository.save(any(MatchResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void handleIncomingNotice_ShouldSaveLocallyAndFindMatch_WhenScoreIsAboveThreshold() {
        NoticeCreatedEvent event = new NoticeCreatedEvent(
                newNoticeId, userID,"Dog", "Czarny mops", "LOST", NoticeStatus.ACTIVE
        );

        PetNotice candidate = PetNotice.builder()
                .noticeId(candidateId)
                .userId(userID)
                .type("FOUND")
                .species("Dog")
                .description("Czarny pies mops")
                .status("ACTIVE")
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
    void handleIncomingNotice_shouldSaveNoticeAndCreateMatch_whenScoreIsAboveThreshold() {

        UUID noticeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        NoticeCreatedEvent event = new NoticeCreatedEvent(noticeId, userId, "Pies", "Czarny labrador", "LOST", NoticeStatus.ACTIVE);

        UUID candidateId = UUID.randomUUID();
        PetNotice activeCandidate = PetNotice.builder()
                .noticeId(candidateId)
                .type("FOUND")
                .species("Pies")
                .description("Znaleziono czarnego labradora")
                .status("ACTIVE")
                .build();

        when(petNoticeRepository.findByTypeAndSpecies("FOUND", "Pies")).thenReturn(List.of(activeCandidate));
        when(semanticMatchService.comparePetDescription(any(), any())).thenReturn(0.85); // 0.85 >= 0.75
        when(matchResultRepository.save(any(MatchResult.class))).thenAnswer(invocation -> invocation.getArgument(0));

        matchingService.handleIncomingNotice(event);

        verify(petNoticeRepository).save(argThat(notice ->
                notice.getNoticeId().equals(noticeId) && "ACTIVE".equals(notice.getStatus())
        ));
        verify(matchResultRepository).save(any(MatchResult.class));
        verify(matchResultProducer).sendMatchNotification(any(MatchResult.class));
        verify(matchFoundNoticeProducer).sendFoundMatchNotice(any(MatchResult.class));
    }

    @Test
    void handleIncomingNotice_ShouldNotPublishMatch_WhenScoreIsBelowThreshold() {
        NoticeCreatedEvent event = new NoticeCreatedEvent(
                newNoticeId, userID, "Dog", "Czarny mops", "LOST", NoticeStatus.ACTIVE
        );

        PetNotice candidate = PetNotice.builder()
                .noticeId(candidateId)
                .userId(userID)
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
                newNoticeId, userID, "Cat", "Biały kot", "FOUND", NoticeStatus.ACTIVE
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

    @Test
    void updateNoticeStatus_shouldUpdateStatus_whenNoticeExists() {
        // given
        UUID noticeId = UUID.randomUUID();
        NoticeStatusUpdateEvent event = new NoticeStatusUpdateEvent(noticeId, "RESOLVED");
        PetNotice existingNotice = PetNotice.builder()
                .noticeId(noticeId)
                .status("ACTIVE")
                .build();

        when(petNoticeRepository.findById(noticeId)).thenReturn(Optional.of(existingNotice));

        matchingService.updateNoticeStatus(event);

        assertEquals("RESOLVED", existingNotice.getStatus());
        verify(petNoticeRepository).save(existingNotice);
    }

    @Test
    void processLocationMatchEvent_shouldCreateMatch_whenValidCandidateAndNoDuplicateExists() {
        UUID sourceId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();
        List<UUID> nearbyIds = List.of(candidateId);

        PetNotice sourceNotice = PetNotice.builder()
                .noticeId(sourceId)
                .type("LOST")
                .species("Pies")
                .status("ACTIVE")
                .description("Zgubiono mopsa")
                .build();

        PetNotice candidateNotice = PetNotice.builder()
                .noticeId(candidateId)
                .type("FOUND")
                .species("Pies")
                .status("PENDING")
                .description("Znaleziono małego mopsa")
                .build();

        when(petNoticeRepository.findById(sourceId)).thenReturn(Optional.of(sourceNotice));
        when(petNoticeRepository.findAllById(nearbyIds)).thenReturn(List.of(candidateNotice));
        when(matchResultRepository.findByLostNoticeIdAndSeenNoticeId(sourceId, candidateId)).thenReturn(Optional.empty());
        when(semanticMatchService.comparePetDescription(any(), any())).thenReturn(0.95);
        when(matchResultRepository.save(any(MatchResult.class))).thenAnswer(invocation -> invocation.getArgument(0));

        matchingService.processLocationMatchEvent(sourceId, nearbyIds);

        verify(matchResultProducer).sendMatchNotification(any(MatchResult.class));
    }

    @Test
    void processLocationMatchEvent_shouldSkip_whenMatchAlreadyExistsInDatabase() {
        UUID sourceId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();
        List<UUID> nearbyIds = List.of(candidateId);

        PetNotice sourceNotice = PetNotice.builder()
                .noticeId(sourceId)
                .type("LOST")
                .species("Pies")
                .status("ACTIVE")
                .build();

        PetNotice candidateNotice = PetNotice.builder()
                .noticeId(candidateId)
                .type("FOUND")
                .species("Pies")
                .status("ACTIVE")
                .build();

        when(petNoticeRepository.findById(sourceId)).thenReturn(Optional.of(sourceNotice));
        when(petNoticeRepository.findAllById(nearbyIds)).thenReturn(List.of(candidateNotice));

        when(matchResultRepository.findByLostNoticeIdAndSeenNoticeId(sourceId, candidateId))
                .thenReturn(Optional.of(new MatchResult()));

        matchingService.processLocationMatchEvent(sourceId, nearbyIds);

        verifyNoInteractions(semanticMatchService);
        verify(matchResultRepository, never()).save(any());
        verifyNoInteractions(matchResultProducer);
    }


}