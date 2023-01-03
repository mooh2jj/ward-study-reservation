package com.dsg.wardstudy.domain.studyGroup.entity;

import com.dsg.wardstudy.domain.BaseTimeEntity;
import com.dsg.wardstudy.domain.attach.entity.Attach;
import com.dsg.wardstudy.domain.comment.entity.Comment;
import com.dsg.wardstudy.domain.studyGroup.dto.StudyGroupRequest;
import com.dsg.wardstudy.domain.user.entity.User;
import com.dsg.wardstudy.domain.user.entity.UserGroup;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    // 스터디그룹 등록자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Setter
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<UserGroup> userGroups = new ArrayList<>();

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL)
    private List<Attach> attaches = new ArrayList<>();

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL)
    private List<Like> likes = new ArrayList<>();

    @Builder
    public StudyGroup(Long id, User user, String title, String content) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.content = content;
    }

    public static StudyGroup of(User user, StudyGroupRequest studyGroupRequest) {
        return StudyGroup.builder()
                .user(user)
                .title(studyGroupRequest.getTitle())
                .content(studyGroupRequest.getContent())
                .build();
    }

    public void update(StudyGroupRequest studyGroupRequest) {
        this.title = studyGroupRequest.getTitle();
        this.content = studyGroupRequest.getContent();
    }


}
