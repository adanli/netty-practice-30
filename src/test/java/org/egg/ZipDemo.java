package org.egg;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ZipDemo extends TestCase {
    public void testZipAndUnzip() throws Exception{
        // 压缩
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write("hello world!".getBytes(Charset.defaultCharset()));
        gzipOutputStream.close();
        byte[] zipped = byteArrayOutputStream.toByteArray();
        System.out.println(zipped);
        byteArrayOutputStream.close();

        // 解压
        ByteArrayInputStream inputStream = new ByteArrayInputStream(zipped);
        GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int length;
        byte[] bytes = new byte[1024];
        while ((length = gzipInputStream.read(bytes)) > 0) {
            outputStream.write(bytes, 0, length);
        }
        System.out.println(outputStream.toString(Charset.defaultCharset()));

    }
}
