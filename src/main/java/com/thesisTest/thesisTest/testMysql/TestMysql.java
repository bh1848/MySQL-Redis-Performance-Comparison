package com.thesisTest.thesisTest.testMysql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootApplication
public class TestMysql implements CommandLineRunner {

    @Autowired
    private TestDataRepository testDataRepository;

    private static final int COUNT = 10000;
    private static final int BATCH_SIZE = 1000; // 배치 크기를 1000으로 설정

    public static void main(String[] args) {
        SpringApplication.run(TestMysql.class, args);
    }

    @Override
    public void run(String... args) {
        List<Double> batchInsertAverages = new ArrayList<>();
        List<Double> batchFetchAverages = new ArrayList<>();
        List<Double> batchDeleteAverages = new ArrayList<>();

        for (int batch = 0; batch < COUNT / BATCH_SIZE; batch++) {
            List<Long> insertTimes = new ArrayList<>();
            List<Long> fetchTimes = new ArrayList<>();
            List<Long> deleteTimes = new ArrayList<>();

            for (int i = 1; i <= BATCH_SIZE; i++) {
                int keyIndex = batch * BATCH_SIZE + i;

                // 회원가입 - 데이터 삽입
                long insertStart = System.currentTimeMillis();
                TestData savedData = testDataRepository.save(new TestData(keyIndex, "TestData" + keyIndex));
                long insertEnd = System.currentTimeMillis();
                insertTimes.add(insertEnd - insertStart);

                // 로그인 - 데이터 조회
                long fetchStart = System.currentTimeMillis();
                testDataRepository.findById(savedData.getId());
                long fetchEnd = System.currentTimeMillis();
                fetchTimes.add(fetchEnd - fetchStart);

                // 회원 탈퇴 - 데이터 삭제
                long deleteStart = System.currentTimeMillis();
                testDataRepository.deleteById(savedData.getId());
                long deleteEnd = System.currentTimeMillis();
                deleteTimes.add(deleteEnd - deleteStart);
            }

            // 각 배치의 평균 시간을 계산하고 저장
            batchInsertAverages.add(average(insertTimes));
            batchFetchAverages.add(average(fetchTimes));
            batchDeleteAverages.add(average(deleteTimes));
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