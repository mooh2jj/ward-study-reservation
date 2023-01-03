package com.dsg.wardstudy.domain.studyGroup.dto;

import com.dsg.wardstudy.domain.studyGroup.entity.StudyGroup;
import com.dsg.wardstudy.domain.user.entity.UserGroup;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StudyGroupResponse {

    private Long studyGroupId;
    private Long registerId;
    private String title;
    private String content;

    @Builder
    public StudyGroupResponse(Long studyGroupId, Long registerId, String title, String content) {
        this.studyGroupId = studyGroupId;
        this.registerId = registerId;
        this.title = title;
        this.content = content;
    }

    public static StudyGroupResponse mapToDto(StudyGroup savedGroup) {
        return StudyGroupResponse.builder()
                .studyGroupId(savedGroup.getId())
                .registerId(savedGroup.getUser().getId())
                .title(savedGroup.getTitle())
                .content(savedGroup.getContent())
                .build();
    }


}
