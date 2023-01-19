package com.datn.application.service;


import com.datn.application.entity.User;
import com.datn.application.model.dto.UserDTO;
import com.datn.application.model.request.ChangePasswordRequest;
import com.datn.application.model.request.CreateUserRequest;
import com.datn.application.model.request.UpdateProfileRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    List<UserDTO> getListUsers();

    Page<User> adminListUserPages(String fullName, String phone, String email, Integer page);

    User createUser(CreateUserRequest createUserRequest);

    void changePassword(User user, ChangePasswordRequest changePasswordRequest);

    User updateProfile(User user, UpdateProfileRequest updateProfileRequest);
}
