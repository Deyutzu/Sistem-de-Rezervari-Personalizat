package com.stayfinder.service;

import com.stayfinder.dto.UserPreferenceDto;
import com.stayfinder.exception.ResourceNotFoundException;
import com.stayfinder.model.AllInclusivePreference;
import com.stayfinder.model.User;
import com.stayfinder.model.UserPreference;
import com.stayfinder.repository.UserPreferenceRepository;
import com.stayfinder.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRepository userRepository;

    public UserPreferenceService(UserPreferenceRepository userPreferenceRepository, UserRepository userRepository) {
        this.userPreferenceRepository = userPreferenceRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserPreferenceDto getPreferences(Long userId) {
        return userPreferenceRepository.findByUserId(userId)
                .map(UserPreferenceDto::from)
                .orElseGet(() -> new UserPreferenceDto(java.util.Set.of(), null, null, AllInclusivePreference.ANY, null));
    }

    @Transactional
    public UserPreferenceDto saveOrUpdatePreferences(Long userId, UserPreferenceDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul cu id " + userId + " nu există"));

        UserPreference preference = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> new UserPreference(user));

        if (dto.preferredTypes() != null) {
            preference.setPreferredTypes(new HashSet<>(dto.preferredTypes()));
        }
        preference.setBudgetMin(dto.budgetMin());
        preference.setBudgetMax(dto.budgetMax());
        preference.setAllInclusivePreference(dto.allInclusivePreference() != null ? dto.allInclusivePreference() : AllInclusivePreference.ANY);
        preference.setTravelType(dto.travelType());

        UserPreference saved = userPreferenceRepository.save(preference);
        return UserPreferenceDto.from(saved);
    }
}
