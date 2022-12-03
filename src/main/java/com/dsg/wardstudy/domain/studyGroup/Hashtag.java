package com.dsg.wardstudy.domain.studyGroup;

import com.dsg.wardstudy.domain.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(indexes = {
        @Index(columnList = "hashtag_name", unique = true)
})
public class Hashtag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hashtag_name", nullable = false)
    private String hashtagName;

    @ManyToMany(mappedBy = "hashtags")
    private Set<StudyGroup> studyGroups = new LinkedHashSet<>();

    @Builder
    public Hashtag(String hashtagName) {
        this.hashtagName = hashtagName;
    }

    public static Hashtag of(String hashtagName) {
        return Hashtag.builder()
                .hashtagName(hashtagName)
                .build();
    }
}
