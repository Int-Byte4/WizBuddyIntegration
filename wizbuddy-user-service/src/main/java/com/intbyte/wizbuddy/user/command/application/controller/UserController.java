package com.intbyte.wizbuddy.user.command.application.controller;

import com.intbyte.wizbuddy.user.command.application.service.EmailVerificationService;
import com.intbyte.wizbuddy.user.command.domain.aggregate.vo.EmailVerificationVO;
import com.intbyte.wizbuddy.user.command.domain.aggregate.vo.ResponseEmailMessageVO;
import com.intbyte.wizbuddy.user.command.domain.aggregate.vo.request.*;
import com.intbyte.wizbuddy.user.common.exception.CommonException;
import com.intbyte.wizbuddy.user.common.exception.ResponseDTO;
import com.intbyte.wizbuddy.user.common.exception.StatusEnum;
import com.intbyte.wizbuddy.user.query.dto.KakaoUserDTO;
import com.intbyte.wizbuddy.user.security.JwtUtil;
import com.intbyte.wizbuddy.user.command.application.dto.RequestEditUserDTO;
import com.intbyte.wizbuddy.user.command.application.service.UserService;
import com.intbyte.wizbuddy.user.command.domain.aggregate.vo.response.ResponseInsertUserVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController("userCommandController")
@RequestMapping("users")
@Slf4j
public class UserController {

    private JwtUtil jwtUtil;
    private Environment env;
    private ModelMapper modelMapper;
    private UserService userService;

    @Autowired
    private EmailVerificationService emailVerificationService;


    @Autowired
    public UserController(JwtUtil jwtUtil, Environment env, ModelMapper modelMapper, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.env = env;
        this.modelMapper = modelMapper;
        this.userService = userService;
    }

    @Operation(summary = "회원가입")
    @PostMapping("/register")
    public ResponseEntity<ResponseInsertUserVO> registerUser(@RequestBody RequestRegisterUserVO request) {
        ResponseInsertUserVO responseUser = userService.signInUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }

    @Operation(summary = "회원 정보 수정")
    @PatchMapping("/{userCode}/edit")
    public ResponseEntity<Void> modifyUser(@PathVariable("userCode") String userCode, @RequestParam String userPassword, @RequestParam String userPhone) {
        RequestEditUserDTO userDTO = RequestEditUserDTO.builder()
                .userPassword(userPassword)
                .userPhone(userPhone)
                .build();

        userService.modifyUser(userCode, userDTO);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "회원 탈퇴 요청")
    @PatchMapping("/{userCode}/delete")
    public ResponseEntity<Void> deleteEmployer(@PathVariable("userCode") String userCode) {
        userService.deleteUser(userCode);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //설명. 이메일 전송 API (회원가입전 실행)
    @PostMapping("/verification-email")
    public ResponseDTO<?> sendVerificationEmail(@RequestBody @Validated EmailVerificationVO request) {
        try {
            emailVerificationService.sendVerificationEmail(request.getEmail());

            ResponseEmailMessageVO responseEmailMessageVO =new ResponseEmailMessageVO();
            responseEmailMessageVO.setMessage("인증 코드가 이메일로 전송되었습니다.");
            return ResponseDTO.ok(responseEmailMessageVO);
        } catch (Exception e) {
            return ResponseDTO.fail(new CommonException(StatusEnum.INTERNAL_SERVER_ERROR));
        }
    }

    //설명. 이메일 인증번호 검증 API (회원가입전 실행)
    @PostMapping("/verification-email/confirmation")
    public ResponseDTO<?> verifyEmail(@RequestBody @Validated EmailVerificationVO request) {
        boolean isVerified = emailVerificationService.verifyCode(request.getEmail(), request.getCode());

        ResponseEmailMessageVO responseEmailMessageVO =new ResponseEmailMessageVO();
        responseEmailMessageVO.setMessage("이메일 인증이 완료되었습니다.");
        if (isVerified) {
            return ResponseDTO.ok(responseEmailMessageVO);
        } else {
            return ResponseDTO.fail(new CommonException(StatusEnum.INVALID_VERIFICATION_CODE));
        }
    }

    @GetMapping("/oauth2/kakao")
    public ResponseEntity<Map<String, Object>> handleKakaoLogin(@RequestParam String code) {

        log.info("넘어온 카카오 코드 값 확인: {}", code);

        KakaoUserDTO userDTO = userService.processKakaoUser(code);

        log.info("결과적으로 user에 담긴 값: {}", userDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("userCode", userDTO.getUserCode());
        response.put("email", userDTO.getUserEmail());
        response.put("name", userDTO.getUserName());
        response.put("jwtToken", userDTO.getJwtToken());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}