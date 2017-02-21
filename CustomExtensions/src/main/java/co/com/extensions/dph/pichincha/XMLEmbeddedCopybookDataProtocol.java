package co.com.extensions.dph.pichincha;

import java.util.logging.Logger;

import com.itko.lisa.test.TestExec;
import com.itko.lisa.vse.stateful.model.Response;
import com.itko.lisa.vse.stateful.model.TransientResponse;
import com.itko.lisa.vse.stateful.protocol.copybook.CopybookDataProtocol;

/**
 * 
 * @author wtruong
 * 
 * Copybook Data Protocol Handler for response SOAP/XML with embedded copybook payload
 */
public class XMLEmbeddedCopybookDataProtocol extends CopybookDataProtocol {
    protected static Logger log   = Logger.getLogger(XMLEmbeddedCopybookDataProtocol.class.getName());
    
    /**
     * Extract copybook payload during recording
     * 
     * @param response
     *            the response to update.
     */
    @Override
    public void updateResponse(TestExec testExec, Response response) {
        Recording rec = new Recording(response.getBodyAsString());
        String copybook = rec.extract();
        response.setBody(copybook);
        super.updateResponse(testExec, response);
        String xmlCopybook = response.getBodyAsString();
        log.info("xmlCopybook = " + xmlCopybook);
        rec.inject(xmlCopybook);
        String recString = rec.toString();
        log.info("recString = " + recString);
        response.setBody(recString);
    }
    
    /**
     * Extract XML representation of copybook during playback 
     * @param response
     *            the response to update.
     */
    @Override
    public void updateResponse(TestExec testExec, TransientResponse response) {
        Playback play = new Playback(response.getBodyAsString());
        String xmlCopybook = play.extract();
        response.setBody(xmlCopybook);
        super.updateResponse(testExec, response);
        String copybook = response.getBodyAsString();
        log.info("copybook = " + copybook);
        play.inject(copybook);
        String playString = play.toString();
        log.info("playString = " + playString);
        response.setBody(playString);
    }
}
