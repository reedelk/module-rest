package de.codecentric.reedelk.rest.internal.server.mapper;

import de.codecentric.reedelk.rest.component.RESTListener;
import de.codecentric.reedelk.rest.internal.ExecutionException;
import de.codecentric.reedelk.runtime.api.exception.PlatformException;
import de.codecentric.reedelk.runtime.api.message.MessageBuilder;
import de.codecentric.reedelk.runtime.api.message.content.Attachment;
import de.codecentric.reedelk.runtime.api.message.content.MimeType;
import de.codecentric.reedelk.runtime.api.type.MapOfAttachments;
import de.codecentric.reedelk.rest.internal.commons.Messages;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

import static io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

class HttpRequestMultipartFormDataMapper {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestMultipartFormDataMapper.class);

    private HttpRequestMultipartFormDataMapper() {
    }

    static MessageBuilder map(HttpRequestWrapper request) {
        if (HttpMethod.POST != HttpMethod.valueOf(request.method()) ||
                HttpVersion.HTTP_1_1 != HttpVersion.valueOf(request.version())) {
            throw new ExecutionException(Messages.RestListener.ERROR_MULTIPART_NOT_SUPPORTED.format());
        }

        Mono<MapOfAttachments> partsMono = request.data().aggregate().flatMap(byteBuffer -> {

            HttpPostRequestDecoder postDecoder = null;
            FullHttpRequest fullHttpRequest = null;
            try {

                // POST Multipart is only supported on HTTP 1.1 and for POST method.
                fullHttpRequest = new DefaultFullHttpRequest(
                        HttpVersion.HTTP_1_1,
                        HttpMethod.POST,
                        request.requestUri(),
                        byteBuffer,
                        request.requestHeaders(),
                        EmptyHttpHeaders.INSTANCE);

                postDecoder = new HttpPostRequestDecoder(
                        new DefaultHttpDataFactory(false),
                        fullHttpRequest,
                        CharsetUtil.UTF_8);

                MapOfAttachments parts = new MapOfAttachments();

                // Loop attribute/file upload parts
                for (InterfaceHttpData data : postDecoder.getBodyHttpDatas()) {
                    if (HttpDataType.Attribute == data.getHttpDataType()) {
                        handleAttributePart(parts, (Attribute) data);

                    } else if (HttpDataType.FileUpload == data.getHttpDataType()) {
                        handleFileUploadPart(parts, (FileUpload) data);
                    }
                }

                return Mono.just(parts);

            } finally {
                if (postDecoder != null) postDecoder.destroy();
                if (fullHttpRequest != null) fullHttpRequest.release();
            }
        });

        return MessageBuilder.get(RESTListener.class)
                .withJavaObject(partsMono, MapOfAttachments.class);
    }

    private static void handleFileUploadPart(Map<String,Attachment> parts, FileUpload fileUpload) {
        String name = fileUpload.getName();

        byte[] fileContentAsBytes;
        try {
            fileContentAsBytes = fileUpload.get();
        } catch (IOException exception) {
            PlatformException rethrown = new PlatformException(Messages.RestListener.ERROR_MULTIPART_FILE_UPLOAD_VALUE.format(name), exception);
            logger.error("Multipart Mapper error", rethrown);
            throw rethrown;
        } finally {
            // We MUST call delete otherwise the associated ByteBuffer
            // related to this part will NOT be released and a memory
            // leak would occur.
            fileUpload.delete();
        }

        String filename = fileUpload.getFilename();
        String contentType = fileUpload.getContentType();
        String contentTransferEncoding = fileUpload.getContentTransferEncoding();

        MimeType mimeType = MimeType.parse(contentType);
        Attachment part = Attachment.builder()
                .name(name)
                .attribute(MultipartAttribute.TRANSFER_ENCODING, contentTransferEncoding)
                .attribute(MultipartAttribute.CONTENT_TYPE, contentType)
                .attribute(MultipartAttribute.FILE_NAME, filename)
                .data(fileContentAsBytes)
                .mimeType(mimeType)
                .build();
        parts.put(name, part);
    }

    private static void handleAttributePart(Map<String,Attachment> parts, Attribute attribute) {
        String name = attribute.getName();
        String attributeValue;
        try {
            attributeValue = attribute.getValue();
        } catch (IOException e) {
            PlatformException rethrown = new PlatformException(Messages.RestListener.ERROR_MULTIPART_ATTRIBUTE_VALUE.format(name), e);
            logger.error("Multipart Mapper error", rethrown);
            throw rethrown;
        }

        Attachment part = Attachment.builder()
                .name(name)
                .data(attributeValue.getBytes())
                .mimeType(MimeType.TEXT_PLAIN)
                .build();
        parts.put(name, part);
    }
}
