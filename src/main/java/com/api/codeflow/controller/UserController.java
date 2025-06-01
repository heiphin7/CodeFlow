package com.api.codeflow.controller;

import com.api.codeflow.dto.UpdateUserInfoDto;
import com.api.codeflow.dto.UserInfoDto;
import com.api.codeflow.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/getInfo/{username}")
    public ResponseEntity<?> getUserInfo(@PathVariable String username) {
        try {
            return ResponseEntity.ok(userService.getUserInfo(username));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error while getting userInfo: " + e.getMessage() + " stack trace: ", e);
            return new ResponseEntity<>("Server error:(", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateUserInfo(HttpServletRequest request,
                                            @RequestBody UpdateUserInfoDto dto) {
        try {
            return ResponseEntity.ok(userService.updateUserInfo(dto, request));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error while updating user info: " + e.getMessage());
            return new ResponseEntity<>("Server Error:(", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get/solvedTasks/{username}")
    public ResponseEntity<?> getUsersSolvedTasks(@PathVariable String username) {
        try {
            return ResponseEntity.ok(userService.getSolvedTasks(username));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error while getting solved tasks for " + username + " : " + e.getMessage());
            return new ResponseEntity<>("Server error:(", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
