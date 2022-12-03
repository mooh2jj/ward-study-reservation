package com.dsg.wardstudy.repository.studyGroup.querydsl;

import com.dsg.wardstudy.domain.studyGroup.StudyGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public interface StudyGroupRepositoryCustom {

    List<String> findAllDistinctHashtags();

    Page<StudyGroup> findByHashtagNames(Collection<String> hashtagName, Pageable pageable);
}
