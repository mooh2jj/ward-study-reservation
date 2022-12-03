package com.dsg.wardstudy.repository.studyGroup;

import com.dsg.wardstudy.domain.studyGroup.StudyGroup;
import com.dsg.wardstudy.repository.studyGroup.querydsl.StudyGroupRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface StudyGroupRepository extends
        JpaRepository<StudyGroup, Long>,
        StudyGroupRepositoryCustom,
        QuerydslPredicateExecutor<StudyGroup> {

    // in 절안에 컬렉션 findByIdIn
    List<StudyGroup> findByIdIn(List<Long> ids);

//    Page<StudyGroup> findStudyGroupsViaHashtag(Object o, Pageable pageable);

}
