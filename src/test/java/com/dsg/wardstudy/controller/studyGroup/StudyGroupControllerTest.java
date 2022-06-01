package com.dsg.wardstudy.controller.studyGroup;

import com.dsg.wardstudy.domain.studyGroup.StudyGroup;
import com.dsg.wardstudy.domain.user.User;
import com.dsg.wardstudy.domain.user.UserGroup;
import com.dsg.wardstudy.dto.studyGroup.StudyGroupRequest;
import com.dsg.wardstudy.dto.studyGroup.StudyGroupResponse;
import com.dsg.wardstudy.exception.ErrorCode;
import com.dsg.wardstudy.exception.WSApiException;
import com.dsg.wardstudy.service.studyGroup.StudyGroupService;
import com.dsg.wardstudy.type.UserType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudyGroupController.class)
class StudyGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudyGroupService studyGroupService;

    @Autowired
    private ObjectMapper objectMapper;

    private StudyGroup studyGroup;
    private User user;
    private UserGroup userGroup;

    @BeforeEach
    void setup() {

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        studyGroup = StudyGroup.builder()
                .title("testSG")
                .content("인원 4명의 스터디그룹을 모집합니다.")
                .build();

        user = User.builder()
                .id(1L)
                .build();

        userGroup = UserGroup.builder()
                .user(user)
                .studyGroup(studyGroup)
                .userType(UserType.L)
                .build();


    }

    @Test
    public void givenStudyGroupRequest_whenCreate_thenReturnStudyGroupResponse() throws Exception {
        // given - precondition or setup
        StudyGroupRequest studyGroupRequest = StudyGroupRequest.builder()
                .title(studyGroup.getTitle())
                .content(studyGroup.getContent())
                .build();

        StudyGroupResponse studyGroupResponse = StudyGroupResponse.builder()
                .title(userGroup.getStudyGroup().getTitle())
                .content(userGroup.getStudyGroup().getContent())
                .build();

        given(studyGroupService.create(anyLong(), any(StudyGroupRequest.class)))
                .willReturn(studyGroupResponse);

        // when - action or the behaviour that we are going test
        ResultActions resultActions = mockMvc.perform(post("/users/{userId}/study-group", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studyGroupRequest)));

        // then - verify the output
        resultActions
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is(studyGroup.getTitle())))
                .andExpect(jsonPath("$.content", is(studyGroup.getContent())));

    }

    @Test
    public void givenListOfStudyGroupResponses_whenGet_thenReturnStudyGroupResponseList() throws Exception {
        // given - precondition or setup
        int length = 10;
        List<StudyGroupResponse> studyGroupResponses = new ArrayList<>();
        StudyGroupResponse studyGroupResponse = StudyGroupResponse.builder()
                .title(studyGroup.getTitle())
                .content(studyGroup.getContent())
                .build();
        for (int i = 0; i < length; i++) {
            studyGroupResponses.add(
                    studyGroupResponse
            );
        }

        given(studyGroupService.getAll()).willReturn(studyGroupResponses);

        // when - action or the behaviour that we are going test
        // then - verify the output
        mockMvc.perform(get("/study-group"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(length));

    }

    @Test
    public void givenStudyGroupId_whenGet_thenReturnStudyGroupResponse() throws Exception {
        Long studyGroupId = 1L;
        // given - precondition or setup
        StudyGroupResponse studyGroupResponse = StudyGroupResponse.builder()
                .title(studyGroup.getTitle())
                .content(studyGroup.getContent())
                .build();
        given(studyGroupService.getById(anyLong())).willReturn(studyGroupResponse);

        // when - action or the behaviour that we are going test
        // then - verify the output
        mockMvc.perform(get("/study-group/" + studyGroupId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(studyGroupResponse.getTitle())))
                .andExpect(jsonPath("$.content", is(studyGroupResponse.getContent())));

    }

    @Test
    public void givenInvalidStudyGroupId_whenGet_thenReturn404() throws Exception {
        Long studyGroupId = 190L;
        // given - precondition or setup
        given(studyGroupService.getById(studyGroupId))
                .willThrow(new WSApiException(ErrorCode.NO_FOUND_ENTITY,
                        "can't find a studyGroup by " + "studyGroupId: " +  studyGroupId));

        // when - action or the behaviour that we are going test
        // then - verify the output
        mockMvc.perform(get("/study-group/" + studyGroupId))
                .andDo(print())
                .andExpect(status().isNotFound());

    }

    @Test
    public void getAllByUserId() throws Exception {
        // TODO : controller 메서드 만들기
        // given - precondition or setup

        // when - action or the behaviour that we are going test

        // then - verify the output

    }

    @Test
    public void givenStudyGroupIdAndUpdatedStudyGroupRequest_whenUpdate_thenReturnUpdateStudyGroupId() throws Exception {
        // given - precondition or setup
        Long studyGroupId = 1L;
        Long userId = 1L;

        StudyGroupRequest updateStudyGroupRequest = StudyGroupRequest.builder()
                .title("RamStudy")
                .content("Ram effective Java study")
                .build();

        given(studyGroupService.updateById(userId, studyGroupId, updateStudyGroupRequest))
                .willReturn(studyGroupId);

        // when - action or the behaviour that we are going test
        // then - verify the output
        mockMvc.perform(put("/users/{userId}/study-group/{studyGroupId}",user.getId(), studyGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStudyGroupRequest)))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    public void givenStudyGroupId_whenDelete_thenReturn200() throws Exception {
        // given - precondition or setup
        Long userId = 1L;
        Long studyGroupId = 1L;
        willDoNothing().given(studyGroupService).deleteById(userId, studyGroupId);

        // when - action or the behaviour that we are going test
        // then - verify the output
        mockMvc.perform(delete("/users/{userId}/study-group/{studyGroupId}",userId, studyGroupId))
                .andDo(print())
                .andExpect(status().isOk());

    }
}