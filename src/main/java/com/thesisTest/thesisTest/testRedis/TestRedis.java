package com.thesisTest.thesisTest.testRedis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootApplication
public class TestRedis implements CommandLineRunner {

    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;

    private static final int COUNT = 10000;
    private static final int BATCH_SIZE = 1000; // 배치 크기를 1000으로 설정

    public static void main(String[] args) {
        SpringApplication.run(TestRedis.class, args);
    }

    @Override
    public void run(String... args) {
        List<Double> batchInsertAverages = new ArrayList<>();
        List<Double> batchFetchAverages = new ArrayList<>();
        List<Double> batchDeleteAverages = new ArrayList<>();

        for (int batch = 0; batch < COUNT / BATCH_SIZE; batch++) {
            List<Long> redisInsertTimes = new ArrayList<>();
            List<Long> redisFetchTimes = new ArrayList<>();
            List<Long> redisDeleteTimes = new ArrayList<>();

            for (int i = 1; i <= BATCH_SIZE; i++) {
                int keyIndex = batch * BATCH_SIZE + i;

                // 회원가입 - 데이터 삽입
                long insertStart = System.currentTimeMillis();
                redisTemplate.opsForValue().set("user:" + keyIndex, keyIndex);
                redisInsertTimes.add(System.currentTimeMillis() - insertStart);

                // 로그인 - 데이터 조회
                long fetchStart = System.currentTimeMillis();
                redisTemplate.opsForValue().get("user:" + keyIndex);
                redisFetchTimes.add(System.currentTimeMillis() - fetchStart);

                // 회원 탈퇴 - 데이터 삭제
                long deleteStart = System.currentTimeMillis();
                redisTemplate.delete("user:" + keyIndex);
                redisDeleteTimes.add(System.currentTimeMillis() - deleteStart);
            }

            // 각 배치의 평균 시간을 계산하고 저장
            batchInsertAverages.add(average(redisInsertTimes));
            batchFetchAverages.add(average(redisFetchTimes));
            batchDeleteAverages.add(average(redisDeleteTimes));
        }

        // 각 배치의 평균 시간을 로그로 출력
        for (int i = 0; i < batchInsertAverages.size(); i++) {
            log.info("배치 {} 평균 삽입 시간: {}ms", i + 1, batchInsertAverages.get(i));
            log.info("배치 {} 평균 조회 시간: {}ms", i + 1, batchFetchAverages.get(i));
            log.info("배치 {} 평균 삭제 시간: {}ms", i + 1, batchDeleteAverages.get(i));
        }
    }

    private double average(List<Long> times) {
        return times.stream().mapToLong(Long::longValue).average().orElse(0);
    }
}