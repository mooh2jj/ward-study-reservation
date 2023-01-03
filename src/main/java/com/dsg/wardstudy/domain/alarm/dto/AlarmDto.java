package com.dsg.wardstudy.domain.alarm.dto;

import com.dsg.wardstudy.domain.alarm.constant.AlarmType;
import com.dsg.wardstudy.domain.alarm.entity.Alarm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlarmDto {

    private Long id;
    private AlarmType alarmType;
    private AlarmArgs alarmArgs;

    public static AlarmDto toDto(Alarm alarm) {
        return AlarmDto.builder()
                .id(alarm.getId())
                .alarmType(alarm.getAlarmType())
                .alarmArgs(alarm.getAlarmArgs())
                .build();
    }

}
