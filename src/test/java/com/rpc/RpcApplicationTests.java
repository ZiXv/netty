package com.rpc;
import com.rpc.protocol.constant.SerialType;
import com.rpc.serialization.ISerializer;
import com.rpc.serialization.JavaSerializer;
import com.rpc.serialization.JsonSerializer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class RpcApplicationTests {

    @Test
    public void testJavaSerializer() {
        // 创建 Java 序列化器实例
        ISerializer serializer = new JavaSerializer();
        // 构造测试数据
        TestData data = new TestData(1, "JavaTest");
        // 序列化对象
        byte[] bytes = serializer.serialize(data);
        // 反序列化为 TestData 对象
        TestData result = serializer.deserialize(bytes, TestData.class);
        // 断言反序列化后的对象与原始对象相等
        log.info("result:{}",result);
        log.info("data:{}",data);
        assertEquals(data, result);
        // 验证返回的序列化类型标识是否正确
        assertEquals(SerialType.JAVA_SERIAL.code(), serializer.getType());
    }

    @Test
    public void testJsonSerializer() {
        // 创建 JSON 序列化器实例
        ISerializer serializer = new JsonSerializer();
        // 构造测试数据
        TestData data = new TestData(2, "JsonTest");
        // 序列化对象
        byte[] bytes = serializer.serialize(data);
        // 反序列化为 TestData 对象
        TestData result = serializer.deserialize(bytes, TestData.class);
        // 断言反序列化后的对象与原始对象相等
        log.info("result:{}",result);
        log.info("data:{}",data);
        assertEquals(data, result);
        // 验证返回的序列化类型标识是否正确
        assertEquals(SerialType.JSON_SERIAL.code(), serializer.getType());
    }
    @Test
    public void testProtoBufSerializer() {
        // 创建 JSON 序列化器实例
        ISerializer serializer = new JsonSerializer();
        // 构造测试数据
        TestData data = new TestData(3, "ProtoBufTest");
        // 序列化对象
        byte[] bytes = serializer.serialize(data);
        // 反序列化为 TestData 对象
        TestData result = serializer.deserialize(bytes, TestData.class);
        // 断言反序列化后的对象与原始对象相等
        log.info("result:{}",result);
        log.info("data:{}",data);
        assertEquals(data, result);
        // 验证返回的序列化类型标识是否正确
        assertEquals(SerialType.JSON_SERIAL.code(), serializer.getType());
    }
}
