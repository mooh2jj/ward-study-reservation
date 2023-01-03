package com.dsg.wardstudy.domain.studyGroup.service;

import com.dsg.wardstudy.common.exception.ErrorCode;
import com.dsg.wardstudy.common.exception.WSApiException;
import com.dsg.wardstudy.domain.alarm.constant.AlarmType;
import com.dsg.wardstudy.domain.alarm.dto.AlarmArgs;
import com.dsg.wardstudy.domain.alarm.entity.Alarm;
import com.dsg.wardstudy.domain.attach.entity.Attach;
import com.dsg.wardstudy.domain.studyGroup.entity.Like;
import com.dsg.wardstudy.domain.studyGroup.entity.QStudyGroup;
import com.dsg.wardstudy.domain.studyGroup.entity.StudyGroup;
import com.dsg.wardstudy.domain.studyGroup.dto.PageResponse;
import com.dsg.wardstudy.domain.studyGroup.dto.StudyGroupRequest;
import com.dsg.wardstudy.domain.studyGroup.dto.StudyGroupResponse;
import com.dsg.wardstudy.domain.user.entity.User;
import com.dsg.wardstudy.domain.user.entity.UserGroup;
import com.dsg.wardstudy.repository.alarm.AlarmRepository;
import com.dsg.wardstudy.repository.attach.AttachRepository;
import com.dsg.wardstudy.repository.like.LikeRepository;
import com.dsg.wardstudy.repository.reservation.ReservationQueryRepository;
import com.dsg.wardstudy.repository.reservation.ReservationRepository;
import com.dsg.wardstudy.repository.studyGroup.StudyGroupRepository;
import com.dsg.wardstudy.repository.user.UserGroupRepository;
import com.dsg.wardstudy.repository.user.UserRepository;
import com.dsg.wardstudy.domain.user.constant.UserType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.dsg.wardstudy.config.redis.RedisCacheKey.STUDY_GROUP_LIST;

@Log4j2
@Service
@RequiredArgsConstructor
public class StudyGroupServiceImpl implements StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationQueryRepository reservationQueryRepository;

    private final LikeRepository likeRepository;

    private final AttachRepository attachRepository;

    private final AlarmRepository alarmRepository;

    @Transactional
    @Override
    public StudyGroupResponse register(Long userId, StudyGroupRequest studyGroupRequest) {

        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("user 대상이 없습니다. userId: {}", userId);
                    throw new WSApiException(ErrorCode.NO_FOUND_ENTITY, "can't find a User by " +
                            " userId: " + userId);
                });

        StudyGroup studyGroup = StudyGroup.of(findUser, studyGroupRequest);
        StudyGroup savedStudyGroup = studyGroupRepository.save(studyGroup);

        // studyGroup 등록시 UserType L(리더)로 등록
        UserGroup userGroup = UserGroup.builder()
                .studyGroup(savedStudyGroup)
                .user(findUser)
                .userType(UserType.LEADER)
                .build();
        userGroupRepository.save(userGroup);

        // 파일 첨부 있을시
        if (studyGroupRequest.getAttachDTOS() != null && studyGroupRequest.getAttachDTOS().size() != 0) {
            studyGroupRequest.getAttachDTOS().forEach(attachDTO -> {
                attachRepository.save(Attach.of(attachDTO, savedStudyGroup));
            });
        }

        return StudyGroupResponse.mapToDto(savedStudyGroup);
    }

    @Transactional(readOnly = true)
    @Override
    public StudyGroupResponse getById(Long studyGroupId) {

        StudyGroup studyGroup = studyGroupRepository.findById(studyGroupId)
                .orElseThrow(() -> {
                    log.error("studyGroup 대상이 없습니다. studyGroupId: {}", studyGroupId);
                    throw new WSApiException(ErrorCode.NO_FOUND_ENTITY, "can't find a StudyGroup by " +
                            " studyGroupId: " + studyGroupId);
                });

        return StudyGroupResponse.mapToDto(studyGroup);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse.StudyGroupDetail getAll(Pageable pageable, String type, String keyword) {
        // 검색조건
        BooleanBuilder booleanBuilder = getSearch(type, keyword);
        log.info("booleanBuilder getSearch: {}", booleanBuilder);

        Page<StudyGroupResponse> studyGroupResponsePage = studyGroupRepository.findAll(booleanBuilder, pageable)
                .map(StudyGroupResponse::mapToDto);
        return PageResponse.of(pageable, studyGroupResponsePage);
    }

    private BooleanBuilder getSearch(String type, String keyword) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QStudyGroup qStudyGroup = QStudyGroup.studyGroup;
        BooleanExpression booleanExpression = qStudyGroup.id.gt(0L);
        booleanBuilder.and(booleanExpression);

        // 검색 조건이 없는 경우
        if (!StringUtils.hasText(type)) {
            return booleanBuilder;
        }
        BooleanBuilder conditionBuilder = new BooleanBuilder();
        if(type.contains("t")) {
            conditionBuilder.or(qStudyGroup.title.contains(keyword));
        }
        if(type.contains("c")) {
            conditionBuilder.or(qStudyGroup.content.contains(keyword));
        }
        // 모든 조건 통합
        booleanBuilder.and(conditionBuilder);
        return booleanBuilder;
    }

    @CacheEvict(key = "#userId", value = STUDY_GROUP_LIST, cacheManager = "redisCacheManager")
    @Transactional
    @Override
    public Long updateById(Long userId, Long studyGroupId, StudyGroupRequest studyGroupRequest) {

        StudyGroup studyGroup = validateStudyGroup(userId, studyGroupId);

        // 파일첨부시
        attachRepository.deleteAllByStudyGroupId(studyGroup.getId());

        studyGroup.update(studyGroupRequest);
        log.info("updated studyGroup: {}", studyGroup);

        // 게시판 수정 후 attach insert 가능하게 처리
        if (studyGroupRequest.getAttachDTOS() != null && studyGroupRequest.getAttachDTOS().size() != 0) {
            studyGroupRequest.getAttachDTOS().forEach(attachDTO -> {
                attachRepository.save(Attach.of(attachDTO, studyGroup));
            });
        }

        return studyGroup.getId();

    }

    @Transactional
    @Override
    public void deleteById(Long userId, Long studyGroupId) {
        // 외래키를 가진 자식테이블 Reservation이 먼저 삭제되어야만 부모테이블인 StudyGroup도 지울 수 있음!
        reservationQueryRepository.findByUserIdAndStudyGroupId(userId, studyGroupId)
                .ifPresent(reservation -> {
                    log.info("reservation: {}", reservation);
                    reservationRepository.delete(reservation);
                });
        Optional<StudyGroup> studyGroup = validateDeleteStudyGroup(userId, studyGroupId);
        log.info("studyGroup: {}", studyGroup);
        studyGroup.ifPresent(studyGroupRepository::delete);
    }

    @Cacheable(key = "#userId", value = STUDY_GROUP_LIST, cacheManager = "redisCacheManager")
    @Transactional(readOnly = true)
    @Override
    public List<StudyGroupResponse> getAllByUserId(Long userId) {

        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("user 대상이 없습니다. userId: {}", userId);
                    throw new WSApiException(ErrorCode.NO_FOUND_ENTITY, "can't find a User by userId: " + userId);
                });

        List<UserGroup> userGroups = userGroupRepository.findByUserId(findUser.getId());

        List<Long> studyGroupsIds = userGroups.stream()
                .map(ug -> ug.getStudyGroup().getId())
                .collect(Collectors.toList());

        List<StudyGroup> studyGroups = studyGroupRepository.findByIdIn(studyGroupsIds);
        return studyGroups.stream()
                .map(StudyGroupResponse::mapToDto)
                .collect(Collectors.toList());

    }

    @Transactional
    @Override
    public StudyGroupResponse participate(Long userId, Long studyGroupId) {
        User participateUser = userRepository.findById(userId)
                .orElseThrow(() -> new WSApiException(ErrorCode.NOT_FOUND_USER));
        log.info("participate findById user : {}", participateUser);

        StudyGroup participateStudyGroup = studyGroupRepository.findById(studyGroupId)
                .orElseThrow(() -> new WSApiException(ErrorCode.NO_FOUND_ENTITY, "studyGroup", studyGroupId));
        log.info("participate studyGroup : {}", participateStudyGroup);

        // 중복 등록 방지
        userGroupRepository.findByUserIdAndSGId(participateUser.getId(), participateStudyGroup.getId())
                .ifPresent(ug -> {
                    throw new WSApiException(ErrorCode.DUPLICATED_ENTITY, "UserGroup participateUserId: " +
                            participateUser.getId() + ", studyGroupId: " + participateStudyGroup.getId());
                });

        // studyGroup 등록시 UserType P(참여자)로 등록
        UserGroup userGroup = UserGroup.builder()
                .studyGroup(participateStudyGroup)
                .user(participateUser)
                .userType(UserType.PARTICIPANT)
                .build();

        return StudyGroupResponse.mapToDto(participateStudyGroup);

    }

    @Transactional
    @Override
    public void like(Long userId, Long studyGroupId) {
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    throw new WSApiException(ErrorCode.NO_FOUND_ENTITY, "can't find a User by userId: " + userId);
                });

        StudyGroup findStudyGroup = studyGroupRepository.findById(studyGroupId)
                .orElseThrow(() -> {
                    throw new WSApiException(ErrorCode.NO_FOUND_ENTITY, "can't find a StudyGroup by " +
                            " studyGroupId: " + studyGroupId);
                });
        // check liked -> throw
        likeRepository.findByUserId(findUser.getId()).ifPresent(it -> {
            throw new WSApiException(ErrorCode.DUPLICATED_ENTITY,
                    String.format("userId %d already like studygroup %d", userId, studyGroupId));
        });
        likeRepository.save(Like.of(findUser, findStudyGroup));

        // 알림 save
        alarmRepository.save(
                Alarm.of(
                        findStudyGroup.getUser(),
                        AlarmType.NEW_LIKE_ON_STUDYGROUP,
                        new AlarmArgs(findStudyGroup.getId(), findStudyGroup.getId())
                )
        );
    }

    @Transactional(readOnly = true)
    @Override
    public int likeCount(Long studyGroupId) {
        studyGroupRepository.findById(studyGroupId)
                .orElseThrow(() -> new WSApiException(ErrorCode.NO_FOUND_ENTITY, "no studyGroup"));

        return likeRepository.countByStudyGroupId(studyGroupId);
    }

    private Optional<StudyGroup> validateDeleteStudyGroup(Long userId, Long studyGroupId) {
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("user 대상이 없습니다. userId: {}", userId);
                    throw new WSApiException(ErrorCode.NO_FOUND_ENTITY, "can't find a User by userId: " + userId);
                });

        UserType findUserType = userGroupRepository.findUserTypeByUserIdAndSGId(findUser.getId(), studyGroupId)
                .orElseThrow(() -> new WSApiException(ErrorCode.NOT_FOUND_USER, "studyGroup 등록자가 아닙니다."));

        Optional.of(findUserType).ifPresent(userType -> {
            if (!userType.equals(UserType.LEADER)) {
                log.error("userType이 Leader가 아닙니다.");
                throw new WSApiException(ErrorCode.INVALID_REQUEST,
                        "StudyGroup modification is possible only if the user is the leader."
                );
            }
        });
        return studyGroupRepository.findById(studyGroupId);
    }

    private StudyGroup validateStudyGroup(Long userId, Long studyGroupId) {
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("user 대상이 없습니다. userId: {}", userId);
                    throw new WSApiException(ErrorCode.NO_FOUND_ENTITY, "can't find a User by userId: " + userId);
                });

        StudyGroup findStudyGroup = studyGroupRepository.findById(studyGroupId)
                .orElseThrow(() -> {
                    log.error("studyGroup 대상이 없습니다. studyGroupId: {}", studyGroupId);
                    throw new WSApiException(ErrorCode.NO_FOUND_ENTITY, "can't find a StudyGroup by " +
                            " studyGroupId: " + studyGroupId);
                });

        UserType findUserType = userGroupRepository.findUserTypeByUserIdAndSGId(findUser.getId(), findStudyGroup.getId())
                .orElseThrow(() -> new WSApiException(ErrorCode.NOT_FOUND_USER, "studyGroup 등록자가 아닙니다."));

        Optional.of(findUserType).ifPresent(userType -> {
            if (!userType.equals(UserType.LEADER)) {
                log.error("userType이 Leader가 아닙니다.");
                throw new WSApiException(ErrorCode.INVALID_REQUEST,
                        "StudyGroup modification is possible only if the user is the leader.");
            }
        });
        return findStudyGroup;
    }
}
