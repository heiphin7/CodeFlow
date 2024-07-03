package com.codeflow.controller;

import com.codeflow.dto.UserDto;
import com.codeflow.exception.UsernameTakenException;
import com.codeflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/api/v1/save")
    private String save(@RequestBody UserDto userDto) {
        try {
            userService.save(userDto);
        } catch (UsernameTakenException e) {
            return "Имя пользователя занято!";
        } catch (Exception e) {
            // todo save to logs
            System.out.println(e.getMessage());
            return "Что то пошло не так!";
        }

        return "Success";
    }

    @PostMapping("/api/v1/authenticate")
    public String authenticate(@RequestBody UserDto userDto) {
        try {
            userService.authenticate(userDto);
        } catch (BadCredentialsException e) {
            return "bad credentionals";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Что то пошло не так";
        }

        return "Success";
    }
}
