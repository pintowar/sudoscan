package com.github.pintowar.sudoscan;

import org.apache.commons.io.IOUtils;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.indexer.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Size;
import org.nd4j.common.util.ArrayUtil;
import org.nd4j.linalg.api.concurrency.AffinityManager;
import org.nd4j.linalg.api.memory.pointers.PagedPointer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.exception.ND4JIllegalStateException;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;
import java.io.InputStream;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * Uses JavaCV to load images. Allowed formats: bmp, gif, jpg, jpeg, jp2, pbm, pgm, ppm, pnm, png, tif, tiff, exr, webp
 *
 * @author saudet
 */
public class NativeImageLoader {

    protected long height;
    protected long width;
    protected long channels = -1;
    protected boolean centerCropIfNeeded = false;

    private static final int MIN_BUFFER_STEP_SIZE = 64 * 1024;
    private byte[] buffer = null;
    private Mat bufferMat = null;

    protected OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

    boolean direct = !Loader.getPlatform().startsWith("android");

    /**
     * Instantiate an image with the given
     * height and width
     *
     * @param height the height to load
     * @param width  the width to load
     */
    public NativeImageLoader(long height, long width) {
        this.height = height;
        this.width = width;
    }

    /**
     * Instantiate an image with the given
     * height and width
     *
     * @param height   the height to load
     * @param width    the width to load
     * @param channels the number of channels for the image*
     */
    public NativeImageLoader(long height, long width, long channels) {
        this.height = height;
        this.width = width;
        this.channels = channels;
    }

    /**
     * Instantiate an image with the given
     * height and width
     *
     * @param height             the height to load
     * @param width              the width to load
     * @param channels           the number of channels for the image*
     * @param centerCropIfNeeded to crop before rescaling and converting
     */
    public NativeImageLoader(long height, long width, long channels, boolean centerCropIfNeeded) {
        this(height, width, channels);
        this.centerCropIfNeeded = centerCropIfNeeded;
    }

    /**
     * Read the stream to the buffer, and return the number of bytes read
     *
     * @param is Input stream to read
     * @return Mat with the buffer data as a row vector
     * @throws IOException
     */
    private Mat streamToMat(InputStream is) throws IOException {
        if (buffer == null) {
            buffer = IOUtils.toByteArray(is);
            if (buffer.length <= 0) {
                throw new IOException("Could not decode image from input stream: input stream was empty (no data)");
            }
            bufferMat = new Mat(buffer);
            return bufferMat;
        } else {
            int numReadTotal = is.read(buffer);
            //Need to know if all data has been read.
            //(a) if numRead < buffer.length - got everything
            //(b) if numRead >= buffer.length: we MIGHT have got everything (exact right size buffer) OR we need more data

            if (numReadTotal <= 0) {
                throw new IOException("Could not decode image from input stream: input stream was empty (no data)");
            }

            if (numReadTotal < buffer.length) {
                bufferMat.data().put(buffer, 0, numReadTotal);
                bufferMat.cols(numReadTotal);
                return bufferMat;
            }

            //Buffer is full; reallocate and keep reading
            int numReadCurrent = numReadTotal;
            while (numReadCurrent != -1) {
                byte[] oldBuffer = buffer;
                if (oldBuffer.length == Integer.MAX_VALUE) {
                    throw new IllegalStateException("Cannot read more than Integer.MAX_VALUE bytes");
                }
                //Double buffer, but allocate at least 1MB more
                long increase = Math.max(buffer.length, MIN_BUFFER_STEP_SIZE);
                int newBufferLength = (int) Math.min(Integer.MAX_VALUE, buffer.length + increase);

                buffer = new byte[newBufferLength];
                System.arraycopy(oldBuffer, 0, buffer, 0, oldBuffer.length);
                numReadCurrent = is.read(buffer, oldBuffer.length, buffer.length - oldBuffer.length);
                if (numReadCurrent > 0) {
                    numReadTotal += numReadCurrent;
                }
            }

            bufferMat = new Mat(buffer);
            return bufferMat;
        }

    }

    protected void fillNDArray(Mat image, INDArray ret) {
        long rows = image.rows();
        long cols = image.cols();
        long channels = image.channels();

        if (ret.length() != rows * cols * channels) {
            throw new ND4JIllegalStateException("INDArray provided to store image not equal to image: {channels: "
                    + channels + ", rows: " + rows + ", columns: " + cols + "}");
        }

        Indexer idx = image.createIndexer(direct);
        Pointer pointer = ret.data().pointer();
        long[] stride = ret.stride();
        boolean done = false;
        PagedPointer pagedPointer = new PagedPointer(pointer, rows * cols * channels,
                ret.data().offset() * Nd4j.sizeOfDataType(ret.data().dataType()));

        if (pointer instanceof FloatPointer) {
            FloatIndexer retidx = FloatIndexer.create((FloatPointer) pagedPointer.asFloatPointer(),
                    new long[]{channels, rows, cols}, new long[]{stride[0], stride[1], stride[2]}, direct);
            if (idx instanceof UByteIndexer) {
                UByteIndexer ubyteidx = (UByteIndexer) idx;
                for (long k = 0; k < channels; k++) {
                    for (long i = 0; i < rows; i++) {
                        for (long j = 0; j < cols; j++) {
                            retidx.put(k, i, j, ubyteidx.get(i, j, k));
                        }
                    }
                }
                done = true;
            } else if (idx instanceof UShortIndexer) {
                UShortIndexer ushortidx = (UShortIndexer) idx;
                for (long k = 0; k < channels; k++) {
                    for (long i = 0; i < rows; i++) {
                        for (long j = 0; j < cols; j++) {
                            retidx.put(k, i, j, ushortidx.get(i, j, k));
                        }
                    }
                }
                done = true;
            } else if (idx instanceof IntIndexer) {
                IntIndexer intidx = (IntIndexer) idx;
                for (long k = 0; k < channels; k++) {
                    for (long i = 0; i < rows; i++) {
                        for (long j = 0; j < cols; j++) {
                            retidx.put(k, i, j, intidx.get(i, j, k));
                        }
                    }
                }
                done = true;
            } else if (idx instanceof FloatIndexer) {
                FloatIndexer floatidx = (FloatIndexer) idx;
                for (long k = 0; k < channels; k++) {
                    for (long i = 0; i < rows; i++) {
                        for (long j = 0; j < cols; j++) {
                            retidx.put(k, i, j, floatidx.get(i, j, k));
                        }
                    }
                }
                done = true;
            }
            retidx.release();
        } else if (pointer instanceof DoublePointer) {
            DoubleIndexer retidx = DoubleIndexer.create((DoublePointer) pagedPointer.asDoublePointer(),
                    new long[]{channels, rows, cols}, new long[]{stride[0], stride[1], stride[2]}, direct);
            if (idx instanceof UByteIndexer) {
                UByteIndexer ubyteidx = (UByteIndexer) idx;
                for (long k = 0; k < channels; k++) {
                    for (long i = 0; i < rows; i++) {
                        for (long j = 0; j < cols; j++) {
                            retidx.put(k, i, j, ubyteidx.get(i, j, k));
                        }
                    }
                }
                done = true;
            } else if (idx instanceof UShortIndexer) {
                UShortIndexer ushortidx = (UShortIndexer) idx;
                for (long k = 0; k < channels; k++) {
                    for (long i = 0; i < rows; i++) {
                        for (long j = 0; j < cols; j++) {
                            retidx.put(k, i, j, ushortidx.get(i, j, k));
                        }
                    }
                }
                done = true;
            } else if (idx instanceof IntIndexer) {
                IntIndexer intidx = (IntIndexer) idx;
                for (long k = 0; k < channels; k++) {
                    for (long i = 0; i < rows; i++) {
                        for (long j = 0; j < cols; j++) {
                            retidx.put(k, i, j, intidx.get(i, j, k));
                        }
                    }
                }
                done = true;
            } else if (idx instanceof FloatIndexer) {
                FloatIndexer floatidx = (FloatIndexer) idx;
                for (long k = 0; k < channels; k++) {
                    for (long i = 0; i < rows; i++) {
                        for (long j = 0; j < cols; j++) {
                            retidx.put(k, i, j, floatidx.get(i, j, k));
                        }
                    }
                }
                done = true;
            }
            retidx.release();
        }


        if (!done) {
            for (long k = 0; k < channels; k++) {
                for (long i = 0; i < rows; i++) {
                    for (long j = 0; j < cols; j++) {
                        if (ret.rank() == 3) {
                            ret.putScalar(k, i, j, idx.getDouble(i, j, k));
                        } else if (ret.rank() == 4) {
                            ret.putScalar(1, k, i, j, idx.getDouble(i, j, k));
                        } else if (ret.rank() == 2) {
                            ret.putScalar(i, j, idx.getDouble(i, j));
                        } else
                            throw new ND4JIllegalStateException("NativeImageLoader expects 2D, 3D or 4D output array, but " + ret.rank() + "D array was given");
                    }
                }
            }
        }

        idx.release();
        image.data();
        Nd4j.getAffinityManager().tagLocation(ret, AffinityManager.Location.HOST);
    }

    public INDArray asMatrix(Frame image) throws IOException {
        return asMatrix(converter.convert(image));
    }

    public INDArray asMatrix(org.opencv.core.Mat image) throws IOException {
        INDArray ret = transformImage(image, null);

        return ret.reshape(ArrayUtil.combine(new long[]{1}, ret.shape()));
    }

    public INDArray asMatrix(Mat image) throws IOException {
        INDArray ret = transformImage(image, null);

        return ret.reshape(ArrayUtil.combine(new long[]{1}, ret.shape()));
    }

    public INDArray asMatrix(InputStream is) throws IOException {
        return asMatrix(streamToMat(is));
    }

    protected INDArray transformImage(org.opencv.core.Mat image, INDArray ret) throws IOException {
        Frame f = converter.convert(image);
        return transformImage(converter.convert(f), ret);
    }

    protected INDArray transformImage(Mat image, INDArray ret) throws IOException {
        Mat image2 = null, image3 = null, image4 = null;
        if (channels > 0 && image.channels() != channels) {
            int code = -1;
            switch (image.channels()) {
                case 1:
                    switch ((int) channels) {
                        case 3:
                            code = CV_GRAY2BGR;
                            break;
                        case 4:
                            code = CV_GRAY2RGBA;
                            break;
                    }
                    break;
                case 3:
                    switch ((int) channels) {
                        case 1:
                            code = CV_BGR2GRAY;
                            break;
                        case 4:
                            code = CV_BGR2RGBA;
                            break;
                    }
                    break;
                case 4:
                    switch ((int) channels) {
                        case 1:
                            code = CV_RGBA2GRAY;
                            break;
                        case 3:
                            code = CV_RGBA2BGR;
                            break;
                    }
                    break;
            }
            if (code < 0) {
                throw new IOException("Cannot convert from " + image.channels() + " to " + channels + " channels.");
            }
            image2 = new Mat();
            cvtColor(image, image2, code);
            image = image2;
        }
        if (centerCropIfNeeded) {
            image3 = centerCropIfNeeded(image);
            if (image3 != image) {
                image = image3;
            } else {
                image3 = null;
            }
        }
        image4 = scalingIfNeed(image);
        if (image4 != image) {
            image = image4;
        } else {
            image4 = null;
        }

        if (ret == null) {
            int rows = image.rows();
            int cols = image.cols();
            int channels = image.channels();
            ret = Nd4j.create(channels, rows, cols);
        }
        fillNDArray(image, ret);

        image.data(); // dummy call to make sure it does not get deallocated prematurely
        if (image2 != null) {
            image2.deallocate();
        }
        if (image3 != null) {
            image3.deallocate();
        }
        if (image4 != null) {
            image4.deallocate();
        }
        return ret;
    }

    // TODO build flexibility on where to crop the image
    protected Mat centerCropIfNeeded(Mat img) {
        int x = 0;
        int y = 0;
        int height = img.rows();
        int width = img.cols();
        int diff = Math.abs(width - height) / 2;

        if (width > height) {
            x = diff;
            width = width - diff;
        } else if (height > width) {
            y = diff;
            height = height - diff;
        }
        return img.apply(new Rect(x, y, width, height));
    }

    protected Mat scalingIfNeed(Mat image) {
        return scalingIfNeed(image, height, width);
    }

    protected Mat scalingIfNeed(Mat image, long dstHeight, long dstWidth) {
        Mat scaled = image;
        if (dstHeight > 0 && dstWidth > 0 && (image.rows() != dstHeight || image.cols() != dstWidth)) {
            resize(image, scaled = new Mat(), new Size(
                    (int) Math.min(dstWidth, Integer.MAX_VALUE),
                    (int) Math.min(dstHeight, Integer.MAX_VALUE)));
        }
        return scaled;
    }

    /**
     * Returns {@code asFrame(array, -1)}.
     */
    public Frame asFrame(INDArray array) {
        return converter.convert(asMat(array));
    }

    /**
     * Converts an INDArray to a JavaCV Frame. Only intended for images with rank 3.
     *
     * @param array    to convert
     * @param dataType from JavaCV (DEPTH_FLOAT, DEPTH_UBYTE, etc), or -1 to use same type as the INDArray
     * @return data copied to a Frame
     */
    public Frame asFrame(INDArray array, int dataType) {
        return converter.convert(asMat(array, OpenCVFrameConverter.getMatDepth(dataType)));
    }

    /**
     * Returns {@code asMat(array, -1)}.
     */
    public Mat asMat(INDArray array) {
        return asMat(array, -1);
    }

    /**
     * Converts an INDArray to an OpenCV Mat. Only intended for images with rank 3.
     *
     * @param array    to convert
     * @param dataType from OpenCV (CV_32F, CV_8U, etc), or -1 to use same type as the INDArray
     * @return data copied to a Mat
     */
    public Mat asMat(INDArray array, int dataType) {
        if (array.rank() > 4 || (array.rank() > 3 && array.size(0) != 1)) {
            throw new UnsupportedOperationException("Only rank 3 (or rank 4 with size(0) == 1) arrays supported");
        }
        int rank = array.rank();
        long[] stride = array.stride();
        long offset = array.data().offset();
        Pointer pointer = array.data().pointer().position(offset);

        long rows = array.size(rank == 3 ? 1 : 2);
        long cols = array.size(rank == 3 ? 2 : 3);
        long channels = array.size(rank == 3 ? 0 : 1);
        boolean done = false;

        if (dataType < 0) {
            dataType = pointer instanceof DoublePointer ? CV_64F : CV_32F;
        }
        Mat mat = new Mat((int) Math.min(rows, Integer.MAX_VALUE), (int) Math.min(cols, Integer.MAX_VALUE),
                CV_MAKETYPE(dataType, (int) Math.min(channels, Integer.MAX_VALUE)));
        Indexer matidx = mat.createIndexer(direct);

        Nd4j.getAffinityManager().ensureLocation(array, AffinityManager.Location.HOST);

        if (pointer instanceof FloatPointer && dataType == CV_32F) {
            FloatIndexer ptridx = FloatIndexer.create((FloatPointer) pointer, new long[]{channels, rows, cols},
                    new long[]{stride[rank == 3 ? 0 : 1], stride[rank == 3 ? 1 : 2], stride[rank == 3 ? 2 : 3]}, direct);
            FloatIndexer idx = (FloatIndexer) matidx;
            for (long k = 0; k < channels; k++) {
                for (long i = 0; i < rows; i++) {
                    for (long j = 0; j < cols; j++) {
                        idx.put(i, j, k, ptridx.get(k, i, j));
                    }
                }
            }
            done = true;
            ptridx.release();
        } else if (pointer instanceof DoublePointer && dataType == CV_64F) {
            DoubleIndexer ptridx = DoubleIndexer.create((DoublePointer) pointer, new long[]{channels, rows, cols},
                    new long[]{stride[rank == 3 ? 0 : 1], stride[rank == 3 ? 1 : 2], stride[rank == 3 ? 2 : 3]}, direct);
            DoubleIndexer idx = (DoubleIndexer) matidx;
            for (long k = 0; k < channels; k++) {
                for (long i = 0; i < rows; i++) {
                    for (long j = 0; j < cols; j++) {
                        idx.put(i, j, k, ptridx.get(k, i, j));
                    }
                }
            }
            done = true;
            ptridx.release();
        }

        if (!done) {
            for (long k = 0; k < channels; k++) {
                for (long i = 0; i < rows; i++) {
                    for (long j = 0; j < cols; j++) {
                        if (rank == 3) {
                            matidx.putDouble(new long[]{i, j, k}, array.getDouble(k, i, j));
                        } else {
                            matidx.putDouble(new long[]{i, j, k}, array.getDouble(0, k, i, j));
                        }
                    }
                }
            }
        }

        matidx.release();
        return mat;
    }

}
