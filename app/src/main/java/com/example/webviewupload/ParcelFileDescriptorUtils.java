package com.example.webviewupload;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;

public class ParcelFileDescriptorUtils {
    public static ParcelFileDescriptor pipeFrom(InputStream inputStream)
            throws IOException {
        ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
        ParcelFileDescriptor readPart = pipe[0];
        ParcelFileDescriptor writePart = pipe[1];
        final Semaphore semaphore = new Semaphore(1);

        new TransferThread(-1, inputStream, new ParcelFileDescriptor.AutoCloseOutputStream(writePart),
                semaphore)
                .start();

        semaphore.acquireUninterruptibly();

        return readPart;
    }

    static class TransferThread extends Thread {

        final InputStream mIn;
        final OutputStream mOut;
        long fileId;
        private final Semaphore semaphore;

        TransferThread(long fileId, InputStream in, OutputStream out, Semaphore semaphore) {
            super("Transfer Thread");
            mIn = new BufferedInputStream(in);
            mOut = new BufferedOutputStream(out);
            this.fileId = fileId;
            this.semaphore = semaphore;

            setDaemon(true);
        }

        @Override
        public void run() {
            byte[] buf = new byte[4096];
            int len;

            try {
                while (true) {
                    len = mIn.read(buf);
                    if (len < 0) {
                        break;
                    }
                    mOut.write(buf, 0, len);
                    mOut.flush();
                }
                mOut.flush();
            } catch (IOException e) {
                Log.i(getName(), "writing failed " + e.getMessage());
            } finally {
                try {
                    mIn.close();
                    mOut.close();
                } catch (IOException e) {
                    Log.i(getName(), e.getMessage());
                }
                semaphore.release();
            }
        }
    }
}
