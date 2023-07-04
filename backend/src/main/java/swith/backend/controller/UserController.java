package swith.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import swith.backend.config.SecurityUtil;
import swith.backend.domain.User;
import swith.backend.dto.*;
import swith.backend.jwt.TokenInfo;
import swith.backend.service.UserService;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;


    /**
     * 회원 정보 조회
     */
    @GetMapping("/user")
    public ResponseEntity<UserInfoByTokenDto> getUserByEmail() {
        User user = userService.findUser(SecurityUtil.getLoginUsername());
        UserInfoByTokenDto userInfoByTokenDto = UserInfoByTokenDto.builder()
                .name(user.getName())
                .nickname(user.getNickname())
                .serialNumber(user.getSerialNumber())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
        return ResponseEntity.ok(userInfoByTokenDto);
    }

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity singUp(@Valid @RequestBody UserSignUpRequestDto userSignUpRequestDto, BindingResult bindingResult) {
//        if (bindingResult.hasErrors()) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult.getAllErrors());
//        }
//        if (bindingResult.hasErrors()) {
//            Map<String, String> errorMap = new HashMap<>();
//            for (FieldError error : bindingResult.getFieldErrors()) {
//                errorMap.put(error.getField(), error.getDefaultMessage());
//            }
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
//        }

        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errorMessage.append(error.getDefaultMessage()).append(", ");
            }
            errorMessage.delete(errorMessage.length() - 2, errorMessage.length());

            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", HttpStatus.BAD_REQUEST.value());
            errorMap.put("message", errorMessage.toString());

            return ResponseEntity.badRequest().body(errorMap);
        }

        String encodedPassword = passwordEncoder.encode(userSignUpRequestDto.getPassword());
        User user = User.builder()
                .name(userSignUpRequestDto.getName())
                .nickname(userSignUpRequestDto.getNickname())
                .password(encodedPassword)
                .serialNumber(userSignUpRequestDto.getSerialNumber())
                .phoneNumber(userSignUpRequestDto.getPhoneNumber())
                .email(userSignUpRequestDto.getEmail())
                .build();
        user.getRoles().add("USER");
        userService.join(user);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public TokenInfo login(@RequestBody UserLoginRequestDto userLoginRequestDto) {
        TokenInfo tokenInfo = userService.login(userLoginRequestDto.getEmail(), userLoginRequestDto.getPassword());

        return tokenInfo;
    }


    /**
     * 아이디 찾기
     */
    @GetMapping("/find")
    public String FindIdBySerialNumber(@RequestParam("serialNumber") String serialNumber) {

        User userBySerialNumber = userService.findUserBySerialNumber(serialNumber);
        String email = userBySerialNumber.getEmail();

        return email;
    }

    /**
     * 비밀번호 초기화
     */
    @PostMapping("/sendEmail")
    public void sendEmail(@RequestBody EmailRequestDto emailRequestDto) {
        String email = emailRequestDto.getEmail();
        String serialNumber = emailRequestDto.getSerialNumber();
        userService.checkUser(email, serialNumber);
        MailDto m = userService.createMailAndChangePassword(email);
        userService.mailSend(m);

    }

    /**
     * 비밀번호 확인
     */
    @PostMapping("/check")
    public ResponseEntity<String> checkPassword(@RequestBody CheckPasswordDto checkPasswordDto) {
        return userService.checkPassword(checkPasswordDto.getEmail(), checkPasswordDto.getPassword());
    }

    /**
     * 회원정보 수정
     */
    @PutMapping("/edit")
    public void editUserInfo(@RequestBody UserEditDto userEditDto) {
        userService.edit(userEditDto);
    }

    /**
     * 회원 탈퇴
     */
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/withdraw/{serialNumber}")
    public void delete(@PathVariable("serialNumber") String serialNumber) {

        userService.delete(serialNumber);
    }
}
