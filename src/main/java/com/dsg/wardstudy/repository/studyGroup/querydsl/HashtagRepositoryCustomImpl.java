package com.dsg.wardstudy.repository.studyGroup.querydsl;

import com.dsg.wardstudy.domain.studyGroup.Hashtag;
import com.dsg.wardstudy.domain.studyGroup.QHashtag;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class HashtagRepositoryCustomImpl extends QuerydslRepositorySupport implements HashtagRepositoryCustom {

    public HashtagRepositoryCustomImpl() {
        super(Hashtag.class);
    }
    @Override
    public List<String> findAllHashtagNames() {
        QHashtag qHashtag = QHashtag.hashtag;

        return from(qHashtag)
                .select(qHashtag.hashtagName)
                .fetch();
    }
}
