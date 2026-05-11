package org.bot.nullbot.entity;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * 支持HTTP范围请求的Resource实现 - Spring Boot 4 版本
 */
public class HttpRangeResource extends AbstractResource {

    private final Resource delegate;
    private final long rangeStart;
    private final long rangeEnd;
    private final long contentLength;

    public HttpRangeResource(Resource delegate, long rangeStart, long rangeEnd) {
        this.delegate = delegate;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.contentLength = rangeEnd - rangeStart + 1;
    }

    @Override
    public String getDescription() {
        return "Range resource for " + delegate.getDescription() +
                " (bytes " + rangeStart + "-" + rangeEnd + ")";
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream in = delegate.getInputStream();

        // 跳过起始字节
        long skipped = in.skip(rangeStart);
        if (skipped < rangeStart) {
            // 如果无法跳过足够字节，可能文件太小
            throw new IOException("Failed to skip to byte position " + rangeStart);
        }

        // 返回限制长度的输入流
        return new LimitedInputStream(in, contentLength);
    }

    @Override
    public boolean exists() {
        return delegate.exists();
    }

    @Override
    public URL getURL() throws IOException {
        return delegate.getURL();
    }

    @Override
    public URI getURI() throws IOException {
        return delegate.getURI();
    }

    @Override
    public File getFile() throws IOException {
        return delegate.getFile();
    }

    @Override
    public ReadableByteChannel readableChannel() throws IOException {
        return Channels.newChannel(getInputStream());
    }

    @Override
    public long contentLength() throws IOException {
        return contentLength;
    }

    @Override
    public long lastModified() throws IOException {
        return delegate.lastModified();
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        return delegate.createRelative(relativePath);
    }

    @Override
    public String getFilename() {
        return delegate.getFilename();
    }

    @Override
    public boolean isReadable() {
        return delegate.isReadable();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public boolean isFile() {
        return delegate.isFile();
    }

    /**
     * 限制长度的InputStream
     */
    private static class LimitedInputStream extends InputStream {
        private final InputStream delegate;
        private long remaining;
        private long position;

        public LimitedInputStream(InputStream delegate, long limit) {
            this.delegate = delegate;
            this.remaining = limit;
            this.position = 0;
        }

        @Override
        public int read() throws IOException {
            if (remaining <= 0) {
                return -1;
            }

            int result = delegate.read();
            if (result != -1) {
                remaining--;
                position++;
            }
            return result;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) {
                return -1;
            }

            int toRead = (int) Math.min(len, remaining);
            int read = delegate.read(b, off, toRead);
            if (read != -1) {
                remaining -= read;
                position += read;
            }
            return read;
        }

        @Override
        public long skip(long n) throws IOException {
            long toSkip = Math.min(n, remaining);
            long skipped = delegate.skip(toSkip);
            remaining -= skipped;
            position += skipped;
            return skipped;
        }

        @Override
        public int available() throws IOException {
            return (int) Math.min(delegate.available(), remaining);
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        @Override
        public synchronized void mark(int readlimit) {
            delegate.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            delegate.reset();
        }

        @Override
        public boolean markSupported() {
            return delegate.markSupported();
        }
    }
}
