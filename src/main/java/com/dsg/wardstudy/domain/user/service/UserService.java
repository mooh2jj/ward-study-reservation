package com.dsg.wardstudy.domain.user.service;

import com.dsg.wardstudy.domain.alarm.dto.AlarmDto;
import com.dsg.wardstudy.domain.user.entity.UserGroup;
import com.dsg.wardstudy.domain.user.dto.LoginDto;
import com.dsg.wardstudy.domain.user.dto.SignUpRequest;
import com.dsg.wardstudy.domain.user.dto.UserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserInfo create(SignUpRequest signUpRequest);

    UserInfo getUser(Long userId);

    LoginDto getByEmailAndPassword(String email, String password);

    void withdrawUser(Long userId);

    Page<AlarmDto> alarmList(Long userId, Pageable pageable);
}
