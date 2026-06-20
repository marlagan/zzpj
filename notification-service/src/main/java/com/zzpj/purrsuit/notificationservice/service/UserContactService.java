package com.zzpj.purrsuit.notificationservice.service;

import com.zzpj.purrsuit.common.events.UserProfileEvent;
import com.zzpj.purrsuit.notificationservice.entity.UserContact;
import com.zzpj.purrsuit.notificationservice.repository.UserContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserContactService {

    private final UserContactRepository userContactRepository;

    @Transactional
    public UserContact saveOrUpdate(UserProfileEvent event) {
        UserContact contact = userContactRepository.findById(event.userId())
                .orElse(UserContact.builder().userId(event.userId()).build());

        contact.setEmail(event.email());
        contact.setFirstName(event.firstName());
        contact.setLastName(event.lastName());

        return userContactRepository.save(contact);
    }

    @Transactional(readOnly = true)
    public UserContact getByUserId(UUID userId) {
        return userContactRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Brak danych kontaktowych dla userId=" + userId
                                + " — użytkownik musi się zalogować (sync-profile)"));
    }
}
