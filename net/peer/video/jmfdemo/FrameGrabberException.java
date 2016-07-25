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

/**
 *  Exception that is thrown if the frame grabber doesn't work correctly
 **/
public class FrameGrabberException extends Exception {
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public FrameGrabberException(String msg) {
    super(msg);
  }
}
