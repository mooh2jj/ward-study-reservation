package com.dsg.wardstudy.service.reservation;

import com.dsg.wardstudy.domain.reservation.Reservation;
import com.dsg.wardstudy.domain.reservation.Room;
import com.dsg.wardstudy.domain.studyGroup.StudyGroup;
import com.dsg.wardstudy.domain.user.User;
import com.dsg.wardstudy.dto.reservation.ReservationCreateRequest;
import com.dsg.wardstudy.dto.reservation.ReservationDetails;
import com.dsg.wardstudy.dto.reservation.ReservationUpdateRequest;
import com.dsg.wardstudy.dto.reservation.ValidateFindByIdDto;
import com.dsg.wardstudy.exception.ErrorCode;
import com.dsg.wardstudy.exception.WSApiException;
import com.dsg.wardstudy.repository.reservation.ReservationRepository;
import com.dsg.wardstudy.repository.reservation.RoomRepository;
import com.dsg.wardstudy.repository.studyGroup.StudyGroupRepository;
import com.dsg.wardstudy.repository.user.UserGroupRepository;
import com.dsg.wardstudy.repository.user.UserRepository;
import com.dsg.wardstudy.type.UserType;
import com.dsg.wardstudy.utils.TimeParsingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.dsg.wardstudy.config.redis.RedisCacheKey.RESERVATION_LIST;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService{

    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    @CacheEvict(key = "#roomId", value = RESERVATION_LIST, cacheManager = "redisCacheManager")
    @Transactional
    @Override
    public ReservationDetails create(Long studyGroupId, Long roomId, ReservationCreateRequest reservationRequest) {

        Reservation reservation = validateCreateRequest(studyGroupId, roomId, reservationRequest);
        Reservation saveReservation = reservationRepository.save(reservation);
        return ReservationDetails.mapToDto(saveReservation);

    }

    private Reservation validateCreateRequest(
            Long studyGroupId,
            Long roomId,
            ReservationCreateRequest reservationRequest) {

        ValidateFindByIdDto validateFindByIdDto = validateFindById(reservationRequest.getUserId(), studyGroupId, roomId);

        userGroupRepository.findUserTypeByUserIdAndSGId(
                reservationRequest.getUserId(), studyGroupId)
                .ifPresent(userType -> {
                    if (!userType.equals(UserType.L)) {
                        log.error("userType이 Leader가 아닙니다.");
                        throw new WSApiException(ErrorCode.INVALID_REQUEST, "Reservation registration is possible only if the user is the leader.");
                    }
                });
        Reservation reservation = ReservationCreateRequest.mapToEntity(
                reservationRequest
                , validateFindByIdDto.getUser()
                , validateFindByIdDto.getStudyGroup()
                , validateFindByIdDto.getRoom()
        );

        // 중복 reservation 체크
        reservationRepository.findByIdLock(reservation.getId()).ifPresent( r -> {
            log.error("Same reservationId is existing, can't make the reservation");
            throw new WSApiException(ErrorCode.DUPLICATED_ENTITY, "The same reservation exists.");
        });

        return reservation;
    }

    private ValidateFindByIdDto validateFindById(Long userId, Long studyGroupId, Long roomId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("user 대상이 없습니다. userId: {}", userId);
                    throw new WSApiException(ErrorCode.NO_FOUND_ENTITY, "can't find a User by " +
                            " userId: " + userId);
                });
        StudyGroup studyGroup = studyGroupRepository.findById(studyGroupId)
                .orElseThrow(() -> {
                    log.error("studyGroup 대상이 없습니다. studyGroupId: {}", studyGroupId);
                    throw new WSApiException(ErrorCode.NO_FOUND_ENTITY, "can't find a StudyGroup by " +
                            " studyGroupId: " + studyGroupId);
                });
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> {
                    log.error("room 대상이 없습니다. roomId: {}", roomId);
                    throw new WSApiException(ErrorCode.NO_FOUND_ENTITY, "can't find a Room by " +
                            " roomId: " + roomId);
                });

        return ValidateFindByIdDto.builder()
                .user(user)
                .studyGroup(studyGroup)
                .room(room)
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReservationDetails> getAllByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("user 대상이 없습니다. userId: {}", userId);
                    throw new WSApiException(ErrorCode.NO_FOUND_ENTITY, "can't find a User by " +
                            " userId: " + userId);
                });
        List<Long> sgIds = userGroupRepository.findSgIdsByUserId(user.getId());

        return reservationRepository.findByStudyGroupIds(sgIds).stream()
                .map(ReservationDetails::mapToDto)
                .collect(Collectors.toList());

    }

    @Transactional(readOnly = true)
    @Override
    public List<ReservationDetails> getByRoomIdAndTimePeriod(Long roomId, String startTime, String endTime) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> {
                    log.error("room 대상이 없습니다. roomId: {}", roomId);
                    throw new WSApiException(ErrorCode.NO_FOUND_ENTITY, "can't find a Room by " +
                            " roomId: " + roomId);
                });

        LocalDateTime sTime = TimeParsingUtils.formatterLocalDateTime(startTime);
        LocalDateTime eTime = TimeParsingUtils.formatterLocalDateTime(endTime);

        return reservationRepository.findByRoomIdAndTimePeriod(room.getId(), sTime, eTime).stream()
                .map(ReservationDetails::mapToDto)
                .collect(Collectors.toList());

    }

    @Cacheable(key = "#roomId", value = RESERVATION_LIST, cacheManager = "redisCacheManager")
    @Transactional(readOnly = true)
    @Override
    public List<ReservationDetails> getByRoomId(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> {
                    log.error("room 대상이 없습니다. roomId: {}", roomId);
                    throw new WSApiException(ErrorCode.NO_FOUND_ENTITY, "can't find a Room by " +
                            " roomId: " + roomId);
                });

        return reservationRepository.findByRoomId(room.getId()).stream()
                .map(ReservationDetails::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public ReservationDetails getByRoomIdAndReservationId(Long roomId, String reservationId) {
        Reservation reservation = reservationRepository.findByRoomIdAndId(roomId, reservationId)
                .orElseThrow(() -> {
                    log.error("reservation 대상이 없습니다. roomId: {}, reservationId: {}", roomId, reservationId);
                    throw new WSApiException(
                            ErrorCode.NO_FOUND_ENTITY,  "can't find a reservation by " + "roomId: " +  roomId +
                            " and reservationId: " + reservationId);
                });
        return ReservationDetails.mapToDto(reservation);
    }

    @Transactional
    @Override
    public String updateById(Long roomId, String reservationId, ReservationUpdateRequest reservationRequest) {
        // update 로직 변경 : find old -> save new -> delete old
        Reservation oldReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("reservation 대상이 없습니다. reservationId: {}", reservationId);
                    throw new WSApiException(ErrorCode.NO_FOUND_ENTITY, "can't find a reservation by " +
                            " reservation id: " + reservationId);
                });

        Reservation newReservation = validateUpdateRequest(roomId, reservationRequest);
        Reservation updatedReservation = reservationRepository.save(newReservation);
        reservationRepository.delete(oldReservation);

        return updatedReservation.getId();
    }


    private Reservation validateUpdateRequest(Long roomId, ReservationUpdateRequest reservationRequest) {

        ValidateFindByIdDto validateFindByIdDto = validateFindById(reservationRequest.getUserId(),
                reservationRequest.getStudyGroupId(),
                roomId);

        userGroupRepository.findUserTypeByUserIdAndSGId(
                reservationRequest.getUserId(), reservationRequest.getStudyGroupId())
                .ifPresent(userType -> {
                    if (!userType.equals(UserType.L)) {
                        log.error("userType이 Leader가 아닙니다.");
                        throw new WSApiException(ErrorCode.INVALID_REQUEST, "Reservation modification is possible only if the user is the leader.");
                    }
                });

        Reservation newReservation = Reservation.builder()
                .startTime(TimeParsingUtils.formatterLocalDateTime(reservationRequest.getStartTime()))
                .endTime(TimeParsingUtils.formatterLocalDateTime(reservationRequest.getEndTime()))
                .user(validateFindByIdDto.getUser())
                .studyGroup(validateFindByIdDto.getStudyGroup())
                .room(validateFindByIdDto.getRoom())
                .build();

        reservationRepository.findById(newReservation.getId()).ifPresent( r -> {
            log.error("Same reservationId is existing, can't make the reservation");
            throw new WSApiException(ErrorCode.DUPLICATED_ENTITY, "The same reservation exists.");
        });
        
        log.info("newReservation: {}", newReservation);

        return newReservation;
    }

    @Transactional
    @Override
    public void deleteById(Long userId, String reservationId) {
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("user 대상이 없습니다. userId: {}", userId);
                    throw new WSApiException(ErrorCode.NO_FOUND_ENTITY, "can't find a User by userId: " + userId);
                });
        // 해당 register 인지 validate
        reservationRepository.findById(reservationId)
                .ifPresent(reservation -> {
                    log.info("해당 reservation register: {}", reservation.getUser().getId());
                    if (reservation.getUser().getId().equals(userId)) {
                        reservationRepository.delete(reservation);
                    } else {
                        log.error("해당 reservation register가 아닙니다.");
                        throw new WSApiException(ErrorCode.INVALID_REQUEST, "Reservation modification is possible only if the user is the register.");
                    }
                });
    }

    @Transactional
    @Override
    public void changeIsEmailSent(Reservation reservation) {
        reservation.changeIsEmailSent(true);
        log.info("reservation.changeIsEmailSent(true); 실행");
    }

}
