package com.example.webviewupload;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ParcelFileDescriptorUtils {
    public static ParcelFileDescriptor pipeFrom(InputStream inputStream)
            throws IOException {
        ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
        ParcelFileDescriptor readPart = pipe[0];
        ParcelFileDescriptor writePart = pipe[1];

        new TransferThread(-1, inputStream, new FileOutputStream(writePart.getFileDescriptor()))
                .start();

        return readPart;
    }

    static class TransferThread extends Thread {

        final InputStream mIn;
        final OutputStream mOut;
        long fileId;

        TransferThread(long fileId, InputStream in, OutputStream out) {
            super("Transfer Thread");
            mIn = new BufferedInputStream(in);
            mOut = new BufferedOutputStream(out);
            this.fileId = fileId;

            setDaemon(true);
        }

        @Override
        public void run() {
            byte[] buf = new byte[1024];
            int len;

            try {
                while (true) {
                    len = mIn.read(buf);

                    if (len < 0) {
                        break;
                    }

                    mOut.write(buf, 0, len);
                    mOut.flush(); // just to be safe
                }

                mOut.flush(); // just to be safe
            } catch (IOException e) {
                Log.i(getName(), "writing failed " + e.getMessage());
            } finally {
                try {
                    mIn.close();
                } catch (IOException e) {
                    Log.i(getName(), e.getMessage());
                }
                try {
                    mOut.close();
                } catch (IOException e) {
                    Log.i(getName(), e.getMessage());
                }
            }
        }
    }
}
