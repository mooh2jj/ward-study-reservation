package com.dsg.wardstudy.domain.alarm.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AlarmType {

    NEW_COMMENT_ON_STUDYGROUP("new comment!"),
    NEW_LIKE_ON_STUDYGROUP("new like!"),
    ;

    private final String alarmText;

}
