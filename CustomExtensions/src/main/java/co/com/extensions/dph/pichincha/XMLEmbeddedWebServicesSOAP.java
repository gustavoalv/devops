/**
 * 
 */
package co.com.extensions.dph.pichincha;


import org.apache.log4j.Logger;

import com.itko.lisa.test.TestExec;
import com.itko.lisa.vse.stateful.model.Response;
import com.itko.lisa.vse.stateful.model.TransientResponse;
import com.itko.lisa.vse.stateful.protocol.ws.WSSOAPProtocolHandler;

/**
 * @author alvgu02
 *
 */

public class XMLEmbeddedWebServicesSOAP extends WSSOAPProtocolHandler {

	private static Logger logger = Logger.getLogger(XMLEmbeddedWebServicesSOAP.class);
	/**
     * Extract xml payload during recording
     * 
     * @param response
     *            the response to update.
     */
	@Override
	public void updateResponse(TestExec testExec, Response response) {
		RecordingXMLEmbedded rec = new RecordingXMLEmbedded(response.getBodyAsString());
        String xmlFormated = rec.extract();
//        logger.info("Previous xmlFormated = " + xmlFormated);
//        xmlFormated = StringEscapeUtils.escapeXml(xmlFormated);
        logger.info("xmlFormated = " + xmlFormated);
        response.setBody(xmlFormated);
        super.updateResponse(testExec, response);
        String xmlEmbedded = response.getBodyAsString();
        logger.info("xmlEmbedded = " + xmlEmbedded);
        rec.inject(xmlEmbedded);
        String recString = rec.toString();
        logger.info("recString = " + recString);
        response.setBody(recString);
		super.updateResponse(testExec, response);
		logger.info("Recording: public void updateResponse(TestExec testExec, Response response)");
	}

	 /**
     * Extract XML representation of XML during playback 
     * @param response
     *            the response to update.
     */
	@Override
	public void updateResponse(TestExec testExec, TransientResponse response) {
		//TODO hay que modificar la respuesta para que los tags XML sean planos
		logger.info("Playback Extract response.getBodyAsString() = " + response.getBodyAsString());
		PlaybackXMLEmbedded play = new PlaybackXMLEmbedded(response.getBodyAsString());
        String xmlExtract = play.extract();
        logger.info("Playback Extract xmlExtract = " + xmlExtract);
        response.setBody(xmlExtract);
        super.updateResponse(testExec, response);
        String xml = response.getBodyAsString();
        logger.info("Playback xml = " + xml);
        play.inject( xml);
        String playString = play.toString();
        logger.info("playString = " + playString);
        response.setBody(playString);
	}

}
