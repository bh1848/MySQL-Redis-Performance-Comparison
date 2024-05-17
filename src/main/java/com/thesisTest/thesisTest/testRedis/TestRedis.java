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

    private static final int COUNT = 10000; //총 작업 수
    private static final int BATCH_SIZE = 1000; //배치 당 작업 수
    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(TestRedis.class, args);
    }

    @Override
    public void run(String... args) {
        List<Double> batchInsertAverages = new ArrayList<>();
        List<Double> batchFetchAverages = new ArrayList<>();
        List<Double> batchDeleteAverages = new ArrayList<>();

        //배치 작업 반복 수행
        for (int batch = 0; batch < COUNT / BATCH_SIZE; batch++) {
            log.info("배치 작동중 {} of {}", batch + 1, COUNT / BATCH_SIZE);
            List<Long> insertTimes = performBatchOperations(batch, "INSERT"); //삽입 실행 및 시간 측정
            List<Long> fetchTimes = performBatchOperations(batch, "SELECT"); //조회 실행 및 시간 측정
            List<Long> deleteTimes = performBatchOperations(batch, "DELETE"); //삭제 실행 및 시간 측정

            batchInsertAverages.add(average(insertTimes));
            batchFetchAverages.add(average(fetchTimes));
            batchDeleteAverages.add(average(deleteTimes));

            log.info("배치 실행 종료 {}.", batch + 1);
        }

        logBatchResults(batchInsertAverages, batchFetchAverages, batchDeleteAverages);
        log.info("모든 배치 종료.");
    }

    private List<Long> performBatchOperations(int batch, String operationType) {
        List<Long> operationTimes = new ArrayList<>(); //작업 시간을 저장할 리스트
        for (int i = 1; i <= BATCH_SIZE; i++) {
            int keyIndex = batch * BATCH_SIZE + i; //키 인덱스 계산
            String key = "user:" + keyIndex; //Redis 키 생성
            switch (operationType) {
                case "INSERT":
                    operationTimes.add(executeRedisCommand(() -> redisTemplate.opsForValue().set(key, keyIndex))); //삽입 명령어
                    break;
                case "SELECT":
                    operationTimes.add(executeRedisCommand(() -> redisTemplate.opsForValue().get(key))); //조회 명령어
                    break;
                case "DELETE":
                    operationTimes.add(executeRedisCommand(() -> redisTemplate.delete(key))); //삭제 명령어
                    break;
            }
        }
        return operationTimes;
    }
    
    //배치 당 시간 계산
    private long executeRedisCommand(Runnable command) {
        long startTime = System.currentTimeMillis();
        command.run();
        long duration = System.currentTimeMillis() - startTime;
        log.debug("Executed command in {} ms", duration);
        return duration;
    }

    private void logBatchResults(List<Double> insertAverages, List<Double> fetchAverages, List<Double> deleteAverages) {
        for (int i = 0; i < insertAverages.size(); i++) {
            log.info("배치 {} 평균 삽입 시간: {}ms, 평균 조회 시간: {}ms, 평균 삭제 시간: {}ms",
                    i + 1, insertAverages.get(i), fetchAverages.get(i), deleteAverages.get(i));
        }
    }
    
    //평균 시간 계산
    private double average(List<Long> times) {
        return times.stream().mapToLong(Long::longValue).average().orElse(0);
    }
}