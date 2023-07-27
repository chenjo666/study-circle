package com.cj.studycirclebackend;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cj.studycirclebackend.StudyCircleBackendApplication;
import com.cj.studycirclebackend.dao.LetterMapper;
import com.cj.studycirclebackend.pojo.Letter;
import com.cj.studycirclebackend.pojo.User;
import com.cj.studycirclebackend.service.LetterService;
import com.cj.studycirclebackend.service.UserService;
import com.cj.studycirclebackend.vo.LetterOverviewVO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@SpringBootTest(classes = {StudyCircleBackendApplication.class})
public class LetterServiceTests {
    @Resource
    private LetterService letterService;

    @Resource
    private UserService userService;

    @Resource
    private LetterMapper letterMapper;

    @Test
    public void test() {
        List<Letter> letters = letterService.list(new QueryWrapper<Letter>()
                .select("user_from_id")
                .eq("user_to_id", 1677640320653533186L)
                .groupBy("user_from_id"));
        for (Letter letter : letters) {
            System.out.println(letter);
        }
    }

    @Test
    public void test2() {
        List<Long> list = letterMapper.getToUserIds(1677640320653533186L);
        list.forEach(System.out::println);
    }

//    @Test
//    public void insert() {
//        List<User> list = userService.list();
//        Random random = new Random();
//        List<Letter> letters = new ArrayList<>();
//        for (int i = 0; i < 100; i++) {
//            Letter letter = new Letter();
//            letter.setIsRead(0);
//            letter.setUserFromId(list.get(random.nextInt(list.size())).getId());
//            letter.setUserToId(1677640320653533186L);
//            letter.setContent("hello");
//            // 随机生成时间
//            LocalDateTime now = LocalDateTime.now();
//            LocalDateTime randomTime = now.minusDays(random.nextInt(365)).minusHours(random.nextInt(24))
//                    .minusMinutes(random.nextInt(60)).minusSeconds(random.nextInt(60));
//            letter.setSendTime(Date.from(randomTime.atZone(ZoneId.systemDefault()).toInstant()));
//            letters.add(letter);
//        }
//        letterService.saveBatch(letters);
//    }
}
