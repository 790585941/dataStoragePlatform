package com.zyh432.core.utils;

import cn.hutool.core.date.DateUtil;
import com.zyh432.core.constants.DataStoragePlatformConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文件相关的工具类
 */
public class FileUtil {
    public static String getFileSuffix(String fileName) {
        if(StringUtils.isBlank(fileName) || fileName.indexOf(DataStoragePlatformConstants.POINT_STR)==DataStoragePlatformConstants.MINUS_ONE_INT){
            return StringUtils.EMPTY;
        }
        return fileName.substring(fileName.lastIndexOf(DataStoragePlatformConstants.POINT_STR));

    }

    /**
     * 通过文件大小转化文件大小的展示名称
     * @param totalSize
     * @return
     */
    public static String byteCountToDisplaySize(Long totalSize) {
        if (Objects.isNull(totalSize)) {
            return DataStoragePlatformConstants.EMPTY_STR;
        }
        return org.apache.commons.io.FileUtils.byteCountToDisplaySize(totalSize);
    }

    /**
     * 批量删除物理文件
     * @param realFilePathList
     */
    public static void deleteFiles(List<String> realFilePathList) throws IOException {
        if (CollectionUtils.isEmpty(realFilePathList)){
            return;
        }
        for (String realFilePath : realFilePathList) {
            org.apache.commons.io.FileUtils.forceDelete(new File(realFilePath));
        }
    }

    /**
     * 生成文件的存储路径
     *
     * 生成规则：基础路径 + 年 + 月 + 日 + 随机的文件名称
     * @param basePath
     * @param filename
     * @return
     */
    public static String generateStoreFileRealPath(String basePath, String filename) {
        return new StringBuffer(basePath)
                .append(File.separator)
                .append(DateUtil.thisYear())
                .append(File.separator)
                .append(DateUtil.thisMonth() + 1)
                .append(File.separator)
                .append(DateUtil.thisDayOfMonth())
                .append(File.separator)
                .append(UUIDUtil.getUUID())
                .append(getFileSuffix(filename))
                .toString();
    }

    /**
     * 将文件的输入流写入到文件中
     * 使用底层的sendfile零拷贝来提高传输效率
     * @param inputStream
     * @param targetFile
     * @param totalSize
     */
    public static void writeStream2File(InputStream inputStream, File targetFile, Long totalSize) throws IOException{
        createFile(targetFile);
        RandomAccessFile randomAccessFile = new RandomAccessFile(targetFile, "rw");
        FileChannel outputChannel = randomAccessFile.getChannel();
        ReadableByteChannel inputChannel = Channels.newChannel(inputStream);
        outputChannel.transferFrom(inputChannel, 0L, totalSize);
        inputChannel.close();
        outputChannel.close();
        randomAccessFile.close();
        inputStream.close();
    }

    /**
     * 创建文件
     * 包含父文件一起视情况去创建
     *
     * @param targetFile
     */
    public static void createFile(File targetFile) throws IOException {
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        targetFile.createNewFile();
    }

    /**
     * 生成默认的文件存储路径
     *
     * 生成规则：当前登录用户的文件目录+datastorageplatform
     * @return
     */
    public static String generateDefaultStoreFileRealPath() {
        return new StringBuffer("D:")
                .append(File.separator)
                .append("datastorageplatform")
                .toString();
    }

    /**
     * 生成默认的文件分片的存储路径前缀
     * @return
     */
    public static String generateDefaultStoreFileChunkRealPath() {
        return new StringBuffer("D:")
                .append(File.separator)
                .append("datastorageplatform")
                .append(File.separator)
                .append("chunks")
                .toString();
    }

    /**
     *生成文件分片的存储路径
     * 生成规则：基础路径 + 年 + 月 + 日 + 唯一标识 + 随机的文件名称 + __,__ + 文件分片的下标
     * @param basePath
     * @param identifier
     * @param chunkNumber
     * @return
     */
    public static String generateStoreFileChunkRealPath(String basePath, String identifier, Integer chunkNumber) {
        return new StringBuffer(basePath)
                .append(File.separator)
                .append(DateUtil.thisYear())
                .append(File.separator)
                .append(DateUtil.thisMonth() + 1)
                .append(File.separator)
                .append(DateUtil.thisDayOfMonth())
                .append(File.separator)
                .append(identifier)
                .append(File.separator)
                .append(UUIDUtil.getUUID())
                .append(DataStoragePlatformConstants.COMMON_SEPARATOR)
                .append(chunkNumber)
                .toString();
    }

    /**
     * 追加写文件
     *
     * @param target
     * @param source
     */
    public static void appendWrite(Path target, Path source) throws IOException {
        Files.write(target, Files.readAllBytes(source), StandardOpenOption.APPEND);
    }


    /**
     * 利用零拷贝技术读取文件内容并写入文件的输出流中
     * @param fileInputStream
     * @param outputStream
     * @param length
     * @throws IOException
     */
    public static void writeFile2OutputStream(FileInputStream fileInputStream, OutputStream outputStream, long length) throws IOException{
        FileChannel fileChannel = fileInputStream.getChannel();
        WritableByteChannel writableByteChannel = Channels.newChannel(outputStream);
        fileChannel.transferTo(DataStoragePlatformConstants.ZERO_LONG, length, writableByteChannel);
        outputStream.flush();
        fileInputStream.close();
        outputStream.close();
        fileChannel.close();
        writableByteChannel.close();
    }


    /**
     * 获取文件的类型
     *
     * @param filename
     * @return
     */
    public static String getFileExtName(String filename) {
        if (StringUtils.isBlank(filename) || filename.lastIndexOf(DataStoragePlatformConstants.POINT_STR) == DataStoragePlatformConstants.MINUS_ONE_INT) {
            return DataStoragePlatformConstants.EMPTY_STR;
        }
        return filename.substring(filename.lastIndexOf(DataStoragePlatformConstants.POINT_STR) + DataStoragePlatformConstants.ONE_INT).toLowerCase();
    }



    /**
     * 普通的流对流数据传输
     *
     * @param inputStream
     * @param outputStream
     */
    public static void writeStream2StreamNormal(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != DataStoragePlatformConstants.MINUS_ONE_INT) {
            outputStream.write(buffer, DataStoragePlatformConstants.ZERO_INT, len);
        }
        outputStream.flush();
        inputStream.close();
        outputStream.close();
    }
}
