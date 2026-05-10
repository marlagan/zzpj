package com.zzpj.purrsuit.noticeservice.service;

import com.zzpj.purrsuit.noticeservice.data.NoticeCreateRequest;
import com.zzpj.purrsuit.noticeservice.entity.Notice;
import com.zzpj.purrsuit.noticeservice.enums.NoticeStatus;
import com.zzpj.purrsuit.noticeservice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final AiGenerationService aiGenerationService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public Notice createPendingNotice(NoticeCreateRequest request) {
        String generatedDescription = aiGenerationService.generateNoticeDescription(
                request.getTraits(), request.getLocation(), request.getName()
        );

        Notice notice = new Notice();
        notice.setPetId(request.getPetId());
        notice.setType(request.getType());
        notice.setPhotoUrls(request.getPhotoUrls());
        notice.setGeneratedText(generatedDescription);
        notice.setStatus(NoticeStatus.PENDING_APPROVAL); // Logika Human-in-the-loop
        notice.setCreatedAt(LocalDateTime.now());

        return noticeRepository.save(notice);
    }

    @Transactional
    public Notice publishNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found"));

        if (notice.getStatus() == NoticeStatus.PUBLISHED) {
            return notice; // Już opublikowane
        }

        notice.setStatus(NoticeStatus.PUBLISHED);
        noticeRepository.save(notice);

        // Wysyłanie eventu NoticeCreatedEvent do Kafki dla innych serwisów
        // Najlepiej byłoby stworzyć klasę DTO i serializować ją do JSON, dla uproszczenia wysyłamy String
        String eventPayload = String.format("{\"noticeId\": %d, \"petId\": %d, \"type\": \"%s\"}",
                notice.getId(), notice.getPetId(), notice.getType().name());

        kafkaTemplate.send("notice-events-topic", eventPayload);

        return notice;
    }

    public List<Notice> getNoticesByPetId(Long petId) {
        return noticeRepository.findByPetId(petId);
    }
}