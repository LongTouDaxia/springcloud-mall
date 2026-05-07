package com.longtou.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longtou.commoncore.utils.JwtUtils;
import com.longtou.commonweb.exception.BusinessException;
import com.longtou.commoncore.constant.ErrorCode;

import com.longtou.userservice.domain.dto.LoginDTO;
import com.longtou.userservice.domain.dto.RegisterDTO;
import com.longtou.userservice.domain.entity.User;
import com.longtou.userservice.domain.vo.UserVO;
import com.longtou.userservice.mapper.UserMapper;
import com.longtou.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImp extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;

    private final JwtUtils jwtUtils;

    @Override
    public String login(LoginDTO loginDTO) {
        //查数据库
        User user = query().eq("username", loginDTO.getUsername())
                .eq("password", loginDTO.getPassword()).one();
        if(user == null){
            //用户不存在
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }
        //存在发放token
        String token = jwtUtils.createToken(user.getUsername(), user.getId().toString());
        return token;
    }

    @Override
    public String register(RegisterDTO registerDTO) {
        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerDTO.getUsername());
        if (this.count(wrapper) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // 2. 创建新用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        // 加密密码
        user.setPassword(registerDTO.getPassword());
        user.setPhone(registerDTO.getPhone());


        // 3. 保存到数据库
        this.save(user);

        // 4. 生成 token
        return jwtUtils.createToken(user.getUsername(), user.getId().toString());
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null){
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        return userVO;
    }
}
