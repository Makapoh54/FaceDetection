package face_detection;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.video.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class TestApp {

	private static JFrame buildFrame() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(1280, 720);
		frame.setVisible(true);
		return frame;
	}

	private static Image toBufferedImage(Mat m) {
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (m.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b); // get all the pixels
		BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return image;

	}

	public static void main(String args[]) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		VideoCapture camera = new VideoCapture(0);
		camera.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, 640);
		camera.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, 360);

		CascadeClassifier faceDetector = new CascadeClassifier(
				"lbpcascade_frontalface.xml");
		MatOfRect faceDetections = new MatOfRect();

		ProcessBuilder pbOff = new ProcessBuilder("cmd", "/c", "nircmd monitor off");
		ProcessBuilder pbOn = new ProcessBuilder("cmd", "/c", "nircmd monitor on");

		List<Integer> cathcedFrames = new ArrayList<Integer>();
		// int sum = IntStream.of(a).sum();

		if (!camera.isOpened()) {
			System.out.println("Error");
		} else {
			int index = 0;
			JFrame frameToDisplay = buildFrame();
			Mat frame = new Mat();
			while (true) {
				if (camera.read(frame)) {

					frameToDisplay.validate();
					frameToDisplay.getContentPane().removeAll();
					System.out.println("Captured Frame Width " + frame.width() + " Height " + frame.height());
					Imgcodecs.imwrite("camera" + (index++) + ".jpg", frame);
					System.out.println("OK");

					faceDetector.detectMultiScale(frame, faceDetections);
					System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));

					if (cathcedFrames.size() < 10) {
						cathcedFrames.add(faceDetections.toArray().length);
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						int sum = cathcedFrames.stream().mapToInt(Integer::intValue).sum();
						cathcedFrames.clear();
						if (sum < 2) {
							try {
								Process process_1 = pbOff.start();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							System.out.println("Face NOT Detected!");

						} else {
							System.out.println("Face Detected!");
							try {
								Process process_1 = pbOn.start();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
					}

					for (Rect rect : faceDetections.toArray()) {
						Imgproc.rectangle(frame, new Point(rect.x, rect.y),
								new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
					}

					try {
						Runtime.getRuntime().exec("taskkill /F /IM nircmd.exe");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					final Image imageToDisplay = toBufferedImage(frame);
					JPanel panel = new BackGroundImage(imageToDisplay);
					frameToDisplay.add(panel);
					/*
					 * try { Thread.sleep(50); } catch (InterruptedException e)
					 * { // TODO Auto-generated catch block e.printStackTrace();
					 * }
					 */
					// display face detections

					// break;
				}

			}
		}
		camera.release();
	}
}