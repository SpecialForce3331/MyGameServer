import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;

public class WSserver extends WebSocketServlet
{
	    private static final long serialVersionUID = 1L;
	    private volatile int byteBufSize;
	    private volatile int charBufSize;

	    @Override
	    public void init() throws ServletException {
	        super.init();
	        byteBufSize = getInitParameterIntValue("byteBufferMaxSize", 2097152);
	        charBufSize = getInitParameterIntValue("charBufferMaxSize", 2097152);
	    }

	    public int getInitParameterIntValue(String name, int defaultValue) {
	        String val = this.getInitParameter(name);
	        int result;
	        if(null != val) {
	            try {
	                result = Integer.parseInt(val);
	            }catch (Exception x) {
	                result = defaultValue;
	            }
	        } else {
	            result = defaultValue;
	        }

	        return result;
	    }

	    @Override
	    protected StreamInbound createWebSocketInbound(String subProtocol,
	            HttpServletRequest request) {
	        return new EchoMessageInbound(byteBufSize,charBufSize);
	    }

	    private static final class EchoMessageInbound extends MessageInbound {

	        public EchoMessageInbound(int byteBufferMaxSize, int charBufferMaxSize) {
	            super();
	            setByteBufferMaxSize(byteBufferMaxSize);
	            setCharBufferMaxSize(charBufferMaxSize);
	        }

	        @Override
	        protected void onBinaryMessage(ByteBuffer message) throws IOException {
	            getWsOutbound().writeBinaryMessage(message);
	        }

	        @Override
	        protected void onTextMessage(CharBuffer message) throws IOException {
	            getWsOutbound().writeTextMessage(message);
	        }
	    }
	}
