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

    private static final int COUNT = 10000; // 전체 테스트 횟수
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
                String key = "user:" + keyIndex;

                // 데이터 삽입: 각 회원 정보 생성 및 저장. keyIndex를 key로 사용하여 정수값을 저장합니다.
                long insertStart = System.currentTimeMillis();
                redisTemplate.opsForValue().set(key, keyIndex);
                long insertTime = System.currentTimeMillis() - insertStart;
                redisInsertTimes.add(insertTime);

                // 데이터 조회: 저장된 회원 정보 검색. 저장된 keyIndex 값을 조회합니다.
                long fetchStart = System.currentTimeMillis();
                redisTemplate.opsForValue().get(key);
                long fetchTime = System.currentTimeMillis() - fetchStart;
                redisFetchTimes.add(fetchTime);

                // 데이터 삭제: 저장된 회원 정보 삭제. key를 사용하여 데이터를 삭제합니다.
                long deleteStart = System.currentTimeMillis();
                redisTemplate.delete(key);
                long deleteTime = System.currentTimeMillis() - deleteStart;
                redisDeleteTimes.add(deleteTime);
            }

            // 각 배치의 평균 시간을 계산하고 저장
            batchInsertAverages.add(average(redisInsertTimes));
            batchFetchAverages.add(average(redisFetchTimes));
            batchDeleteAverages.add(average(redisDeleteTimes));
        }

        // 결과 로깅
        logBatchResults(batchInsertAverages, batchFetchAverages, batchDeleteAverages);
    }

    private void logBatchResults(List<Double> insertAverages, List<Double> fetchAverages, List<Double> deleteAverages) {
        for (int i = 0; i < insertAverages.size(); i++) {
            log.info("배치 {} 평균 삽입 시간: {}ms, 평균 조회 시간: {}ms, 평균 삭제 시간: {}ms",
                    i + 1, insertAverages.get(i), fetchAverages.get(i), deleteAverages.get(i));
        }
    }

    private double average(List<Long> times) {
        return times.stream().mapToLong(Long::longValue).average().orElse(0);
    }
}