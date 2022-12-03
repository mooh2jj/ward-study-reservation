package com.dsg.wardstudy.domain.studyGroup;

import com.dsg.wardstudy.domain.BaseTimeEntity;
import com.dsg.wardstudy.domain.attach.Attach;
import com.dsg.wardstudy.domain.user.UserGroup;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "study_group")
@ToString(of = {"id", "title", "content"})
public class StudyGroup extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_group_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @JoinTable(
            name = "study_group_hashtag",
            joinColumns = @JoinColumn(name = "study_group_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Hashtag> hashtags = new LinkedHashSet<>();

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<UserGroup> userGroups = new ArrayList<>();

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL)
    private List<Attach> attaches = new ArrayList<>();

    @Builder
    public StudyGroup(Long id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }


}
