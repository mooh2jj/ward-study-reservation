package com.dsg.wardstudy.domain.alarm.entity;

import com.dsg.wardstudy.domain.BaseTimeEntity;
import com.dsg.wardstudy.domain.alarm.constant.AlarmType;
import com.dsg.wardstudy.domain.alarm.dto.AlarmArgs;
import com.dsg.wardstudy.domain.user.entity.User;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@TypeDef(name = "json", typeClass = JsonStringType.class)
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "alarms", indexes = {
        @Index(name = "user_id_idx", columnList = "user_id")
})
public class Alarm extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private Long id;

    // 알림을 받는 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Setter
    private User user;

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;

    @Type(type = "json")
    @Column(columnDefinition = "json")
    private AlarmArgs alarmArgs;

    @Builder
    public Alarm(Long id, User user, AlarmType alarmType, AlarmArgs alarmArgs) {
        this.id = id;
        this.user = user;
        this.alarmType = alarmType;
        this.alarmArgs = alarmArgs;
    }

    public static Alarm of(User user, AlarmType alarmType, AlarmArgs alarmArgs) {
        return Alarm.builder()
                .user(user) // 알림을 받은 사람
                .alarmType(alarmType)
                .alarmArgs(alarmArgs)
                .build();
    }
}
