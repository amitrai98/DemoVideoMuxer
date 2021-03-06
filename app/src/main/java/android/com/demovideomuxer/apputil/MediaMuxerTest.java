package android.com.demovideomuxer.apputil;

/**
 * Created by amitrai on 20/5/16.
 */
//public class MediaMuxerTest extends AndroidTestCase {
//    private static final String TAG = "MediaMuxerTest";
//    private static final boolean VERBOSE = false;
//    private static final int MAX_SAMPLE_SIZE = 256 * 1024;
//    private Resources mResources;
//
//    @Override
//    public void setContext(Context context) {
//        super.setContext(context);
//        mResources = context.getResources();
//    }
//    /**
//     * Test: make sure the muxer handles both video and audio tracks correctly.
//     */
//    public void testVideoAudio() throws Exception {
//        int source = R.drawable.Produce;
//        String outputFile = "/sdcard/videoAudio.mp4";
//        cloneAndVerify(source, outputFile, 2, 90);
//    }
//    /**
//     * Test: make sure the muxer handles audio track only file correctly.
//     */
//    public void testAudioOnly() throws Exception {
//        int source = R.drawable.amit;
//        String outputFile = "/sdcard/audioOnly.mp4";
//        cloneAndVerify(source, outputFile, 1, -1);
//    }
//    /**
//     * Test: make sure the muxer handles video track only file correctly.
//     */
//    public void testVideoOnly() throws Exception {
//        int source = R.drawable.Produce;
//        String outputFile = "/sdcard/videoOnly.mp4";
//        cloneAndVerify(source, outputFile, 1, 180);
//    }
//    /**
//     * Tests: make sure the muxer handles exceptions correctly.
//     * <br> Throws exception b/c start() is not called.
//     * <br> Throws exception b/c 2 video tracks were added.
//     * <br> Throws exception b/c 2 audio tracks were added.
//     * <br> Throws exception b/c 3 tracks were added.
//     * <br> Throws exception b/c no tracks was added.
//     * <br> Throws exception b/c a wrong format.
//     */
//    public void testIllegalStateExceptions() throws IOException {
//        String outputFile = "/sdcard/muxerExceptions.mp4";
//        MediaMuxer muxer;
//        // Throws exception b/c start() is not called.
//        muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//        muxer.addTrack(MediaFormat.createVideoFormat("video/avc", 480, 320));
//        try {
//            muxer.stop();
//            fail("should throw IllegalStateException.");
//        } catch (IllegalStateException e) {
//            // expected
//        }
//        // Throws exception b/c 2 video tracks were added.
//        muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//        muxer.addTrack(MediaFormat.createVideoFormat("video/avc", 480, 320));
//        try {
//            muxer.addTrack(MediaFormat.createVideoFormat("video/avc", 480, 320));
//            fail("should throw IllegalStateException.");
//        } catch (IllegalStateException e) {
//            // expected
//        }
//        // Throws exception b/c 2 audio tracks were added.
//        muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//        muxer.addTrack(MediaFormat.createAudioFormat("audio/mp4a-latm", 48000, 1));
//        try {
//            muxer.addTrack(MediaFormat.createAudioFormat("audio/mp4a-latm", 48000, 1));
//            fail("should throw IllegalStateException.");
//        } catch (IllegalStateException e) {
//            // expected
//        }
//        // Throws exception b/c 3 tracks were added.
//        muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//        muxer.addTrack(MediaFormat.createVideoFormat("video/avc", 480, 320));
//        muxer.addTrack(MediaFormat.createAudioFormat("audio/mp4a-latm", 48000, 1));
//        try {
//            muxer.addTrack(MediaFormat.createVideoFormat("video/avc", 480, 320));
//            fail("should throw IllegalStateException.");
//        } catch (IllegalStateException e) {
//            // expected
//        }
//        // Throws exception b/c no tracks was added.
//        muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//        try {
//            muxer.start();
//            fail("should throw IllegalStateException.");
//        } catch (IllegalStateException e) {
//            // expected
//        }
//        // Throws exception b/c a wrong format.
//        muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//        try {
//            muxer.addTrack(MediaFormat.createVideoFormat("vidoe/mp4", 480, 320));
//            fail("should throw IllegalStateException.");
//        } catch (IllegalStateException e) {
//            // expected
//        }
//        new File(outputFile).delete();
//    }
//    /**
//     * Using the MediaMuxer to clone a media file.
//     */
//    private void cloneMediaUsingMuxer(int srcMedia, String dstMediaPath,
//                                      int expectedTrackCount, int degrees) throws IOException {
//        // Set up MediaExtractor to read from the source.
//        AssetFileDescriptor srcFd = mResources.openRawResourceFd(srcMedia);
//        MediaExtractor extractor = new MediaExtractor();
//        extractor.setDataSource(srcFd.getFileDescriptor(), srcFd.getStartOffset(),
//                srcFd.getLength());
//        int trackCount = extractor.getTrackCount();
//        assertEquals("wrong number of tracks", expectedTrackCount, trackCount);
//        // Set up MediaMuxer for the destination.
//        MediaMuxer muxer;
//        muxer = new MediaMuxer(dstMediaPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//        // Set up the tracks.
//        HashMap<Integer, Integer> indexMap = new HashMap<Integer, Integer>(trackCount);
//        for (int i = 0; i < trackCount; i++) {
//            extractor.selectTrack(i);
//            MediaFormat format = extractor.getTrackFormat(i);
//            int dstIndex = muxer.addTrack(format);
//            indexMap.put(i, dstIndex);
//        }
//        // Copy the samples from MediaExtractor to MediaMuxer.
//        boolean sawEOS = false;
//        int bufferSize = MAX_SAMPLE_SIZE;
//        int frameCount = 0;
//        int offset = 100;
//        ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
//        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
//        if (degrees >= 0) {
//            muxer.setOrientationHint(degrees);
//        }
//        muxer.start();
//        while (!sawEOS) {
//            bufferInfo.offset = offset;
//            bufferInfo.size = extractor.readSampleData(dstBuf, offset);
//            if (bufferInfo.size < 0) {
//                if (VERBOSE) {
//                    Log.d(TAG, "saw input EOS.");
//                }
//                sawEOS = true;
//                bufferInfo.size = 0;
//            } else {
//                bufferInfo.presentationTimeUs = extractor.getSampleTime();
//                bufferInfo.flags = MediaCodec.BUFFER_FLAG_CODEC_CONFIG;//extractor.getSampleFlags();
//                int trackIndex = extractor.getSampleTrackIndex();
//                muxer.writeSampleData(indexMap.get(trackIndex), dstBuf,
//                        bufferInfo);
//                extractor.advance();
//                frameCount++;
//                if (VERBOSE) {
//                    Log.d(TAG, "Frame (" + frameCount + ") " +
//                            "PresentationTimeUs:" + bufferInfo.presentationTimeUs +
//                            " Flags:" + bufferInfo.flags +
//                            " TrackIndex:" + trackIndex +
//                            " Size(KB) " + bufferInfo.size / 1024);
//                }
//            }
//        }
//        muxer.stop();
//        muxer.release();
//        srcFd.close();
//        return;
//    }
//    /**
//     * Clones a media file and then compares against the source file to make
//     * sure they match.
//     */
//    private void cloneAndVerify(int srcMedia, String outputMediaFile,
//                                int expectedTrackCount, int degrees) throws IOException {
//        try {
//            cloneMediaUsingMuxer(srcMedia, outputMediaFile, expectedTrackCount, degrees);
//            verifyAttributesMatch(srcMedia, outputMediaFile, degrees);
//            // Check the sample on 1s and 0.5s.
//            verifySamplesMatch(srcMedia, outputMediaFile, 1000000);
//            verifySamplesMatch(srcMedia, outputMediaFile, 500000);
//        } finally {
//            new File(outputMediaFile).delete();
//        }
//    }
//    /**
//     * Compares some attributes using MediaMetadataRetriever to make sure the
//     * cloned media file matches the source file.
//     */
//    private void verifyAttributesMatch(int srcMedia, String testMediaPath,
//                                       int degrees) {
//        AssetFileDescriptor testFd = mResources.openRawResourceFd(srcMedia);
//        MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
//        retrieverSrc.setDataSource(testFd.getFileDescriptor(),
//                testFd.getStartOffset(), testFd.getLength());
//        MediaMetadataRetriever retrieverTest = new MediaMetadataRetriever();
//        retrieverTest.setDataSource(testMediaPath);
//        String testDegrees = retrieverTest.extractMetadata(
//                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
//        if (testDegrees != null) {
//            assertEquals("Different degrees", degrees,
//                    Integer.parseInt(testDegrees));
//        }
//        String heightSrc = retrieverSrc.extractMetadata(
//                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
//        String heightTest = retrieverTest.extractMetadata(
//                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
//        assertEquals("Different height", heightSrc,
//                heightTest);
//        String widthSrc = retrieverSrc.extractMetadata(
//                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
//        String widthTest = retrieverTest.extractMetadata(
//                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
//        assertEquals("Different height", widthSrc,
//                widthTest);
//        String durationSrc = retrieverSrc.extractMetadata(
//                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
//        String durationTest = retrieverTest.extractMetadata(
//                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
//        assertEquals("Different height", durationSrc,
//                durationTest);
//        retrieverSrc.release();
//        retrieverTest.release();
//    }
//    /**
//     * Uses 2 MediaExtractor, seeking to the same position, reads the sample and
//     * makes sure the samples match.
//     */
//    private void verifySamplesMatch(int srcMedia, String testMediaPath,
//                                    int seekToUs) throws IOException {
//        AssetFileDescriptor testFd = mResources.openRawResourceFd(srcMedia);
//        MediaExtractor extractorSrc = new MediaExtractor();
//        extractorSrc.setDataSource(testFd.getFileDescriptor(),
//                testFd.getStartOffset(), testFd.getLength());
//        int trackCount = extractorSrc.getTrackCount();
//        MediaExtractor extractorTest = new MediaExtractor();
//        extractorTest.setDataSource(testMediaPath);
//        assertEquals("wrong number of tracks", trackCount,
//                extractorTest.getTrackCount());
//        // Make sure the format is the same and select them
//        for (int i = 0; i < trackCount; i++) {
//            MediaFormat formatSrc = extractorSrc.getTrackFormat(i);
//            MediaFormat formatTest = extractorTest.getTrackFormat(i);
//            String mimeIn = formatSrc.getString(MediaFormat.KEY_MIME);
//            String mimeOut = formatTest.getString(MediaFormat.KEY_MIME);
//            if (!(mimeIn.equals(mimeOut))) {
//                fail("format didn't match on track No." + i +
//                        formatSrc.toString() + "\n" + formatTest.toString());
//            }
//            extractorSrc.selectTrack(i);
//            extractorTest.selectTrack(i);
//        }
//        // Pick a time and try to compare the frame.
//        extractorSrc.seekTo(seekToUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
//        extractorTest.seekTo(seekToUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
//        int bufferSize = MAX_SAMPLE_SIZE;
//        ByteBuffer byteBufSrc = ByteBuffer.allocate(bufferSize);
//        ByteBuffer byteBufTest = ByteBuffer.allocate(bufferSize);
//        extractorSrc.readSampleData(byteBufSrc, 0);
//        extractorTest.readSampleData(byteBufTest, 0);
//        if (!(byteBufSrc.equals(byteBufTest))) {
//            fail("byteBuffer didn't match");
//        }
//    }
//}
