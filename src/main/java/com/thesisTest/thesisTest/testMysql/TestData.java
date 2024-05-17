package com.thesisTest.thesisTest.testMysql;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter // Lombok을 사용하여 getter 메서드 자동 생성
@NoArgsConstructor
@AllArgsConstructor // 모든 매개변수를 받는 생성자 자동 생성
@Entity // JPA 엔티티 선언
public class TestData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int keyIndex;
    private String name;

    public TestData(int id, String name) {
        this.id = id;
        this.name = name;
    }
}