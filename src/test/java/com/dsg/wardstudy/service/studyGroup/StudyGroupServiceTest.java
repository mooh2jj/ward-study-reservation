package com.dsg.wardstudy.service.studyGroup;

import com.dsg.wardstudy.domain.reservation.Reservation;
import com.dsg.wardstudy.domain.studyGroup.QStudyGroup;
import com.dsg.wardstudy.domain.studyGroup.StudyGroup;
import com.dsg.wardstudy.domain.studyGroup.service.StudyGroupServiceImpl;
import com.dsg.wardstudy.domain.user.User;
import com.dsg.wardstudy.domain.user.UserGroup;
import com.dsg.wardstudy.domain.studyGroup.dto.PageResponse;
import com.dsg.wardstudy.domain.studyGroup.dto.StudyGroupRequest;
import com.dsg.wardstudy.domain.studyGroup.dto.StudyGroupResponse;
import com.dsg.wardstudy.common.exception.WSApiException;
import com.dsg.wardstudy.repository.reservation.ReservationQueryRepository;
import com.dsg.wardstudy.repository.reservation.ReservationRepository;
import com.dsg.wardstudy.repository.studyGroup.HashtagRepository;
import com.dsg.wardstudy.repository.studyGroup.StudyGroupRepository;
import com.dsg.wardstudy.repository.user.UserGroupRepository;
import com.dsg.wardstudy.repository.user.UserRepository;
import com.dsg.wardstudy.type.UserType;
import com.querydsl.core.BooleanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

@Slf4j
@ExtendWith(MockitoExtension.class)
class StudyGroupServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationQueryRepository reservationQueryRepository;
    @Mock
    private StudyGroupRepository studyGroupRepository;
    @Mock
    private UserRepository userRepository;

    @Mock
    private HashtagRepository hashtagRepository;

    @Mock
    private UserGroupRepository userGroupRepository;

    @InjectMocks
    private StudyGroupServiceImpl studyGroupService;

    private StudyGroup studyGroup;
    private User user;
    private UserGroup userGroup;

    private Reservation reservation;

    private StudyGroupRequest studyGroupRequest;

    @BeforeEach
    void setup() {
        studyGroup = StudyGroup.builder()
                .id(1L)
                .title("testSG")
                .content("인원 4명의 스터디그룹을 모집합니다.")
                .build();

        user = User.builder()
                .id(1L)
                .build();

        userGroup = UserGroup.builder()
                .user(user)
                .studyGroup(studyGroup)
                .userType(UserType.LEADER)
                .build();

        reservation = Reservation.builder()
//                .id("1||2019-11-03 06:30:00")
                .user(user)
                .studyGroup(studyGroup)
                .build();

    }

    @Test
    public void givenStudyGroup_whenSave_thenReturnStudyGroupResponse() {
        // given - precondition or setup
        given(userRepository.findById(anyLong()))
                .willReturn(Optional.ofNullable(user));
        given(studyGroupRepository.save(any(StudyGroup.class)))
                .willReturn(studyGroup);

        given(userGroupRepository.save(any(UserGroup.class)))
                .willReturn(userGroup);

        studyGroupRequest = StudyGroupRequest.builder()
                .title(studyGroup.getTitle())
                .content(studyGroup.getContent())
                .build();
        // when - action or the behaviour that we are going test
        StudyGroupResponse studyGroupResponse = studyGroupService.register(user.getId(), studyGroupRequest);
        log.info("studyGroupResponse: {}", studyGroupResponse);

        // then - verify the output
        assertThat(studyGroupResponse).isNotNull();
        assertThat(studyGroupResponse.getTitle()).isEqualTo(studyGroupRequest.getTitle());
        assertThat(studyGroupResponse.getContent()).isEqualTo(studyGroupRequest.getContent());

    }

    @Test
    public void givenStudyGroup_whenGetById_thenReturnStudyGroupResponse() {
        // getById
        // given - precondition or setup
        Optional<StudyGroup> studyGroup = Optional.of(this.studyGroup);
        given(studyGroupRepository.findById(1L))
                .willReturn(studyGroup);
        // when - action or the behaviour that we are going test
        StudyGroupResponse studyGroupResponse = studyGroupService.getById(1L);
        log.info("studyGroupResponse: {}", studyGroupResponse);

        // then - verify the output
        assertThat(studyGroupResponse.getTitle()).isEqualTo("testSG");
        assertThat(studyGroupResponse.getContent()).isEqualTo("인원 4명의 스터디그룹을 모집합니다.");

    }

    @Test
    public void givenStudyGroup_whenGetById_thenThrowsException() {
        // given - precondition or setup
        // when - action or the behaviour that we are going test
        given(studyGroupRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        // then - verify the output
        assertThatThrownBy(() -> {
            studyGroupService.getById(1L);
        }).isInstanceOf(WSApiException.class);
    }


    @Test
    public void givenStudyGroupList_whenGetAll_thenReturnStudyGroupResponseList() {
        // TODO : paging NPE 발생 issue#23 findAll(pageable)부터 안됐음.
        // given - precondition or setup
        StudyGroup studyGroup1 = StudyGroup.builder()
                .id(100L)
                .title("testSG2")
                .content("인원 6명의 스터디그룹을 모집합니다.")
                .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());

        QStudyGroup qStudyGroup = QStudyGroup.studyGroup;

        String type = "t";
        String keyword = "test";

        // 검색조건 추가
        BooleanBuilder conditionBuilder = new BooleanBuilder();

        if(type.contains("t")) {
            conditionBuilder.or(qStudyGroup.title.contains(keyword));
        }
        if(type.contains("c")) {
            conditionBuilder.or(qStudyGroup.content.contains(keyword));
        }

        given(studyGroupRepository.findAll(conditionBuilder, pageable).getContent())
                .willReturn(List.of(studyGroup, studyGroup1));
        // when - action or the behaviour that we are going test


        PageResponse.StudyGroupDetail studyGroupPageResponses = studyGroupService.getAll(pageable, type, keyword);
        log.info("studyGroupPageResponses: {}", studyGroupPageResponses);
        // then - verify the output
        assertThat(studyGroupPageResponses).isNotNull();
        assertThat(studyGroupPageResponses.getTotalElements()).isEqualTo(2);

    }

    @Test
    public void givenStudyGroupList_whenGetAll_Negative_thenReturnStudyGroupResponseList() {
        // TODO : paging NPE 발생 issue#23
        // given - precondition or setup
        StudyGroup studyGroup1 = StudyGroup.builder()
                .id(2L)
                .title("testSG2")
                .content("인원 6명의 스터디그룹을 모집합니다.")
                .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());

        QStudyGroup qStudyGroup = QStudyGroup.studyGroup;

        String type = "t";
        String keyword = "test";

        // 검색조건 추가
        BooleanBuilder conditionBuilder = new BooleanBuilder();

        if(type.contains("t")) {
            conditionBuilder.or(qStudyGroup.title.contains(keyword));
        }
        if(type.contains("c")) {
            conditionBuilder.or(qStudyGroup.content.contains(keyword));
        }

        given(studyGroupRepository.findAll(conditionBuilder, pageable).getContent())
                .willReturn(Collections.emptyList());
        // when - action or the behaviour that we are going test
        PageResponse.StudyGroupDetail studyGroupPageResponses = studyGroupService.getAll(pageable, type, keyword);
        log.info("studyGroupPageResponses: {}", studyGroupPageResponses);
        // then - verify the output
        assertThat(studyGroupPageResponses).isNull();
        assertThat(studyGroupPageResponses.getTotalElements()).isEqualTo(0);

    }

    @Test
    public void givenStudyGroup_whenUpdate_thenReturnUpdatedStudyGroup() {
        // given - precondition or setup
        given(userRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(studyGroupRepository.findById(anyLong()))
                .willReturn(Optional.of(studyGroup));

        given(userGroupRepository.findUserTypeByUserIdAndSGId(anyLong(), anyLong()))
                .willReturn(Optional.of(UserType.LEADER));

        studyGroupRequest = StudyGroupRequest.builder()
                .title("JumpToSpringboot_study")
                .content("JumpTo님이 진행하는 스터디")
                .build();

        // when - action or the behaviour that we are going test
        Long updatedStudyGroupId = studyGroupService.updateById(user.getId(), studyGroup.getId(), studyGroupRequest);
        log.info("updatedStudyGroupId: {}", updatedStudyGroupId);
        StudyGroupResponse updatedResponse = studyGroupService.getById(updatedStudyGroupId);

        // then - verify the output
        assertThat(this.studyGroup.getId()).isEqualTo(updatedStudyGroupId);
        assertThat(updatedResponse.getTitle()).isEqualTo(studyGroupRequest.getTitle());
        assertThat(updatedResponse.getContent()).isEqualTo(studyGroupRequest.getContent());

    }

    @Test
    public void givenStudyGroupId_whenDelete_thenNothing() {
        // given - precondition or setup
        Long userId = 1L;
        Long studyGroupId = 1L;

        given(reservationQueryRepository.findByUserIdAndStudyGroupId(anyLong(), anyLong()))
                .willReturn(Optional.ofNullable(reservation));

        given(userRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(userGroupRepository.findUserTypeByUserIdAndSGId(anyLong(), anyLong()))
                .willReturn(Optional.of(UserType.LEADER));

        given(studyGroupRepository.findById(anyLong()))
                .willReturn(Optional.of(studyGroup));

        willDoNothing().given(reservationRepository).delete(reservation);
        willDoNothing().given(studyGroupRepository).delete(studyGroup);

        // when - action or the behaviour that we are going test
        studyGroupService.deleteById(userId, studyGroupId);

        // then - verify the output
        verify(reservationRepository).delete(reservation);
        verify(studyGroupRepository).delete(studyGroup);

    }

    @DisplayName("해시태그를 조회하면 유니크 해시태그 리스트를 반환")
    @Test
    public void givenHashtag_returnList() {
        // given
        List<String> expectedHashtags = List.of("#java", "#spring", "#boot");
        given(studyGroupRepository.findAllDistinctHashtags()).willReturn(expectedHashtags);

        // when
        List<String> actualHashtags = studyGroupService.getHashtags();

        // then
        assertThat(actualHashtags).isEqualTo(expectedHashtags);
        then(studyGroupRepository).should().findAllDistinctHashtags();

    }

}
