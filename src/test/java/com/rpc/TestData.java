package com.rpc;

import java.io.Serializable;
import java.util.Objects;

public class TestData implements Serializable {
    private int id;
    private String name;

    public TestData() {
    }

    public TestData(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getter 和 Setter
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // 重写 equals 和 hashCode 用于比较对象内容
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestData)) return false;
        TestData testData = (TestData) o;
        return id == testData.id && Objects.equals(name, testData.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "TestData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
