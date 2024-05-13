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

                // 데이터 삽입: 각 회원 정보 생성 및 저장
                long insertStart = System.currentTimeMillis();
                TestData savedData = testDataRepository.save(new TestData(keyIndex, "TestData" + keyIndex));
                long insertEnd = System.currentTimeMillis();
                insertTimes.add(insertEnd - insertStart);

                // 데이터 조회: 저장된 회원 정보 검색
                long fetchStart = System.currentTimeMillis();
                testDataRepository.findById(savedData.getId());
                long fetchEnd = System.currentTimeMillis();
                fetchTimes.add(fetchEnd - fetchStart);

                // 데이터 삭제: 저장된 회원 정보 삭제
                long deleteStart = System.currentTimeMillis();
                testDataRepository.deleteById(savedData.getId());
                long deleteEnd = System.currentTimeMillis();
                deleteTimes.add(deleteEnd - deleteStart);
            }

            // 평균 시간 계산 및 저장
            batchInsertAverages.add(average(insertTimes));
            batchFetchAverages.add(average(fetchTimes));
            batchDeleteAverages.add(average(deleteTimes));
        }

        // 결과 로깅
        logBatchResults(batchInsertAverages, batchFetchAverages, batchDeleteAverages);
    }

    private void logBatchResults(List<Double> insertAverages, List<Double> fetchAverages, List<Double> deleteAverages) {
        for (int i = 0; i < insertAverages.size(); i++) {
            log.info("Batch {} - 평균 삽입 시간: {}ms, 평균 조회 시간: {}ms, 평균 삭제 시간: {}ms",
                    i + 1, insertAverages.get(i), fetchAverages.get(i), deleteAverages.get(i));
        }
    }

    private double average(List<Long> times) {
        return times.stream().mapToLong(Long::longValue).average().orElse(0);
    }
}