package com.yocn.meida.mediacodec;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import com.yocn.meida.util.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AACToPCM {
    private static final String TAG = "AACToPCM";
    public static final int ERROR_INPUT_INVALID = 100;
    public static final int ERROR_OUTPUT_FAILED = 200;
    public static final int ERROR_OPEN_CODEC = 300;
    public static final int OK = 0;
    private static final int TIMEOUT_USEC = 0;
    private MediaExtractor mExtractor;
    private MediaFormat mFormat;
    private MediaCodec mDecoder;
    private FileOutputStream mFos;
    private ByteBuffer[] mInputBuffers;
    private ByteBuffer[] mOutputBuffers;
    private boolean mDecodeEnd;

    public AACToPCM() {
    }

    private int checkPath(String path) {
        if (path == null || path.isEmpty()) {
            Log.d(TAG, "invalid path, path is empty");
            return ERROR_INPUT_INVALID;
        }
        File file = new File(path);
        if (!file.isFile()) {
            Log.d(TAG, "path is not a file, path:" + path);
            return ERROR_INPUT_INVALID;
        } else if (!file.exists()) {
            Log.d(TAG, "file not exists, path:" + path);
            return ERROR_INPUT_INVALID;
        } else {
            Log.d(TAG, "path is a file, path:" + path);
        }
        return OK;
    }

    public int decodeAACToPCM(String audioPath, String pcmPath) {
        int ret;
        if (OK != (ret = openInput(audioPath))) {
            return ret;
        }
        if (OK != (ret = openOutput(pcmPath))) {
            return ret;
        }
        if (OK != (ret = openCodec(mFormat))) {
            return ret;
        }
        mDecodeEnd = false;
        while (!mDecodeEnd) {
            if (OK != (ret = decode(mDecoder, mExtractor))) {
                Log.d(TAG, "decode failed, ret=" + ret);
                break;
            }
        }
        close();
        return ret;
    }

    private int decode(MediaCodec codec, MediaExtractor extractor) {
        Log.d(TAG, "decode");
        int inputIndex = codec.dequeueInputBuffer(TIMEOUT_USEC);
        if (inputIndex >= 0) {
            ByteBuffer inputBuffer;
            if (Build.VERSION.SDK_INT >= 21) {
                inputBuffer = codec.getInputBuffer(inputIndex);
            } else {
                inputBuffer = mInputBuffers[inputIndex];
            }
            inputBuffer.clear();
            int sampleSize = extractor.readSampleData(inputBuffer, 0);
            if (sampleSize < 0) { //read end
                codec.queueInputBuffer(inputIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                extractor.advance();
            }
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputIndex = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
        if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {//TIMEOUT
            Log.d(TAG, "INFO_TRY_AGAIN_LATER");//TODO how to declare this info
            return OK;
        } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            Log.d(TAG, "output format changed");
            return OK;
        } else if (outputIndex < 0) {
            Log.d(TAG, "outputIndex=" + outputIndex);
            return OK;
        } else {
            ByteBuffer outputBuffer;
            if (Build.VERSION.SDK_INT >= 21) {
                outputBuffer = codec.getOutputBuffer(outputIndex);
            } else {
                outputBuffer = mOutputBuffers[outputIndex];
            }
            byte[] buffer = new byte[bufferInfo.size];
            outputBuffer.get(buffer);
            try {
                Log.d(TAG, "output write, size=" + bufferInfo.size);
                mFos.write(buffer);
                mFos.flush();
            } catch (IOException e) {
                e.printStackTrace();
                return ERROR_OUTPUT_FAILED;
            }
            codec.releaseOutputBuffer(outputIndex, false);
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                mDecodeEnd = true;
            }
        }
        return OK;
    }

    private int openCodec(MediaFormat format) {
        Log.d(TAG, "openCodec, format mime:" + format.getString(MediaFormat.KEY_MIME));
        try {
            mDecoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME));
        } catch (IOException e) {
            e.printStackTrace();
            return ERROR_OPEN_CODEC;
        }
        mDecoder.configure(format, null, null, 0);
        mDecoder.start();
        if (Build.VERSION.SDK_INT < 21) {
            mInputBuffers = mDecoder.getInputBuffers();
            mOutputBuffers = mDecoder.getOutputBuffers();
        }
        return OK;
    }

    private int openInput(String audioPath) {
        Log.d(TAG, "openInput audioPath:" + audioPath);
        int ret;
        if (OK != (ret = checkPath(audioPath))) {
            return ret;
        }
        mExtractor = new MediaExtractor();
        int audioTrack = -1;
        boolean hasAudio = false;
        try {
            mExtractor.setDataSource(audioPath);
            for (int i = 0; i < mExtractor.getTrackCount(); ++i) {
                MediaFormat format = mExtractor.getTrackFormat(i);

                int channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                String mime = format.getString(MediaFormat.KEY_MIME);
                LogUtil.d("yocn", " channelCount:" + channelCount + " sampleRate:" + sampleRate);
                LogUtil.d("mime=", "mime:" + format.toString());
                Log.d(TAG, "mime=" + mime);
                if (mime.startsWith("audio/")) {
                    audioTrack = i;
                    hasAudio = true;
                    mFormat = format;
                    break;
                }
            }
            if (!hasAudio) {
                Log.d(TAG, "input contain no audio");
                return ERROR_INPUT_INVALID;
            }
            mExtractor.selectTrack(audioTrack);
        } catch (IOException e) {
            return ERROR_INPUT_INVALID;
        }
        return OK;
    }

    private int openOutput(String outputPath) {
        Log.d(TAG, "openOutput outputPath:" + outputPath);
        try {
            mFos = new FileOutputStream(outputPath);
        } catch (IOException e) {
            return ERROR_OUTPUT_FAILED;
        }
        return OK;
    }

    private void close() {
        mExtractor.release();
        mDecoder.stop();
        mDecoder.release();
        try {
            mFos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}