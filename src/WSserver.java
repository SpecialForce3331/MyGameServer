import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;

public class WSserver extends WebSocketServlet
{
	 	private final static Set<EchoMessageInbound> connections = new CopyOnWriteArraySet<EchoMessageInbound>();

	    @Override
	    protected StreamInbound createWebSocketInbound(String subProtocol,
	            HttpServletRequest request) {
	        return new EchoMessageInbound();
	    }

	    private static final class EchoMessageInbound extends MessageInbound {

	        @Override
	        protected void onBinaryMessage(ByteBuffer message) throws IOException {
	            getWsOutbound().writeBinaryMessage(message);
	            
	        }

	        @Override
	        protected void onTextMessage(CharBuffer message) throws IOException {
	            getWsOutbound().writeTextMessage(message);
	            broadcast(message);
	        }
	        
	        private void broadcast(CharBuffer message) {
	            for (EchoMessageInbound connection : connections) {
	                try {	               
	                    	connection.getWsOutbound().writeTextMessage(message);
	                } catch (IOException ignore) {
	                    // Ignore
	                }
	            }
	        }
	    }
	}
