package com.longtou.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.longtou.userservice.domain.dto.LoginDTO;
import com.longtou.userservice.domain.dto.RegisterDTO;
import com.longtou.userservice.domain.entity.User;
import com.longtou.userservice.domain.vo.UserVO;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

@Service
public interface UserService  extends IService<User> {
    String login(@Valid LoginDTO loginDTO);

    String register(@Valid RegisterDTO registerDTO);

    UserVO getUserInfo(Long userId);
}
