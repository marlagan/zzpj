import com.zzpj.purrsuit.notificationservice.dto.UserProfileEvent;
import com.zzpj.purrsuit.notificationservice.entity.UserProfile;
import com.zzpj.purrsuit.notificationservice.repository.UserProfileRepository;
import com.zzpj.purrsuit.notificationservice.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // 💡 FIX: This tells JUnit 5 to process Mockito annotations
public class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    void save_ShouldMapEventToEntityAndSaveInRepository() {
        // Given
        UUID profileId = UUID.randomUUID();
        UserProfileEvent event = new UserProfileEvent(
                profileId,
                "test@purrsuit.com",
                "Jan",
                "Kowalski"
        );

        // When
        userProfileService.save(event);

        // Then
        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository, times(1)).save(profileCaptor.capture());

        UserProfile savedProfile = profileCaptor.getValue();
        assertEquals(profileId, savedProfile.getUserId());
        assertEquals("test@purrsuit.com", savedProfile.getEmail());
        assertEquals("Jan", savedProfile.getFirstName());
        assertEquals("Kowalski", savedProfile.getLastName());
    }

    @Test
    void get_ShouldReturnProfile_WhenProfileExists() {
        // Given
        UUID profileId = UUID.randomUUID();
        UserProfile profile = UserProfile.builder()
                .userId(profileId)
                .email("test@purrsuit.com")
                .firstName("Jan")
                .lastName("Kowalski")
                .build();

        when(userProfileRepository.findById(profileId)).thenReturn(Optional.of(profile));

        // When
        Optional<UserProfile> result = userProfileService.get(profileId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(profileId, result.get().getUserId());
        assertEquals("test@purrsuit.com", result.get().getEmail());
        verify(userProfileRepository, times(1)).findById(profileId);
    }

    @Test
    void get_ShouldReturnEmptyOptional_WhenProfileDoesNotExist() {
        // Given
        UUID profileId = UUID.randomUUID();
        when(userProfileRepository.findById(profileId)).thenReturn(Optional.empty());

        // When
        Optional<UserProfile> result = userProfileService.get(profileId);

        // Then
        assertTrue(result.isEmpty());
        verify(userProfileRepository, times(1)).findById(profileId);
    }
}