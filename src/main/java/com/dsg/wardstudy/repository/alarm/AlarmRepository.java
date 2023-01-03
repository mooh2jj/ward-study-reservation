package com.dsg.wardstudy.repository.alarm;

import com.dsg.wardstudy.domain.alarm.entity.Alarm;
import com.dsg.wardstudy.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    Page<Alarm> findByUser(User user, Pageable pageable);
}
