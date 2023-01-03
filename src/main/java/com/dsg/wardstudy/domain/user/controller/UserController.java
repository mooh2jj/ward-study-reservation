package com.dsg.wardstudy.domain.user.controller;

import com.dsg.wardstudy.common.Response;
import com.dsg.wardstudy.common.auth.AuthUser;
import com.dsg.wardstudy.domain.alarm.dto.AlarmDto;
import com.dsg.wardstudy.domain.user.entity.User;
import com.dsg.wardstudy.domain.user.entity.UserGroup;
import com.dsg.wardstudy.domain.user.dto.LoginDto;
import com.dsg.wardstudy.domain.user.dto.SignUpRequest;
import com.dsg.wardstudy.domain.user.dto.SignUpResponse;
import com.dsg.wardstudy.domain.user.dto.UserInfo;
import com.dsg.wardstudy.domain.user.service.LoginService;
import com.dsg.wardstudy.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Log4j2
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final LoginService loginService;

    /**
     * 회원가입(register)
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignUpRequest signUpDto
    ) {
        log.info("users signup, signUpDto: {}", signUpDto);
        SignUpResponse signUpResponse = loginService.signUp(signUpDto);

        return new ResponseEntity<>(signUpResponse, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginDto loginDto
    ) {
        loginService.loginUser(loginDto);
        return ResponseEntity.ok("login success!");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> login() {
        loginService.logoutUser();
        return ResponseEntity.ok("logout success!");
    }

    /**
     * 사용자 회원정보 조회
     *
     * @return
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable("userId") Long userId) {
        UserInfo userInfo = userService.getUser(userId);
        log.info("userInfo: {}", userInfo);

        return new ResponseEntity<>(userInfo, HttpStatus.OK);
    }

    /**
     * 사용자 서비스 탈퇴
     *
     * @param userId
     * @return
     */
    @DeleteMapping
    public ResponseEntity<?> userWithdraw(@AuthUser Long userId) {
        userService.withdrawUser(userId);
        return ResponseEntity.ok("userWithdraw success");
    }

    @GetMapping("/alarm")
    public Page<AlarmDto> alarm(@AuthUser Long userId, Pageable pageable) {

        return userService.alarmList(userId, pageable);
    }

}
