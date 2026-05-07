package com.longtou.userservice.controller;

import com.longtou.commonweb.exception.BusinessException;
import com.longtou.commoncore.constant.ErrorCode;
import com.longtou.commoncore.result.Result;
import com.longtou.commoncore.utils.UserContext;
import com.longtou.userservice.domain.dto.LoginDTO;
import com.longtou.userservice.domain.dto.RegisterDTO;
import com.longtou.userservice.domain.vo.UserVO;
import com.longtou.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginDTO loginDTO){


        String token = userService.login(loginDTO);

        return Result.success(token);

    }

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterDTO registerDTO) {
        // 校验两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            return Result.fail("两次输入的密码不一致");
        }
        String token = userService.register(registerDTO);
        return Result.success(token);
    }

    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        UserVO vo = userService.getUserInfo(userId);
        return Result.success(vo);
    }
}
