package com.songsy.iframe.service.impl;


import com.songsy.iframe.core.persistence.provider.mapper.BaseCurdMapper;
import com.songsy.iframe.core.persistence.provider.service.AbstractBaseService;
import com.songsy.iframe.mapper.UserMapper;
import com.songsy.iframe.model.User;
import com.songsy.iframe.service.UserMasterDataService;
import com.songsy.iframe.service.UserService;
import com.songsy.iframe.service.UserSlaveDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author songshuiyang
 * @date 2018/10/28 10:13
 */
@Service
public class UserServiceImpl extends AbstractBaseService<User, Integer> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserMasterDataService userMasterService;

    @Autowired
    private UserSlaveDataService userSlaveDataService;

    @Override
    public BaseCurdMapper<User, Integer> getRepository() {
        return userMapper;
    }

    @Override
    public void updateAllUser(User user) {
        userMasterService.updateMasterDatabase(user);
        // System.out.println();
        userSlaveDataService.updateSlaveDatabase(user);
    }

    @Override
    public void updateMasterDatabase(User user) {
        userMapper.insert(user);
    }

    @Override
    public void updateSlaveDatabase(User user) {
        userMapper.insert(user);
    }

}
