package com.dsg.wardstudy.domain.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlarmArgs {

    // 알림을 발생시킨 사람
    private Long fromUserId;
    private Long targetId;

}
