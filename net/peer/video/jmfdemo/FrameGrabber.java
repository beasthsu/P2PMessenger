/**
 *  JMF/Webcam Frame Grabber Demo
 *
 * @author S.Ritter  24.01.2002
 * @version 1.0
 *
 *  ALL EXAMPLES OF CODE AND/OR COMMAND-LINE INSTRUCTIONS ARE BEING 
 *  PROVIDED BY SUN AS A COURTESY, "AS IS," AND SUN DISCLAIMS ANY AND 
 *  ALL WARRANTIES PERTAINING THERETO, INCLUDING ANY WARRANTIES OF 
 *  MERCHANTABILTY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. 
 *  SUN IS NOT LICENSING THIS EXAMPLE FOR ANY USE OTHER THAN FOR THE
 *  EDUCATIONAL PURPOSE OF SHOWING THE FUNCTIONALITY CONTAINED
 *  THEREIN, BY WAY OF EXAMPLE.
 **/
package net.peer.video.jmfdemo;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.util.Properties;
import javax.media.*;
import javax.media.control.*;
import javax.media.protocol.*;
import javax.media.util.*;
import javax.media.format.RGBFormat;

/**
 *  Frame grabber class
 **/
public class FrameGrabber extends Thread implements ControllerListener {
  /*  Default device name and format parameters to use if no properties file
   *  is provided
   */
  private final static String DEFAULT_DEV_NAME =
    "vfw:Microsoft WDM Image Capture (Win32):0";
  private final static String DEFAULT_X_RES = "320";
  private final static String DEFAULT_Y_RES = "240";
  private final static String DEFAULT_DEPTH = "24";

  private Properties videoProperties;

  /*  These objects are used for controlling access via wait and notify to
   *  ensure that the processor has been realised and the second thread has
   *  completed it's startup
   */
  private Object stateLock = new Object();
  private Object runLock = new Object();

  private Processor deviceProc = null;
  private PushBufferStream camStream;
  private PushBufferDataSource source = null;
  private BufferToImage converter;
  private Image currentImage;
  private boolean threadRunning = false;
  private boolean flag = true;

  /**
   *  Constructor
   *
   * 
   * @throws FrameGrabberException If we can't start up the camera
   **/
  public FrameGrabber() throws FrameGrabberException {
    /*  If the user chooses the no parameter form of the constructor we
     *  try to get the video property file name from the properties
     *  passed on the command line
     */
    String videoPropFile =
      System.getProperty("video.properties", "video.properties");

    setup(videoPropFile);
  }

  /**
   *  Constructor
   *
   * @param videoPropFile The name of the video properties file 
   * @throws FrameGrabberException If we can't start up the camera
   **/
  public FrameGrabber(String videoPropFile) throws FrameGrabberException {
    setup(videoPropFile);
  }

  /**
   *  Setup method.  Configures webcam and JMF ready to get images
   *
   * @param videoPropFile The name of the video properties file 
   * @throws FrameGrabberException If we can't start up the camera
   **/
  private void setup(String videoPropFile) throws FrameGrabberException {
    videoProperties = new Properties();

    if (videoPropFile != null) {
      try {
        FileInputStream fis = new FileInputStream(new File(videoPropFile));
        videoProperties.load(fis);
      } catch (IOException ioe) {
        System.out.println("Unable to access video properties");
        System.out.println(ioe.getMessage());
      }
    }

    Dimension viewSize = null;
    int viewDepth = 0;

    String cameraDevice =
      videoProperties.getProperty("device-name", DEFAULT_DEV_NAME);

    /*  Get the parameters for the video capture device from the properties
     *  file.  If not defined use default values
     */
    try {
      String pValue =
        videoProperties.getProperty("resolution-x", DEFAULT_X_RES);
      int xRes = Integer.parseInt(pValue);
      pValue = videoProperties.getProperty("resolution-y", DEFAULT_Y_RES);
      int yRes = Integer.parseInt(pValue);
      viewSize = new Dimension(xRes, yRes);
      pValue = videoProperties.getProperty("colour-depth", DEFAULT_DEPTH);
      viewDepth = Integer.parseInt(pValue);
    } catch (NumberFormatException nfe) {
      System.out.println("Bad numeric value in video properties file");
      System.exit(1);
    }

    /*  Try to get the CaptureDevice that matches the name supplied by the
     *  user
     */
    CaptureDeviceInfo device = CaptureDeviceManager.getDevice(cameraDevice);

    if (device == null)
      throw new FrameGrabberException("No device found [ " +
        cameraDevice + "]");

    RGBFormat userFormat = null;
    Format[] cfmt = device.getFormats();

    /*  Find the format that the user has requested (if available)  */
    for (int i = 0; i < cfmt.length; i++) {
      if (cfmt[i] instanceof RGBFormat) {
        userFormat = (RGBFormat)cfmt[i];
        Dimension d = userFormat.getSize();
        int bitsPerPixel = userFormat.getBitsPerPixel();
    
        if (viewSize.equals(d) && bitsPerPixel == viewDepth)
          break;

        userFormat = null;
      }
    }

    /*  Throw an exception if we can't find a format that matches the 
     *  user's criteria
     */
    if (userFormat == null)
      throw new FrameGrabberException("Requested format not supported");

    /*  To use this device we need a MediaLocator  */
    MediaLocator loc = device.getLocator();

    if (loc == null)
      throw new FrameGrabberException("Unable to get MediaLocator for device");

    DataSource formattedSource = null;

    /*  Now create a dataSource for this device and set the format to
     *  the one chosen by the user.
     */
    try {
      formattedSource = Manager.createDataSource(loc);
    } catch (IOException ioe) {
      throw new FrameGrabberException("IO Error creating dataSource");
    } catch (NoDataSourceException ndse) {
      throw new FrameGrabberException("Unable to create dataSource");
    }

    /*  Setting the format is rather complicated.  Firstly we need to get
     *  the format controls from the dataSource we just created.  In order
     *  to do this we need a reference to an object implementing the 
     *  CaptureDevice interface (which DataSource objects can).
     */
    if (!(formattedSource instanceof CaptureDevice))
      throw new FrameGrabberException("DataSource not a CaptureDevice");

    FormatControl[] fmtControls =
      ((CaptureDevice)formattedSource).getFormatControls();

    if (fmtControls == null || fmtControls.length == 0)
      throw new FrameGrabberException("No FormatControl available");

    Format setFormat = null;

    /*  Now we need to loop through the available FormatControls and try
     *  to set the format to the one we want.  According to the documentation
     *  even though this may appear to work, it may fail later on.  Since
     *  we know that the format is supported we hope that this won't happen
     */
    for (int i = 0; i < fmtControls.length; i++) {
      if (fmtControls[i] == null)
        continue;

      if ((setFormat = fmtControls[i].setFormat(userFormat)) != null)
        break;
    }

    /*  Throw an exception if we couldn't set the format  */
    if (setFormat == null)
      throw new FrameGrabberException("Failed to set camera format");

    /*  Connect to the DataSource  */
    try {
      formattedSource.connect();
    } catch (IOException ioe) {
      throw new FrameGrabberException("Unable to connect to DataSource");
    }

    /*  Since we don't want to display the output to the user at this stage
     *  we use a processor rather than a player to get frame access
     */
    try {
      deviceProc = Manager.createProcessor(formattedSource);
    } catch (IOException ioe) {
      throw new FrameGrabberException("Unable to get Processor for device: " +
        ioe.getMessage());
    } catch (NoProcessorException npe) {
      throw new FrameGrabberException("Unable to get Processor for device: " +
        npe.getMessage());
    }

    /*  In order to use the controller we have to put it in the realized
     *  state.  We do this by calling the realize method, but this will
     *  return immediately so we must register a listener (this class) to
     *  be notified when the controller is ready.
     */
    deviceProc.addControllerListener(this);
    deviceProc.realize();

    /*  Wait for the device to send an event telling us that it has
     *  reached the realized state
     */
    while (deviceProc.getState() != Controller.Realized) {
      synchronized (stateLock) {
        try {
          stateLock.wait();
        } catch (InterruptedException ie) {
          throw new FrameGrabberException("Failed to get to realized state");
        }
      }
    }

    deviceProc.start();

    /*  Get access to the PushBufferDataSource which will provide us with
     *  a means to get at the frame grabber
     */
    try {
      source = (PushBufferDataSource)deviceProc.getDataOutput();
    } catch (NotRealizedError nre) {
      /*  Should never happen  */
      throw new FrameGrabberException("Processor not realized");
    }
    
    /*  Now we can retrieve the PushBufferStreams that will enable us to 
     *  access the data from the camera
     */
    PushBufferStream[] streams = source.getStreams();
    camStream = null;

    for (int i = 0; i < streams.length; i++) {
      /*  Use the first Stream that is RGBFormat (there should be only one  */
      if (streams[i].getFormat() instanceof RGBFormat) {
        camStream = streams[i];
        RGBFormat rgbf = (RGBFormat)streams[i].getFormat();
        converter = new BufferToImage(rgbf);
        break;
      }
    }

    System.out.println("Capture device ready");
  }

  /**
   *  Get an image from the camera as an AWT Image object
   *
   * @returns The current image from the camera
   **/
  public Image getImage() {
    /*  Since we are using a second thread to grab the images from the webcam
     *  we need to ensure that an image has been aquired.
     *  We do this by using a flag which will be set to true in the run() 
     *  method.  If this is false we wait until the run method notifies us
     *  that there is an image to collect
     */
    while (threadRunning == false) {
      synchronized (runLock) {
        try {
          runLock.wait();
        } catch (InterruptedException ie) {
          // Ignore
        }
      }
    }

    return accessInternalImage(null);
  }

  /**
   *  Get an image from the camera as a BufferedImage
   *
   * @returns The current image from the camera
   **/
  public BufferedImage getBufferedImage() {
    return (BufferedImage)getImage();
  }

  /**
   *  Run method for Thread class
   **/
  public void run() {
    System.out.println("Capture thread starting...");
    Buffer b = new Buffer();

    /*  Simply loop forever grabbing images from the web cam and storing 
     *  them so that the user can retrieve them when required.
     */
    
    while (flag) {
      try {
        camStream.read(b);
      } catch (Exception e) {
    	  System.out.println("Cannot read camStream");
        //  Ignore.  Nothing we can really do about this
      }

      Image i = converter.createImage(b);
      accessInternalImage(i);

      /*  If this is the first image we've collected we need to advertise
       *  to the main thread that there is an image ready and then notify 
       *  the main thread in case it is waiting on the image
       */
      if (!threadRunning) {
        threadRunning = true;

        synchronized (runLock) {
          runLock.notifyAll();
        }
      }
    }
  }

  /**
   *  Method called when a controller event is received (implements
   *  ControllerListener interface)
   *
   * @param ce The controller event
   **/
  public void controllerUpdate(ControllerEvent ce) {
    if (ce instanceof RealizeCompleteEvent) {
      synchronized (stateLock) {
        stateLock.notifyAll();
      }
    }
  }

  /**
   *  Method that controls access to the global image variable.  This ensures
   *  that there is no confusion over one thread reading an image whilst
   *  another is writing to it
   *
   * @param image The image to store (null indicates retrieval of the image)
   * @return The image (if the parameter was null)
   **/
  private synchronized Image accessInternalImage(Image image) {
    if (image == null) {
      return currentImage;
    }

    currentImage = image;
    return null;
  }
  
  public void setFlag(boolean flag){
	  this.flag = flag;
  }
}
