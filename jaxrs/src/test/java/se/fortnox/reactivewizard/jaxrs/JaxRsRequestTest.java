package se.fortnox.reactivewizard.jaxrs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.MockHttpServerRequest;
import org.junit.Assert;
import org.junit.Test;
import rx.subjects.UnicastSubject;

import java.nio.charset.Charset;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.fest.assertions.Assertions.assertThat;

public class JaxRsRequestTest {
    @Test
    public void shouldDecodeMultiChunkBody() {
        UnicastSubject<ByteBuf> content   = UnicastSubject.create();
        byte[]                  byteArray = "ö".getBytes(Charset.defaultCharset());
        content.onNext(Unpooled.wrappedBuffer(new byte[]{byteArray[0]}));
        content.onNext(Unpooled.wrappedBuffer(new byte[]{byteArray[1]}));
        content.onCompleted();
        HttpServerRequest<ByteBuf> serverReq = new MockHttpServerRequest("/", HttpMethod.POST, content);
        JaxRsRequest               req       = new JaxRsRequest(serverReq, new ByteBufCollector());
        String                     body      = new String(req.loadBody().toBlocking().single().getBody());
        assertThat(body).isEqualTo("ö");
    }

    @Test
    public void shouldDecodeSingleChunkBody() {
        HttpServerRequest<ByteBuf> serverReq = new MockHttpServerRequest("/", HttpMethod.POST, "ö");
        JaxRsRequest               req       = new JaxRsRequest(serverReq, new ByteBufCollector());
        String                     body      = new String(req.loadBody().toBlocking().single().getBody());
        assertThat(body).isEqualTo("ö");
    }

    @Test
    public void shouldDecodeBodyForDeleteRequests() {
        HttpServerRequest<ByteBuf> serverReq = new MockHttpServerRequest("/", HttpMethod.DELETE, "test");
        JaxRsRequest               req       = new JaxRsRequest(serverReq, new ByteBufCollector());
        String                     body      = new String(req.loadBody().toBlocking().single().getBody());
        assertThat(body).isEqualTo("test");
    }

    @Test
    public void shouldDecodeBodyOfSpecifiedSize() {
        String input = generateLargeString(5);
        HttpServerRequest<ByteBuf> serverReq = new MockHttpServerRequest("/", HttpMethod.DELETE, input);
        JaxRsRequest               req       = new JaxRsRequest(serverReq, new ByteBufCollector(5 * 1024 * 1024));
        String                     body      = new String(req.loadBody().toBlocking().single().getBody());
        assertThat(body).isEqualTo(input);
    }

    @Test
    public void shouldFailWhenDecodingTooLargeBody() {
        String input = generateLargeString(6);
        HttpServerRequest<ByteBuf> serverReq = new MockHttpServerRequest("/", HttpMethod.DELETE, input);
        JaxRsRequest               req       = new JaxRsRequest(serverReq, new ByteBufCollector(5 * 1024 * 1024));
        try {
            req.loadBody().toBlocking().single();
            Assert.fail("Should throw exception");
        } catch (WebException e) {
            assertThat(e.getError()).isEqualTo("too.large.input");
        }
    }

    @Test
    public void testParams() throws Exception {
        MockHttpServerRequest serverReq = new MockHttpServerRequest("/");
        JaxRsRequest          req       = new JaxRsRequest(serverReq, null, new byte[0], new ByteBufCollector());
        assertThat(req.getPathParam("path")).isNull();
        assertThat(req.getQueryParam("query")).isNull();
        assertThat(req.getFormParam("form")).isNull();
        assertThat(req.getHeader("header")).isNull();
        assertThat(req.getCookie("cookie")).isNull();
    }


    private String generateLargeString(int sizeInMB) {
        String largeString = IntStream.range(1, sizeInMB * 1024 * 1024 - 1)
            .mapToObj(i -> "a")
            .collect(Collectors.joining());

        System.out.println(largeString.getBytes().length);

        return "\"" + largeString + "\"";
    }

}
