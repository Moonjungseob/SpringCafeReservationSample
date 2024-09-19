package com.busanit501lsy.springcafereservationsample.controller.api;

import com.busanit501lsy.springcafereservationsample.dto.PredictionResponseDTO;
import com.busanit501lsy.springcafereservationsample.entity.User;
import com.busanit501lsy.springcafereservationsample.service.ImageUploadService;
import com.busanit501lsy.springcafereservationsample.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/public/users")
@Log4j2
public class UserRestPublicController {

    @Autowired
    private UserService userService;

    @Autowired
    PasswordEncoder bCryptPasswordEncoder;

    @Autowired
    ImageUploadService imageUploadService;

    // 파일 업로드 할 경우
    @PostMapping
    public ResponseEntity<User> createUser( @RequestPart("user") User user,
                                            @RequestParam(value = "profileImage", required = false) MultipartFile file) {
        try {
            log.info("image 확인 : " + file);
//            @RequestPart를 사용하여 멀티파트 요청의 user 부분을 User 객체로 자동 변환
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            // 사용자 정보 저장
            User createdUser = userService.createUser(user);

            // 파일이 존재할 경우 프로필 이미지 저장
            if (file !=null && !file.isEmpty()) {
                userService.saveProfileImage(createdUser.getId(), file);
            }

            return ResponseEntity.ok(createdUser);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save user or profile image", e);
        }
    }

    //아이디 중복 검사
    @PostMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        log.info("username: " + username);
        boolean isAvailable = userService.checkUsernameAvailability(username);

        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);

        return ResponseEntity.ok(response);
    }


}
