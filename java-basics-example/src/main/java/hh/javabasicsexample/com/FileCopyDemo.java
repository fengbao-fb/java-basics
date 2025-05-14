package hh.javabasicsexample.com;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;

/**
 * @author HuangHao
 * @since 2025/5/14 14:00
 * @des Java 文件拷贝5种方式
 */
public class FileCopyDemo {

    public static void main(String[] args) {
        //1. FileInputStream + FileOutputStream
        try (FileInputStream fis = new FileInputStream("D:\\1.xlsx");
             FileOutputStream fos = new FileOutputStream("D:\\copy1.xlsx")) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            System.out.println("文件拷贝成功！");

        } catch (IOException e) {
            System.err.println("文件拷贝失败: " + e.getMessage());
            e.printStackTrace();
        }
        //2.BufferedInputStream + BufferedOutputStream
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream("D:\\1.xlsx"));
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("D:\\copy2.xlsx"))) {

            byte[] buffer = new byte[8192]; // 8KB缓冲区，默认是8192字节
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            System.out.println("文件拷贝成功！");

        } catch (IOException e) {
            System.err.println("文件拷贝失败: " + e.getMessage());
            e.printStackTrace();
        }
        //3. NIO Files.copy()
        try {
            Path sourcePath = Paths.get("D:\\1.xlsx");
            Path targetPath = Paths.get("D:\\copy3.xlsx");

            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("文件拷贝成功！");

        } catch (IOException e) {
            System.err.println("文件拷贝失败: " + e.getMessage());
            e.printStackTrace();
        }
        //4.NIO FileChannel
        try (FileChannel sourceChannel = FileChannel.open(Paths.get("D:\\1.xlsx"), StandardOpenOption.READ);
             FileChannel targetChannel = FileChannel.open(Paths.get("D:\\copy4.xlsx"),
                     StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            // 使用 transferTo 方法进行高效传输
            long position = 0;
            long count = sourceChannel.size();
            sourceChannel.transferTo(position, count, targetChannel);

            System.out.println("文件拷贝成功！");

        } catch (IOException e) {
            System.err.println("文件拷贝失败: " + e.getMessage());
            e.printStackTrace();
        }
        //5.内存映射文件拷贝
        try (RandomAccessFile sourceFile = new RandomAccessFile("D:\\1.xlsx", "r");
             RandomAccessFile targetFile = new RandomAccessFile("D:\\copy5.xlsx", "rw")) {
            FileChannel sourceChannel = sourceFile.getChannel();
            MappedByteBuffer buffer = sourceChannel.map(FileChannel.MapMode.READ_ONLY, 0, sourceChannel.size());
            targetFile.getChannel().write(buffer);
            System.out.println("文件拷贝成功！");
        } catch (IOException e) {
            System.err.println("文件拷贝失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
