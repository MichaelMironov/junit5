package org.example.dto;

import org.example.dao.UserDao;

import java.util.HashMap;
import java.util.Map;

public class UserDaoSpy extends UserDao {
    private final UserDao userDao;

    private final Map<Integer, Boolean> answers = new HashMap<>();

    public UserDaoSpy(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean delete(Integer userId) {
        return answers.getOrDefault(userId, userDao.delete(userId));
    }
}
