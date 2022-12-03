package com.dsg.wardstudy.repository.studyGroup.querydsl;

import com.dsg.wardstudy.domain.studyGroup.QHashtag;
import com.dsg.wardstudy.domain.studyGroup.QStudyGroup;
import com.dsg.wardstudy.domain.studyGroup.StudyGroup;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.Collection;
import java.util.List;

public class StudyGroupRepositoryCustomImpl extends QuerydslRepositorySupport implements StudyGroupRepositoryCustom {


    public StudyGroupRepositoryCustomImpl() {
        super(StudyGroup.class);
    }

    @Override
    public List<String> findAllDistinctHashtags() {

        QStudyGroup qStudyGroup = QStudyGroup.studyGroup;

        return from(qStudyGroup)
                .select(qStudyGroup.hashtags.any().hashtagName)
                .fetch();

    }

    @Override
    public Page<StudyGroup> findByHashtagNames(Collection<String> hashtagNames, Pageable pageable) {
        QHashtag qHashtag = QHashtag.hashtag;
        QStudyGroup qStudyGroup = QStudyGroup.studyGroup;

        JPQLQuery<StudyGroup> query = from(qStudyGroup)
                .innerJoin(qStudyGroup.hashtags, qHashtag)
                .where(qHashtag.hashtagName.in(hashtagNames));

        List<StudyGroup> studyGroups = getQuerydsl().applyPagination(pageable, query).fetch();

        return new PageImpl<>(studyGroups, pageable, query.fetchCount());
    }
}
