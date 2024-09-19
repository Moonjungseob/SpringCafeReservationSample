package com.busanit501lsy.springcafereservationsample.service;

import com.busanit501lsy.springcafereservationsample.entity.MemberRole;
import com.busanit501lsy.springcafereservationsample.entity.User;
import com.busanit501lsy.springcafereservationsample.entity.mongoEntity.ProfileImage;
import com.busanit501lsy.springcafereservationsample.repository.UserRepository;
import com.busanit501lsy.springcafereservationsample.repository.mongoRepository.ProfileImageRepository;
import com.busanit501lsy.springcafereservationsample.util.ImageUtil;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class UserService {

    @Autowired
    UserRepository userRepository;

    // 프로필 이미지, 몽고디비 연결
    @Autowired
    ProfileImageRepository profileImageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 페이징 처리
    public Page<User> getAllUsersWithPage(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByName(String name) {
        return userRepository.findByName(name);
    }


    @Transactional
    public User createUser(User user) {
        user.addRole(MemberRole.USER);
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setName(userDetails.getName());
        user.setAddress(userDetails.getAddress());
        user.setPhone(userDetails.getPhone());
        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // 프로필 이미지 삭제
        if(user.getProfileImageId() != null && !user.getProfileImageId().isEmpty()) {
            deleteProfileImage(user);
        }
        //reservationItem 삭제되면

        // 유저 삭제시, reservation도 같이 삭제

        //
        userRepository.delete(user);
    }

    @Transactional
    //프로필 이미지 업로드, 레스트 형식
    public void saveProfileImage(Long userId, MultipartFile file) throws IOException {
        // Get the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        byte[] imageFile = ImageUtil.createThumbnail(file,500,500);
        // Create and save the profile image
        ProfileImage profileImage = new ProfileImage(
                file.getOriginalFilename(),
                file.getContentType(),
                imageFile
        );
        ProfileImage savedImage = profileImageRepository.save(profileImage);

        // Link the profile image to the user
        user.setProfileImageId(savedImage.getId());
        userRepository.save(user);
    }

    @Transactional
    // 이미지 가져오기
    public ProfileImage getProfileImage(String imageId) {
        return profileImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));
    }

    @Transactional
    // 프로필 이미지만 삭제
    public void deleteProfileImage(User user) {
        // 현재 사용자가 가진 프로필 이미지 ID 가져오기
        log.info("lsy user : " + user);
        String profileImageId = user.getProfileImageId();
        log.info("lsy profileImageId : " + profileImageId);

        // 프로필 이미지 ID가 null이 아닌 경우에만 삭제 작업 수행
        if (profileImageId != null) {
            // MongoDB에서 프로필 이미지 삭제
            profileImageRepository.deleteById(profileImageId);

            // 사용자의 profileImageId 필드를 null로 설정
            user.setProfileImageId(null);

            // 업데이트된 사용자 정보 저장
            userRepository.save(user);
        }
    }

    // 아이디 중복 검사
    // 아이디 중복 여부 체크
    public boolean checkUsernameAvailability(String username) {
        // 레포지토리에서 아이디로 유저를 찾고, 존재하면 중복된 아이디로 판단
        return !userRepository.existsByUsername(username);
    }

}
