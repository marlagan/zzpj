package com.zzpj.purrsuit.noticeservice.service;

import com.zzpj.purrsuit.common.events.NoticeStatus;
import com.zzpj.purrsuit.noticeservice.domain.NoticeType;
import com.zzpj.purrsuit.noticeservice.dto.NoticeDto.CreateNoticeRequest;
import com.zzpj.purrsuit.noticeservice.dto.NoticeDto.NoticeResponse;
import com.zzpj.purrsuit.noticeservice.entity.Notice;
import com.zzpj.purrsuit.noticeservice.kafka.NoticeEventProducer;
import com.zzpj.purrsuit.noticeservice.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock private NoticeRepository noticeRepository;
    @Mock private AnimalVisionService visionService;
    @Mock private NoticeEventProducer eventProducer;

    @InjectMocks
    private NoticeService noticeService;

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    private CreateNoticeRequest request;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        request = new CreateNoticeRequest();
        request.type = NoticeType.LOST;
        request.species = "kot";
        request.breed = "europejski";
        request.colorDescription = "rudy";
        request.latitude = 51.75;
        request.longitude = 19.45;
        request.eventDate = LocalDateTime.now().minusHours(1);
    }

    private Notice savedNotice(UUID id, UUID owner) {
        return Notice.builder()
                .id(id)
                .type(NoticeType.LOST)
                .status(NoticeStatus.ACTIVE)
                .reportedByUserId(owner)
                .species("kot")
                .breed("europejski")
                .colorDescription("rudy")
                .location(GF.createPoint(new Coordinate(19.45, 51.75)))
                .eventDate(request.eventDate)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createNotice_usesUserIdFromParameter_notFromRequest() {
        UUID noticeId = UUID.randomUUID();
        when(noticeRepository.save(any())).thenReturn(savedNotice(noticeId, userId));
        when(visionService.generateDescription(any(), any(), any(), any())).thenReturn("opis AI");

        NoticeResponse response = noticeService.createNotice(request, userId);

        assertThat(response.getReportedByUserId()).isEqualTo(userId);
        assertThat(response.getId()).isEqualTo(noticeId);

        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
        verify(noticeRepository).save(captor.capture());
        assertThat(captor.getValue().getReportedByUserId()).isEqualTo(userId);
    }

    @Test
    void createNotice_publishesBothKafkaEvents() {
        UUID noticeId = UUID.randomUUID();
        when(noticeRepository.save(any())).thenReturn(savedNotice(noticeId, userId));
        when(visionService.generateDescription(any(), any(), any(), any())).thenReturn("opis AI");

        noticeService.createNotice(request, userId);

        verify(eventProducer).sendDescriptionEvent(
                eq(noticeId), eq(userId), eq("kot"), any(), eq(NoticeType.LOST), eq(NoticeStatus.ACTIVE));
        verify(eventProducer).sendLocationEvent(
                eq(noticeId), eq(NoticeType.LOST), any(), any(), eq(NoticeStatus.ACTIVE), eq("kot"));
    }

    @Test
    void createNotice_withNullAiDescription_stillPublishesEvents() {
        UUID noticeId = UUID.randomUUID();
        Notice n = savedNotice(noticeId, userId);
        n.setAiGeneratedDescription(null);
        when(noticeRepository.save(any())).thenReturn(n);
        when(visionService.generateDescription(any(), any(), any(), any())).thenReturn(null);

        noticeService.createNotice(request, userId);

        verify(eventProducer).sendDescriptionEvent(any(), any(), any(), any(), any(), any());
    }

    @Test
    void getById_returnsResponse_whenExists() {
        UUID id = UUID.randomUUID();
        when(noticeRepository.findById(id)).thenReturn(Optional.of(savedNotice(id, userId)));

        NoticeResponse response = noticeService.getById(id);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getStatus()).isEqualTo(NoticeStatus.ACTIVE);
    }

    @Test
    void getById_throwsNoSuchElementException_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(noticeRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.getById(id))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void getByUser_returnsMappedList() {
        UUID id = UUID.randomUUID();
        when(noticeRepository.findByReportedByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(savedNotice(id, userId)));

        List<NoticeResponse> result = noticeService.getByUser(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReportedByUserId()).isEqualTo(userId);
    }

    @Test
    void getByTypeAndStatus_returnsFilteredList() {
        UUID id = UUID.randomUUID();
        when(noticeRepository.findByTypeAndStatus(NoticeType.LOST, NoticeStatus.ACTIVE))
                .thenReturn(List.of(savedNotice(id, userId)));

        List<NoticeResponse> result = noticeService.getByTypeAndStatus(NoticeType.LOST, NoticeStatus.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(NoticeType.LOST);
    }

    @Test
    void updateStatus_changesStatusAndSaves() {
        UUID id = UUID.randomUUID();
        Notice notice = savedNotice(id, userId);
        when(noticeRepository.findById(id)).thenReturn(Optional.of(notice));
        when(noticeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        NoticeResponse response = noticeService.updateStatus(id, NoticeStatus.RESOLVED);

        assertThat(response.getStatus()).isEqualTo(NoticeStatus.RESOLVED);
        verify(noticeRepository).save(notice);
    }

    @Test
    void updateStatus_throwsNoSuchElementException_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(noticeRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.updateStatus(id, NoticeStatus.RESOLVED))
                .isInstanceOf(NoSuchElementException.class);
    }
}