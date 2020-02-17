package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.body.BodyResult;
import com.reedelk.runtime.api.message.content.ByteArrayContent;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.Part;
import com.reedelk.runtime.api.message.content.Parts;
import org.apache.http.HttpEntity;
import org.apache.http.nio.entity.NByteArrayEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpEntityBuilderTest {

    @Mock
    private BodyResult bodyResult;

    @Test
    void shouldBuildByteArrayEntityCorrectly() {
        // Given
        byte[] contentAsBytes = "Test content body".getBytes();

        doReturn(false)
                .when(bodyResult)
                .isMultipart();

        doReturn(contentAsBytes)
                .when(bodyResult)
                .getDataAsBytes();

        // When
        HttpEntity actual = HttpEntityBuilder.get()
                .bodyProvider(bodyResult)
                .build();

        // Then
        assertThat(actual).isInstanceOf(NByteArrayEntity.class);
        assertThat(actual.getContentLength()).isEqualTo(contentAsBytes.length);

        verify(bodyResult, never()).getDataAsMultipart();
    }

    @Test
    void shouldBuildMultipartEntityCorrectly() {
        // Given
        ByteArrayContent pictureContent = new ByteArrayContent("picturebytes".getBytes(), MimeType.IMAGE_JPEG);
        Part myPicturePart = Part.builder().name("myPicture")
                .attribute("filename", "my_picture.jpg")
                .content(pictureContent)
                .build();

        ByteArrayContent fileContent = new ByteArrayContent("filebytes".getBytes(), MimeType.APPLICATION_BINARY);
        Part myFilePart = Part.builder().name("myFile")
                .attribute("filename", "myFile.wav")
                .content(fileContent)
                .build();

        Parts parts = new Parts();
        parts.put("myPicturePart", myPicturePart);
        parts.put("myFilePart", myFilePart);

        doReturn(true)
                .when(bodyResult)
                .isMultipart();

        doReturn(parts)
                .when(bodyResult)
                .getDataAsMultipart();

        // When
        HttpEntity actual = HttpEntityBuilder.get()
                .bodyProvider(bodyResult)
                .build();

        // Then
        assertThat(actual).isInstanceOf(MultipartFormEntityWrapper.class);
        verify(bodyResult, never()).getDataAsBytes();
    }
}
