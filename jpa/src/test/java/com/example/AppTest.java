package com.example;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {

    @Test
    void testMain() {
        // 標準出力をキャプチャするためのストリームを作成
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        // mainメソッドを呼び出す
        App.main(new String[]{});

        // 標準出力を元に戻す
        System.setOut(originalOut);

        // 出力結果を検証
        String output = outputStream.toString().trim();
        assertEquals("Hello World!", output);
    }
}
