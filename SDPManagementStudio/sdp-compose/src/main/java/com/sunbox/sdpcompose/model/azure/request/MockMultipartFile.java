package com.sunbox.sdpcompose.model.azure.request;

import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

/**
 * @Description: multipart file
 * @Title: MockMultipartFile
 * @Package: com.sunbox.sdpcompose.model.azure.request
 * @Author: wangshihao
 * @Copyright: 版权
 * @CreateTime: 2022/12/9 11:53
 */
public class MockMultipartFile implements MultipartFile {

    private final String name;
    private String originalFilename;
    @Nullable
    private String contentType;
    private final byte[] content;

    public MockMultipartFile(String name, @Nullable byte[] content) {
        this(name, "", (String)null, (byte[])content);
    }

    public MockMultipartFile(String name, InputStream contentStream) throws IOException {
        this(name, "", (String)null, (byte[])FileCopyUtils.copyToByteArray(contentStream));
    }

    public MockMultipartFile(String name, @Nullable String originalFilename, @Nullable String contentType, @Nullable byte[] content) {
        Assert.hasLength(name, "Name must not be null");
        this.name = name;
        this.originalFilename = originalFilename != null ? originalFilename : "";
        this.contentType = contentType;
        this.content = content != null ? content : new byte[0];
    }

    public MockMultipartFile(String name, @Nullable String originalFilename, @Nullable String contentType, InputStream contentStream) throws IOException {
        this(name, originalFilename, contentType, FileCopyUtils.copyToByteArray(contentStream));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getOriginalFilename() {
        return this.originalFilename;
    }

    @Override
    @Nullable
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        return this.content.length == 0;
    }

    @Override
    public long getSize() {
        return (long)this.content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return this.content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(this.content);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        FileCopyUtils.copy(this.content, dest);
    }
}
