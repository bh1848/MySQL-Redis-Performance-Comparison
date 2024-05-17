package com.thesisTest.thesisTest.testMysql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootApplication
public class TestMysql implements CommandLineRunner {

    private static final int COUNT = 10000; //총 작업 수
    private static final int BATCH_SIZE = 1000; //배치 당 작업 수

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        SpringApplication.run(TestMysql.class, args);
    }
    
    //배치 작업
    @Override
    public void run(String... args) {
        List<Double> batchInsertAverages = new ArrayList<>();
        List<Double> batchFetchAverages = new ArrayList<>();
        List<Double> batchDeleteAverages = new ArrayList<>();
        RowMapper<TestData> testDataMapper = (rs, rowNum) -> new TestData(rs.getInt("id"), rs.getString("name")); //ResultSet을 TestData 객체로 매핑하는 RowMapper

        log.info("배치 시작중...");

        for (int batch = 0; batch < COUNT / BATCH_SIZE; batch++) {
            log.info("배치 작동중 {} of {}", batch + 1, COUNT / BATCH_SIZE);
            List<Long> insertTimes = performBatchOperations(batch, "INSERT"); //삽입 실행 및 시간 측정
            List<Long> fetchTimes = performBatchOperations(batch, "SELECT", testDataMapper); //조회 실행 및 시간 측정
            List<Long> deleteTimes = performBatchOperations(batch, "DELETE"); //삭제 실행 및 시간 측정

            batchInsertAverages.add(average(insertTimes));
            batchFetchAverages.add(average(fetchTimes));
            batchDeleteAverages.add(average(deleteTimes));

            log.info("배치 실행 종료 {}.", batch + 1);
        }

        logBatchResults(batchInsertAverages, batchFetchAverages, batchDeleteAverages);
        log.info("모든 배치 종료.");
    }
    
    //작업 쿼리문
    @SafeVarargs
    private List<Long> performBatchOperations(int batch, String operationType, RowMapper<TestData>... mapper) {
        List<Long> operationTimes = new ArrayList<>(); //작업 시간을 저장할 리스트
        String sql;
        for (int i = 1; i <= BATCH_SIZE; i++) {
            int keyIndex = batch * BATCH_SIZE + i; //키 인덱스 계산
            switch (operationType) {
                case "INSERT":
                    sql = "INSERT INTO test_data (id, name, key_index) VALUES (?, ?, ?)";
                    operationTimes.add(executeUpdateSql(sql, keyIndex, "TestData" + keyIndex, keyIndex)); //삽입 쿼리
                    break;
                case "SELECT":
                    sql = "SELECT * FROM test_data WHERE id = ?";
                    operationTimes.add(executeQuerySql(sql, keyIndex, mapper[0])); //조회 쿼리
                    break;
                case "DELETE":
                    sql = "DELETE FROM test_data WHERE id = ?";
                    operationTimes.add(executeUpdateSql(sql, keyIndex)); //삭제 쿼리
                    break;
            }
        }
        return operationTimes;
    }

    private long executeUpdateSql(String sql, Object... params) {
        long startTime = System.currentTimeMillis();
        jdbcTemplate.update(sql, params);
        long duration = System.currentTimeMillis() - startTime;
        log.debug("Executed {} with parameters {} in {} ms", sql, params, duration);
        return duration;
    }

    private long executeQuerySql(String sql, Object param, RowMapper<TestData> mapper) {
        long startTime = System.currentTimeMillis();
        jdbcTemplate.queryForObject(sql, new Object[]{param}, mapper);
        long duration = System.currentTimeMillis() - startTime;
        log.debug("Executed {} with parameter {} in {} ms", sql, param, duration);
        return duration;
    }

    private void logBatchResults(List<Double> insertAverages, List<Double> fetchAverages, List<Double> deleteAverages) {
        for (int i = 0; i < insertAverages.size(); i++) {
            log.info("Batch {} - 평균 삽입 시간: {}ms, 평균 조회 시간: {}ms, 평균 삭제 시간: {}ms",
                    i + 1, insertAverages.get(i), fetchAverages.get(i), deleteAverages.get(i));
        }
    }
    
    //평균 시간 계산
    private double average(List<Long> times) {
        return times.stream().mapToLong(Long::longValue).average().orElse(0);
    }
}